package com.mogukun.databaseengine.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseEngine {

    private String url;
    private String username;
    private String password;
    private Connection connection;
    private boolean isConnected;

    public DatabaseEngine(String host, String user, String pass, String database) {
        this.url = "jdbc:mysql://" + host + "/" + database;
        this.username = user;
        this.password = pass;
        try {
            connection = DriverManager.getConnection(url, username, password);
            isConnected = true;
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to connect to the database.");
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void shutdown() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to close the connection.");
        }
    }


    public Table getTable(String tableName) {
        return new Table(tableName, connection);
    }

    public Table createTable(String tableName) {
        return new Table(tableName, connection, true);
    }

}
