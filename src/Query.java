package com.mogukun.databaseengine.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Query {

    private String tableName;
    private Connection connection;
    private StringBuilder whereClause;
    private List<Object> parameters; // To hold parameters for prepared statement

    public Query(String tableName, Connection connection) {
        this.tableName = sanitizeTableName(tableName); // Sanitize table name
        this.connection = connection;
        this.whereClause = new StringBuilder();
        this.parameters = new ArrayList<>();
    }

    // Sanitize table name
    private String sanitizeTableName(String tableName) {
        if (!tableName.matches("[a-zA-Z_]+")) {
            throw new IllegalArgumentException("Invalid table name.");
        }
        return tableName;
    }

    // Add a condition to the query
    public Query ifCondition(String field, String value) {
        return ifCondition(field, "=", value); // Default to equal condition
    }

    // Add a condition with custom operator (e.g., <, >, !=, LIKE)
    public Query ifCondition(String field, String operator, Object value) {
        if (whereClause.length() == 0) {
            whereClause.append(" WHERE ");
        } else {
            whereClause.append(" AND ");
        }
        whereClause.append(field).append(" ").append(operator).append(" ?");
        parameters.add(value);
        return this;
    }

    // Execute the query to fetch the first record
    public Record first() {
        String sql = "SELECT * FROM " + tableName + whereClause.toString() + " LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to fetch the first record: " + e.getMessage());
        }
        return null; // No record found
    }

    // Execute the query to fetch all records
    public List<Record> all() {
        List<Record> records = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + whereClause.toString();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                records.add(mapRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to fetch all records: " + e.getMessage());
        }
        return records;
    }

    // Execute the query and get the size of the result set
    public int size() {
        String sql = "SELECT COUNT(*) FROM " + tableName + whereClause.toString();
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseEngine] Failed to get size of records: " + e.getMessage());
        }
        return 0;
    }

    // Set parameters for the prepared statement
    private void setParameters(PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            pstmt.setObject(i + 1, parameters.get(i));
        }
    }

    // Map the ResultSet to a Record
    private Record mapRecord(ResultSet rs) throws SQLException {
        Record record = new Record(tableName, connection);
        int columnCount = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rs.getMetaData().getColumnName(i);
            Object value = rs.getObject(i);
            record.set(columnName, value);
        }
        return record;
    }
}
