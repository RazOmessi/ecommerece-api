package com.openu.apis.dal;

import com.openu.apis.exceptions.EcommerceException;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDal {
    Connection getConnection() throws SQLException;
    void closeConnection(Connection con);

    <K> K getLastInsertId(Connection con, IResultSetExtractor<K> extractor) throws SQLException, EcommerceException;
}
