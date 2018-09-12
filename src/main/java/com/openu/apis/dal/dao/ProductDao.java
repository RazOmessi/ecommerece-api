package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.LookupsException;
import com.openu.apis.exceptions.ProductDaoException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;
import com.openu.apis.utils.SqlUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        String url = Lookups.getInstance().getLkpProductImages().getLookup(productId);

        return new ProductBean(productId, category, vendor, name, description, price, unitsInStock, discount, url);
    }

    private String buildQuery(List<String> vendors, List<String> categories) throws ProductDaoException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM `e-commerce`.products");

        if (SqlUtils.isWhereClause(vendors, categories)) {
            queryBuilder.append(" where");
        }

        //todo: prepare statment
        if (vendors != null && !vendors.isEmpty()) {
            List<String> vendorIds = vendors.stream().map(vendor -> Lookups.getInstance().getLkpVendor().getReversedLookup(vendor).toString()).collect(Collectors.toList());
            queryBuilder.append(String.format(" vendorId in (%s)", String.join(",", vendorIds)));
        }

        //todo: prepare statment
        if (categories != null && !categories.isEmpty()) {
            List<String> categoriesIds = categories.stream().map(category -> Lookups.getInstance().getLkpCategory().getReversedLookup(category)).filter(Objects::nonNull).map(Object::toString).collect(Collectors.toList());
            //todo: remove this shit
            if(categoriesIds.isEmpty()){
                throw new ProductDaoException(String.format("Unknown category"));
            }
            if(vendors != null && !vendors.isEmpty())
                queryBuilder.append(" and");
            queryBuilder.append(String.format(" categoryId in (%s)", String.join(",", categoriesIds)));
        }

        queryBuilder.append(";");
        return queryBuilder.toString();
    }

    public List<ProductBean> getProducts(List<String> vendors, List<String> categories) throws SQLException, ProductDaoException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(buildQuery(vendors, categories));

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
                throw new ProductDaoException("Unknown error creating product.");
            }

            int productId = _dal.getLastInsertId(con, new IResultSetExtractor<Integer>() {
                @Override
                public Integer extract(ResultSet rs) throws EcommerceException, SQLException {
                    if(rs.next()){
                        return rs.getInt("lastId");
                    } else {
                        throw new ProductDaoException("Unable to retrieve product id (product created)");
                    }
                }
            });

            Lookups.getInstance().getLkpProductImages().setLookup(productId, product.getImageUrl());
            return productId;
        } catch (SQLException e) {
            throw new ProductDaoException(String.format("Error creating product: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public ProductBean getProductById(int key) {
        return _products.getValue(key);
    }

    public boolean updateProductById(ProductBean product) throws EcommerceException{
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE `e-commerce`.products SET `categoryId` = ?, `vendorId` = ?, `name` = ?, `description` = ?, `price` = ?, `unitsInStock` = ?, `discount` = ? WHERE id = ?;");
            preparedStatement.setInt(1, Lookups.getInstance().getLkpCategory().getReversedLookup(product.getCategory()));
            preparedStatement.setInt(2, Lookups.getInstance().getLkpVendor().getReversedLookup(product.getVendor()));
            preparedStatement.setString(3, product.getName());
            preparedStatement.setString(4, product.getDescription());
            preparedStatement.setDouble(5, product.getPrice());
            preparedStatement.setInt(6, product.getUnitsInStock());
            preparedStatement.setInt(7, product.getDiscount());
            preparedStatement.setInt(8, product.getId());

            int res = preparedStatement.executeUpdate();
            if(res < 1){
                return false;
            }

            Lookups.getInstance().getLkpProductImages().setLookup(product.getId(), product.getImageUrl());
            _products.reload(product.getId());

            return true;
        } catch (SQLException e) {
            throw new ProductDaoException(String.format("Error updating product: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

}
