package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.ProductBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.lookups.Lookups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDao {
    private final int MAX_SIZE = 1000;

    private static ProductDao _instance;

    private IDal _dal;
    private DataCache<Integer, ProductBean> _products;

    public static ProductDao getInstance() {
        if(_instance != null){
            return _instance;
        }

        synchronized(Product.class) {
            if(_instance == null){
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
                try{
                    con = _dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM `e-commerce`.products WHERE id = ? limit 1;");
                    preparedStatement.setInt(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if(rs.next()){
                        return toProduct(rs);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
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

    public List<ProductBean> getAllProducts() throws SQLException {
        Connection con = null;
        try{
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `e-commerce`.products;");

            List<ProductBean> res = new ArrayList<ProductBean>();
            while(rs.next()){
                res.add(toProduct(rs));
            }
            return res;
        } finally {
            if(con != null){
                _dal.closeConnection(con);
            }
        }
    }

//    public List<ProductBean> getAllProductsByCategory() {
//
//    }
//
//    public List<ProductBean> getAllProductsByProvider() {
//
//    }

    public ProductBean getProductById(int key) {
        return _products.getValue(key);
    }

}
