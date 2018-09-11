package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.OrderBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.CreateCategoryException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.exceptions.OrderDAOException;
import com.openu.apis.lookups.Lookups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryDao {
    private static CategoryDao _instance;

    private IDal _dal;

    public static CategoryDao getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (Product.class) {
            if (_instance == null) {
                _instance = new CategoryDao(MySqlDal.getInstance());
            }

            return _instance;
        }
    }

    private CategoryDao(IDal dal) {
        this._dal = dal;
    }

    public void createCategories(List<String> categories) throws EcommerceException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            String queryBuilder = "INSERT INTO `e-commerce`.categories (`name`) VALUES " +
                    String.join(",", categories.stream().map(order -> "(?) ").collect(Collectors.toList())) +
                    ";";

            PreparedStatement preparedStatement = con.prepareStatement(queryBuilder);
            int index = 1;
            for(String category : categories){
                preparedStatement.setString(index++, category);
            }

            int res = preparedStatement.executeUpdate();
            if(res != categories.size()){
                throw new CreateCategoryException("Unknown error creating category.");
            }

        } catch (SQLException e) {
            throw new OrderDAOException(String.format("Error creating category: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }
}
