package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.UserAuthBean;
import com.openu.apis.beans.UserBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;
import com.openu.apis.dal.IResultSetExtractor;
import com.openu.apis.dal.MySqlDal;
import com.openu.apis.exceptions.*;
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

    private UserAuthBean toAuth(ResultSet rs) throws SQLException {
        int userId = rs.getInt("userId");
        String token = rs.getString("token");
        Timestamp timestamp = rs.getTimestamp("createdTimestamp"); //todo: change to timestamp

        return new UserAuthBean(userId, token, timestamp);
    }

    public UserAuthBean getAuthByToken(String token) throws GetTokenException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("SELECT  * FROM `e-commerce`.user_tokens where token = ?;");
            preparedStatement.setString(1, token);

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return toAuth(rs);
            }
        } catch (SQLException e) {
            throw new GetTokenException(String.format("Error getting token: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }

        return null;
    }

    public void refreshToken(UserAuthBean auth) throws RefreshTokenException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("UPDATE `e-commerce`.user_tokens SET createTs = ? where userId = ?;");
            preparedStatement.setTimestamp(1, auth.getTimestamp());
            preparedStatement.setInt(2, auth.getUserId());

            int res = preparedStatement.executeUpdate();
            if(res != 1){
                throw new RefreshTokenException(String.format("Unknown error refreshing token for userId: %s.", auth.getUserId()));
            }
        } catch (SQLException e) {
            throw new RefreshTokenException(String.format("Error refreshing token: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public void insertToken(UserAuthBean auth) throws InsertTokenException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO `e-commerce`.user_tokens (`userId`, `token`, `createTs`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE token = ?, createTs = ?;");
            preparedStatement.setInt(1, auth.getUserId());
            preparedStatement.setString(2, auth.getToken());
            preparedStatement.setTimestamp(3, auth.getTimestamp());
            preparedStatement.setString(4, auth.getToken());
            preparedStatement.setTimestamp(5, auth.getTimestamp());

            int res = preparedStatement.executeUpdate();
            if(res != 1){
                throw new InsertTokenException(String.format("Unknown error inserting token for userId: %s.", auth.getUserId()));
            }
        } catch (SQLException e) {
            throw new InsertTokenException(String.format("Error inserting token: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }
}
