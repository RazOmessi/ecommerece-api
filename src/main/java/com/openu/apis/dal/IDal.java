package com.openu.apis.dal;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDal {
    Connection getConnection() throws SQLException;
    void closeConnection(Connection con);
}
