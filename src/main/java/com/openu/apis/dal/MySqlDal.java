package com.openu.apis.dal;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MySqlDal implements IDal{
    private static MySqlDal _instance;

    private HikariDataSource ds;

    public static MySqlDal getInstance() {
        if(_instance != null){
            return _instance;
        }

        synchronized(MySqlDal.class) {
            if(_instance == null){
                _instance = new MySqlDal("");
            }

            return _instance;
        }
    }

    private MySqlDal(String confFilePath){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://81.171.7.222:3306/internal?useSSL=false&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("A#akuoGkhfo8");
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setMaximumPoolSize(5);
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void closeConnection(Connection con) {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
