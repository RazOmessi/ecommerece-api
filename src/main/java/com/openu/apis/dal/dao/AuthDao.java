package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.UserAuthBean;
import com.openu.apis.beans.UserBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.CreateUserException;
import com.openu.apis.exceptions.EcommerceException;
import com.openu.apis.lookups.Lookups;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthDao {
    private static AuthDao _instance;

    private IDal _dal;

    public static AuthDao getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (Product.class) {
            if (_instance == null) {
                _instance = new AuthDao(MySqlDal.getInstance());
            }

            return _instance;
        }
    }

    private AuthDao(IDal dal) {
        this._dal = dal;
    }

    private UserAuthBean toUserAuth(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        String token = rs.getString("token");
        Timestamp timestamp= rs.getTimestamp("createdTs");//todo: change to timestamp instead of createdTs

        return new UserAuthBean(userId, token, timestamp);
    }

    public UserAuthBean getAuthByToken(int key) {

    }

}
