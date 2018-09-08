package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.CreateProductException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDao {
    private final int MAX_SIZE = 1000;

    private static ProductDao _instance;

    private IDal _dal;
    private DataCache<Integer, ProductBean> _products;

    public static ProductDao getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (Product.class) {
            if (_instance == null) {
                _instance = new ProductDao(MySqlDal.getInstance());
            }

            return _instance;
        }
    }

    private ProductDao(IDal dal) {
        this._dal = dal;
        this._products = new DataCache<Integer, ProductBean>(MAX_SIZE, new ICacheLoader<Integer, ProductBean>() {
            public ProductBean load(Integer key) {
                Connection con = null;
                try {
                    con = _dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM `e-commerce`.products WHERE id = ? limit 1;");
                    preparedStatement.setInt(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()) {
                        return toProduct(rs);
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

    private ProductBean toProduct(ResultSet rs) throws SQLException {
        int productId = rs.getInt("id");
        int categoryId = rs.getInt("categoryId");
        int vendorId = rs.getInt("vendorId");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double price = rs.getDouble("price");
        int unitsInStock = rs.getInt("unitsInStock");
        int discount = rs.getInt("discount");

        String category = Lookups.getInstance().getLkpCategory().getLookup(categoryId);
        String vendor = Lookups.getInstance().getLkpVendor().getLookup(vendorId);

        return new ProductBean(productId, category, vendor, name, description, price, unitsInStock, discount);
    }

    @SafeVarargs
    private final boolean isWhereClause(List<String>... filters) {
        for (List<String> filter : filters) {
            if (filter != null && !filter.isEmpty())
                return true;
        }
        return false;
    }

    private String buildQuery(List<String> vendors) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM `e-commerce`.products");

        if (isWhereClause(vendors)) {
            queryBuilder.append(" where");
        }

        //todo: prepare statment
        if (vendors != null && !vendors.isEmpty()) {
            List<String> vendorIds = vendors.stream().map(vendor -> Lookups.getInstance().getLkpVendor().getReversedLookup(vendor).toString()).collect(Collectors.toList());
            queryBuilder.append(String.format(" vendorId in (%s)", String.join(",", vendorIds)));
        }

        queryBuilder.append(";");
        return queryBuilder.toString();
    }

    public List<ProductBean> getProducts(List<String> vendors) throws SQLException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(buildQuery(vendors));

            List<ProductBean> res = new ArrayList<ProductBean>();
            while (rs.next()) {
                res.add(toProduct(rs));
            }
            return res;
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public int createProduct(ProductBean product) throws EcommerceException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO `e-commerce`.products (`categoryId`, `vendorId`, `name`, `description`, `price`, `unitsInStock`, `discount`) VALUES (?, ?, ?, ?, ?, ?, ?);");
            preparedStatement.setInt(1, Lookups.getInstance().getLkpCategory().getReversedLookup(product.getCategory()));
            preparedStatement.setInt(2, Lookups.getInstance().getLkpVendor().getReversedLookup(product.getVendor()));
            preparedStatement.setString(3, product.getName());
            preparedStatement.setString(4, product.getDescription());
            preparedStatement.setDouble(5, product.getPrice());
            preparedStatement.setInt(6, product.getUnitsInStock());
            preparedStatement.setInt(7, product.getDiscount());

            int res = preparedStatement.executeUpdate();
            if(res != 1){
                throw new CreateProductException("Unknown error creating product.");
            }

            return _dal.getLastInsertId(con, new IResultSetExtractor<Integer>() {
                @Override
                public Integer extract(ResultSet rs) throws EcommerceException, SQLException {
                    if(rs.next()){
                        return rs.getInt("lastId");
                    } else {
                        throw new CreateProductException("Unable to retrieve product id (product created)");
                    }
                }
            });
        } catch (SQLException e) {
            throw new CreateProductException(String.format("Error creating product: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public ProductBean getProductById(int key) {
        return _products.getValue(key);
    }

}
