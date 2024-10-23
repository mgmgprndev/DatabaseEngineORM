package com.mogukun.databaseengine.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Table {

    private String name;
    private Connection connection;

    public Table(String name, Connection connection) {
        this.name = sanitizeIdentifier(name); // Sanitize table name
        this.connection = connection;
        if (!isExists()) {
            System.out.println("[DatabaseEngine] " + name + " table does not exist!");
        }
    }

    public Table(String name, Connection connection, boolean creationMode) {
        this.name = sanitizeIdentifier(name); // Sanitize table name
        this.connection = connection;
        if (isExists()) {
            System.out.println("[DatabaseEngine] Failed to create " + name + " table. Table already exists.");
            return;
        }
        try (PreparedStatement pstmt = connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + name + " (ID INT PRIMARY KEY AUTO_INCREMENT)")) {
            pstmt.executeUpdate();
            System.out.println("[DatabaseEngine] Successfully created " + name + " table.");
        } catch (SQLException e) {
            System.out.println("[DatabaseEngine] Failed to create " + name + " table: " + e.getMessage());
        }
    }

    public Table addColumn(String column_name, String datatype) {
        column_name = sanitizeIdentifier(column_name); // Sanitize column name
        datatype = sanitizeDatatype(datatype); // Validate datatype
        String sql = "ALTER TABLE " + name + " ADD " + column_name + " " + datatype;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("[DatabaseEngine] Successfully added " + column_name + " (" + datatype + ") to " + name + " table.");
        } catch (SQLException e) {
            System.out.println("[DatabaseEngine] Failed to add column: " + e.getMessage());
        }
        return this;
    }

    public Table addColumn(String column_name, String datatype, String defaultValue) {
        column_name = sanitizeIdentifier(column_name); // Sanitize column name
        datatype = sanitizeDatatype(datatype); // Validate datatype
        String sql = "ALTER TABLE " + name + " ADD " + column_name + " " + datatype + " DEFAULT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, defaultValue);
            pstmt.executeUpdate();
            System.out.println("[DatabaseEngine] Successfully added " + column_name + " (" + datatype + ") to " + name + " table.");
        } catch (SQLException e) {
            System.out.println("[DatabaseEngine] Failed to add column: " + e.getMessage());
        }
        return this;
    }

    public Record create() {
        return new Record(name, connection, true);
    }

    public Query find() {
        return new Query(name, connection);
    }

    public boolean isExists() {
        String sql = "SHOW TABLES LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("[DatabaseEngine] Failed to check if table exists: " + e.getMessage());
        }
        return false;
    }

    public List<Record> all() {
        return find().all();
    }

    public int size() {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM " + name;
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[DatabaseEngine] Failed to get size of the table " + name + ": " + e.getMessage());
        }
        return count;
    }

    // Sanitize SQL identifiers like table/column names
    private String sanitizeIdentifier(String identifier) {
        if (!identifier.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + identifier);
        }
        return identifier;
    }

    // Validate SQL datatypes (basic check, expand as needed)
    private String sanitizeDatatype(String datatype) {
        if (!datatype.matches("(?i)TEXT|INT|VARCHAR|BOOLEAN|BIGINT|DECIMAL|NUMERIC|FLOAT|DOUBLE|DATE|DATETIME|TIMESTAMP|CHAR|BLOB|CLOB|TINYINT|SMALLINT|MEDIUMINT|REAL|BINARY|VARBINARY|TIME|YEAR")) {
            throw new IllegalArgumentException("Unsupported SQL datatype: " + datatype);
        }
        return datatype.toUpperCase();
    }
}
