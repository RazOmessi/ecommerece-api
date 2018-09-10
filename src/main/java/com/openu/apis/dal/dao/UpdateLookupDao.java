package com.openu.apis.dal.dao;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.dal.IDal;
import com.openu.apis.exceptions.LookupsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateLookupDao<K,V> extends LookupDao<K, V> {
    public UpdateLookupDao(final IDal dal, final LookupTableBean table){
        super(dal, table, true);
    }

    public void setLookup(K key, V value) throws LookupsException {
        Connection con = null;
        try {
            con = dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(String.format("INSERT INTO `%s`.%s (`%s`, `%s`) VALUES (?, ?) ON DUPLICATE KEY UPDATE %s = ?;", table.getSchemaName(), table.getTableName(), table.getKeyColumn(), table.getValueColumn(), table.getValueColumn()));
            preparedStatement.setObject(1, key);
            preparedStatement.setObject(2, value);
            preparedStatement.setObject(3, value);
            if(preparedStatement.executeUpdate() != 1){
                throw new LookupsException("Unknow error store value.");
            }
        } catch (SQLException e){
            throw new LookupsException(String.format("Error storing value: %s", e.getMessage()));
        } finally {
            if(con != null){
                dal.closeConnection(con);
            }
        }
    }
}
