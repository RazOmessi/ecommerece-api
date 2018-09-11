package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.OrderBean;
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
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class OrderDao {
    private static final int MAX_SIZE = 1000;

    private static OrderDao _instance;

    private IDal _dal;
    private DataCache<Integer, OrderBean> _orders;

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
        this._orders = new DataCache<Integer, OrderBean>(MAX_SIZE, new ICacheLoader<Integer, OrderBean>() {
            public OrderBean load(Integer key) {
                Connection con = null;
                try {
                    con = _dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM `e-commerce`.orders WHERE id = ? limit 1;");
                    preparedStatement.setInt(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()) {
                        return toOrder(rs);
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

    private OrderBean toOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("id");
        int userId = rs.getInt("userId");
        int productId = rs.getInt("productId");
        Timestamp timestamp = rs.getTimestamp("timestamp");
        int amount = rs.getInt("amount");
        int statusId = rs.getInt("statusId");

        String status = Lookups.getInstance().getLkpOrderStatuses().getLookup(statusId);

        return new OrderBean(orderId, userId, productId, timestamp, amount, status);
    }

    private String buildQuery(Integer userId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM `e-commerce`.orders");

        if (userId != null) {
            queryBuilder.append(" where");
            queryBuilder.append(String.format(" userId = %s", userId));
        }

        queryBuilder.append(";");
        return queryBuilder.toString();
    }

    public List<OrderBean> getOrders() throws SQLException {
        return getOrders(null);
    }

    public List<OrderBean> getOrders(Integer userId) throws SQLException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(buildQuery(userId));

            List<OrderBean> res = new ArrayList<>();
            while (rs.next()) {
                res.add(toOrder(rs));
            }
            return res;
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public int createOrders(int userId, List<OrderBean> orders) throws EcommerceException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            String queryBuilder = "INSERT INTO `e-commerce`.orders (`userId`, `productId`, `amount`) VALUES " +
                    String.join(",", orders.stream().map(order -> "(?, ?, ?) ").collect(Collectors.toList())) +
                    ";";

            PreparedStatement preparedStatement = con.prepareStatement(queryBuilder);
            int index = 1;
            for(OrderBean order : orders){
                preparedStatement.setInt(index++, order.getUserId());
                preparedStatement.setInt(index++, order.getProductId());
                preparedStatement.setInt(index++, order.getAmount());
            }

            int res = preparedStatement.executeUpdate();
            if(res != orders.size()){
                throw new OrderDAOException("Unknown error creating order.");
            }

            return _dal.getLastInsertId(con, new IResultSetExtractor<Integer>() {
                @Override
                public Integer extract(ResultSet rs) throws EcommerceException, SQLException {
                    if(rs.next()){
                        return rs.getInt("lastId");
                    } else {
                        throw new OrderDAOException("Unable to retrieve order id (order created)");
                    }
                }
            });

        } catch (SQLException e) {
            throw new OrderDAOException(String.format("Error creating order: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public OrderBean getOrderById(int key) {
        return _orders.getValue(key);
    }

    public boolean updateOrder(OrderBean order) throws OrderDAOException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE `e-commerce`.orders SET statusId = ? WHERE id = ?;");
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
