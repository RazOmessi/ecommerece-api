package com.openu.apis.dal.dao;

import com.openu.apis.beans.LookupTableBean;
import com.openu.apis.cache.DataCache;
import com.openu.apis.cache.ICacheLoader;
import com.openu.apis.dal.IDal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LookupDao<K, V> {
    protected IDal dal;
    protected LookupTableBean table;

    protected DataCache<K, V> _lookups;

    public LookupDao(final IDal dal, final LookupTableBean table){
        this(dal, table, true);
    }

    @SuppressWarnings("unchecked")
    private List<V> getAllValuesFromDal() throws SQLException {
        Connection con = null;
        try {
            con = dal.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(String.format("SELECT %s, %s FROM `%s`.%s;", table.getKeyColumn(), table.getValueColumn(), table.getSchemaName(), table.getTableName()));

            List<V> results = new ArrayList<V>();
            while (rs.next()) {
                results.add((V) rs.getObject(table.getValueColumn()));
            }
            return results;
        } finally {
            if(con != null){
                dal.closeConnection(con);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private V getValueFromDal(K key) throws SQLException {
        Connection con = null;
        try {
            con = dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(String.format("SELECT %s FROM `%s`.%s WHERE %s = ? limit 1;", table.getValueColumn(), table.getSchemaName(), table.getTableName(), table.getKeyColumn()));
            preparedStatement.setObject(1, key);
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                return (V) rs.getObject(table.getValueColumn());
            }
        } finally {
            if(con != null){
                dal.closeConnection(con);
            }
        }

        return null;
    }

    private V insertToDal(K key) throws SQLException {
        Connection con = null;
        try {
            con = dal.getConnection();
            PreparedStatement preparedStatement = con.prepareStatement(String.format("INSERT INTO `%s`.%s (`%s`) VALUES (?);", table.getSchemaName(), table.getTableName(), table.getKeyColumn()));
            preparedStatement.setObject(1, key);
            if(preparedStatement.executeUpdate() == 1){
                return getValueFromDal(key);
            }
        } finally {
            if(con != null){
                dal.closeConnection(con);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public LookupDao(final IDal dal, final LookupTableBean table, final boolean isStatic){
        this.dal = dal;
        this.table = table;

        this._lookups = new DataCache<K, V>(table.getMaxCacheSize(), new ICacheLoader<K, V>() {
            public V load(K key) {
                try {
                    V value = getValueFromDal(key);
                    if(value != null) {
                        return value;
                    }

                    if(!isStatic){
                        return insertToDal(key);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    public V getLookup(K key){
        V value = this._lookups.getValue(key);
        if(value == null){
            //todo: add log4j with the specific message
            System.out.println("lookup value was not found");
        }
        return value;
    }

    public List<V> getAllValues(boolean fromCache, boolean syncCache) {
        try{
            return getAllValuesFromDal();
        } catch (SQLException e){
            e.printStackTrace();
            return null;
        }
    }
}
