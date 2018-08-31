package com.openu.apis.dal.dao;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LookupDao<K, V> {
    private DataCache<K, V> _lookups;

    @SuppressWarnings("unchecked")
    public LookupDao(final IDal dal, final LookupTableBean table){
        this._lookups = new DataCache<K, V>(table.getMaxCacheSize(), new ICacheLoader<K, V>() {
            public V load(K key) {
                Connection con = null;
                try {
                    con = dal.getConnection();
                    PreparedStatement preparedStatement = con.prepareStatement(String.format("SELECT %s FROM `%s`.%s WHERE %s = ? limit 1;", table.getValueColumn(), table.getSchemaName(), table.getTableName(), table.getKeyColumn()));
                    preparedStatement.setObject(1, key);
                    ResultSet rs = preparedStatement.executeQuery();

                    if (rs.next()) {
                        return (V) rs.getObject(table.getValueColumn());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        dal.closeConnection(con);
                    }
                }
                return null;
            }
        });
    }

    public V getLookup(K key){
        V value = this._lookups.getValue(key);
        if(value == null){
            //todo: find the right exception to throw
            throw new IllegalArgumentException("lookup value was not found");
        }
        return value;
    }
}
