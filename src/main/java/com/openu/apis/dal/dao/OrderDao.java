package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.OrderInfoBean;
import com.openu.apis.beans.OrderProductsBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.OrderDAOException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderDao {
    private static final int MAX_SIZE = 1000;

    private static OrderDao _instance;

    private IDal _dal;
    private DataCache<Integer, OrderInfoBean> _orders;

    public static OrderDao getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (Product.class) {
            if (_instance == null) {
                _instance = new OrderDao(MySqlDal.getInstance());
            }

            return _instance;
        }
    }

    private OrderDao(IDal dal) {
        this._dal = dal;
        this._orders = new DataCache<Integer, OrderInfoBean>(MAX_SIZE, new ICacheLoader<Integer, OrderInfoBean>() {
            public OrderInfoBean load(Integer key) {
                Connection con = null;
                try {
                    con = _dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("select id, userId, timestamp, statusId, productId, amount from ((select * from `e-commerce`.orders_info where id = ?) a left join (SELECT * FROM `e-commerce`.orders_products) b on a.id = b.orderId);");
                    preparedStatement.setInt(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()) {
                        rs.beforeFirst();
                        return toOrders(rs).get(0);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        _dal.closeConnection(con);
                    }
                }
                return null;
            }
        });
    }

    private List<OrderInfoBean> toOrders(ResultSet rs) throws SQLException {
        Map<Integer, OrderInfoBean> orders = new HashMap<>();

        while(rs.next()){
            int orderId = rs.getInt("id");
            int userId = rs.getInt("userId");
            Timestamp timestamp = rs.getTimestamp("timestamp");
            int statusId = rs.getInt("statusId");
            int productId = rs.getInt("productId");
            int amount = rs.getInt("amount");

            OrderProductsBean productsBean = new OrderProductsBean(productId, amount);

            if(orders.containsKey(orderId)){
                orders.get(orderId).getProducts().add(productsBean);
            } else {
                String status = Lookups.getInstance().getLkpOrderStatuses().getLookup(statusId);

                List<OrderProductsBean> products = new ArrayList<>();
                products.add(productsBean);

                orders.put(orderId, new OrderInfoBean(orderId, userId, timestamp, status, products));
            }
        }

        return new ArrayList<>(orders.values());
    }

    private String buildQuery(Integer userId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select id, userId, timestamp, statusId, productId, amount from ((select * from `e-commerce`.orders_info");

        if (userId != null) {
            queryBuilder.append(" where");
            queryBuilder.append(String.format(" userId = %s", userId));
        }

        queryBuilder.append(") a left join (SELECT * FROM `e-commerce`.orders_products) b on a.id = b.orderId);");

        return queryBuilder.toString();
    }

    public List<OrderInfoBean> getOrders() throws SQLException {
        return getOrders(null);
    }

    public List<OrderInfoBean> getOrders(Integer userId) throws SQLException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(buildQuery(userId));

            if (rs.next()) {
                rs.beforeFirst();
                return toOrders(rs);
            }
            return new ArrayList<>();
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public int createOrders(int userId, OrderInfoBean order) throws EcommerceException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            //todo: make this transaction
            String query = "INSERT INTO `e-commerce`.orders_info (`userId`) VALUES (?);";

            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            int insertCount = preparedStatement.executeUpdate();
            if(insertCount != 1){
                throw new OrderDAOException("Unknown error creating order.");
            }

            int orderId = _dal.getLastInsertId(con, new IResultSetExtractor<Integer>() {
                @Override
                public Integer extract(ResultSet rs) throws EcommerceException, SQLException {
                    if(rs.next()){
                        return rs.getInt("lastId");
                    } else {
                        throw new OrderDAOException("Unable to retrieve order id (order created)");
                    }
                }
            });

            query = "INSERT INTO `e-commerce`.orders_products (`orderId`, `productId`, `amount`) VALUES " +
                    String.join(",", order.getProducts().stream().map(o -> "(?, ?, ?) ").collect(Collectors.toList())) +
                    ";";

            preparedStatement = con.prepareStatement(query);
            int index = 1;
            for(OrderProductsBean opb : order.getProducts()){
                preparedStatement.setInt(index++, orderId);
                preparedStatement.setInt(index++, opb.getProductId());
                preparedStatement.setInt(index++, opb.getAmount());
            }

            int res = preparedStatement.executeUpdate();
            if(res != order.getProducts().size()){
                throw new OrderDAOException("Unknown error creating order.");
            }

            return orderId;

        } catch (SQLException e) {
            throw new OrderDAOException(String.format("Error creating order: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public OrderInfoBean getOrderById(int key) {
        return _orders.getValue(key);
    }

    public boolean updateOrder(OrderInfoBean order) throws OrderDAOException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE `e-commerce`.orders_info SET statusId = ? WHERE id = ?;");
            preparedStatement.setInt(1, Lookups.getInstance().getLkpOrderStatuses().getReversedLookup(order.getStatus()));
            preparedStatement.setInt(2, order.getId());

            int res = preparedStatement.executeUpdate();
            if(res < 1){
                return false;
            }

            //update cache
            getOrderById(order.getId()).setStatus(order.getStatus());
            return true;
        } catch (SQLException e) {
            throw new OrderDAOException(String.format("Error creating order: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }
}
