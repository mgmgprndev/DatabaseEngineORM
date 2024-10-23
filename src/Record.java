package com.mogukun.databaseengine.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Record {

    private String tableName;
    private Connection connection;
    private boolean creationMode;
    private Map<String, Object> fields; // To hold field values
    private Integer id; // To identify the record (if it exists)

    // Constructor for existing records
    public Record(String tableName, Connection connection) {
        this(tableName, connection, false);
    }

    // Constructor for new records
    public Record(String tableName, Connection connection, boolean creationMode) {
        this.tableName = sanitizeTableName(tableName); // Prevent SQL Injection via table name
        this.connection = connection;
        this.creationMode = creationMode;
        this.fields = new HashMap<>();
    }

    // Sanitize table names by allowing only certain valid names or patterns
    private String sanitizeTableName(String tableName) {
        // Add logic to validate table names (e.g., a whitelist, regex pattern, or specific checks)
        // Example of a simple regex check for valid table names
        if (!tableName.matches("[a-zA-Z_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }
        return tableName;
    }

    // Method to set field values
    public Record set(String fieldName, Object value) {
        fields.put(fieldName, value);
        if (fieldName.equalsIgnoreCase("ID")) {
            id = (int) value;
        }
        return this;
    }

    // Method to get a String value
    public String getString(String fieldName) {
        return (String) fields.get(fieldName);
    }

    // Method to get a Boolean value
    public Boolean getBoolean(String fieldName) {
        return (Boolean) fields.get(fieldName);
    }

    public Long getLong(String fieldName) {
        return (Long) fields.get(fieldName);
    }

    // Save the record (create or update)
    public Record save() {
        if (creationMode) {
            insert();
        } else {
            update();
        }
        return this;
    }

    // Insert a new record into the database
    private void insert() {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder values = new StringBuilder(" VALUES (");
        for (String field : fields.keySet()) {
            sql.append(field).append(", ");
            values.append("?, ");
        }
        sql.setLength(sql.length() - 2); // Remove last comma and space
        values.setLength(values.length() - 2); // Remove last comma and space
        sql.append(")").append(values).append(")");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString(), PreparedStatement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            for (Object value : fields.values()) {
                pstmt.setObject(index++, value);
            }
            pstmt.executeUpdate();
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                this.id = generatedKeys.getInt(1); // Get generated ID
                System.out.println("[DatabaseEngine] Successfully inserted record with ID: " + this.id);
            }
            this.creationMode = false; // Set to false after creation
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to insert record: " + e.getMessage());
        }
    }

    // Update an existing record in the database
    private void update() {
        if (id == null) {
            throw new IllegalStateException("Cannot update record: ID is null.");
        }

        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        for (String field : fields.keySet()) {
            sql.append(field).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2); // Remove last comma and space
        sql.append(" WHERE ID = ?");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            int index = 1;
            for (Object value : fields.values()) {
                pstmt.setObject(index++, value);
            }
            pstmt.setInt(index, id); // Set the ID for the WHERE clause
            pstmt.executeUpdate();
            System.out.println("[DatabaseEngine] Successfully updated record with ID: " + id);
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to update record: " + e.getMessage());
        }
    }

    // Destroy the record
    public void destroy() {
        if (id == null) {
            throw new IllegalStateException("Cannot destroy record: ID is null.");
        }

        String sql = "DELETE FROM " + tableName + " WHERE ID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("[DatabaseEngine] Successfully deleted record with ID: " + id);
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to delete record: " + e.getMessage());
        }
    }
}
