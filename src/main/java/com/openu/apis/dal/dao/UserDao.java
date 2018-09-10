package com.openu.apis.dal.dao;

import com.openu.apis.Product;
import com.openu.apis.beans.ProductBean;
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

public class UserDao {
    private final int MAX_SIZE = 1000;

    private static UserDao _instance;

    private IDal _dal;
    private DataCache<Integer, UserBean> _users;

    public static UserDao getInstance() {
        if (_instance != null) {
            return _instance;
        }

        synchronized (Product.class) {
            if (_instance == null) {
                _instance = new UserDao(MySqlDal.getInstance());
            }

            return _instance;
        }
    }

    private UserDao(IDal dal) {
        this._dal = dal;
        this._users = new DataCache<Integer, UserBean>(MAX_SIZE, new ICacheLoader<Integer, UserBean>() {
            public UserBean load(Integer key) {
                Connection con = null;
                try {
                    con = _dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM `e-commerce`.users WHERE id = ? limit 1;");
                    preparedStatement.setInt(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()) {
                        return toUser(rs);
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

    private UserBean toUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("id");
        String username = rs.getString("username");
        //String hashedPassword = rs.getString("hashedPassword");
        Integer roleId = rs.getInt("roleId");
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String email = rs.getString("email");
        String address = rs.getString("address");
        String city = rs.getString("city");
        String zipCode = rs.getString("zipCode");
        String phoneNumber = rs.getString("phoneNumber");

        String role = Lookups.getInstance().getLkpUserRole().getLookup(roleId);

        return new UserBean(userId, username, null, role, firstName, lastName, email, address, city, zipCode, phoneNumber);
    }

    public int createUser(UserBean user) throws EcommerceException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement("INSERT INTO `e-commerce`.products (`username`, `hashedPassword`, `roleId`, `firstName`, `lastName`, `email`, `address`, `city`, `zipCode`, `phoneNumber`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getHashedPassword());
            preparedStatement.setInt(3, Lookups.getInstance().getLkpUserRole().getReversedLookup(user.getRoleId()));
            preparedStatement.setString(4, user.getFirstName());
            preparedStatement.setString(5, user.getLastName());
            preparedStatement.setString(6, user.getEmail());
            preparedStatement.setString(7, user.getAddress());
            preparedStatement.setString(8, user.getCity());
            preparedStatement.setString(9, user.getZipCode());
            preparedStatement.setString(10, user.getPhoneNumber());

            int res = preparedStatement.executeUpdate();
            if(res != 1){
                throw new CreateUserException("Unknown error creating user.");
            }

            return _dal.getLastInsertId(con, new IResultSetExtractor<Integer>() {
                @Override
                public Integer extract(ResultSet rs) throws EcommerceException, SQLException {
                    if(rs.next()){
                        return rs.getInt("lastId");
                    } else {
                        throw new CreateUserException("Unable to retrieve user id (user created)");
                    }
                }
            });
        } catch (SQLException e) {
            throw new CreateUserException(String.format("Error creating user: %s", e.getMessage()));
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }

    public List<UserBean> getAllUser() throws SQLException {
        Connection con = null;
        try {
            con = _dal.getConnection();
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM `e-commerce`.users;");

            List<UserBean> res = new ArrayList<UserBean>();
            while (rs.next()) {
                res.add(toUser(rs));
            }
            return res;
        } finally {
            if (con != null) {
                _dal.closeConnection(con);
            }
        }
    }


    public UserBean getUserById(int key) {
        return _users.getValue(key);
    }

}
