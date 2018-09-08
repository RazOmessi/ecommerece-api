package com.openu.apis.dal;

import com.openu.apis.exceptions.EcommerceException;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IResultSetExtractor<V> {
    /**
     *
     * @param rs The ResultSet of the get last ID response
     * @return The last inserted ID
     * @throws EcommerceException Throws the relevant EcommerceException for the specific flow
     * @throws SQLException Throws for SQL errors
     */
    V extract(ResultSet rs) throws EcommerceException, SQLException;
}