package com.example.shelldemo.cli;

import java.sql.*;
import java.util.*;

/**
 * A unified database operation class that provides a simple interface for database operations
 * across different database types. This class handles connection management and provides
 * common database operations.
 */
public class UnifiedDatabaseOperation implements AutoCloseable {
    private final String dbType;
    private final ConnectionConfig config;
    private final String url;
    private final Properties connectionProperties;
    private Connection connection;

    private UnifiedDatabaseOperation(String dbType, ConnectionConfig config) {
        this.dbType = dbType.trim().toLowerCase();
        this.config = Objects.requireNonNull(config, "Connection config cannot be null");
        this.connectionProperties = new Properties();
        this.connectionProperties.setProperty("user", config.getUsername());
        this.connectionProperties.setProperty("password", config.getPassword());
        this.url = String.format(getUrlFormat(this.dbType), 
            config.getHost(), 
            config.getPort(), 
            config.getDatabase());
    }

    /**
     * Creates a new UnifiedDatabaseOperation instance for the specified database type and configuration.
     *
     * @param dbType the database type (oracle, postgresql, mysql, sqlserver)
     * @param config the connection configuration
     * @return a new UnifiedDatabaseOperation instance
     * @throws SQLException if the database type is invalid or connection fails
     */
    public static UnifiedDatabaseOperation create(String dbType, ConnectionConfig config) throws SQLException {
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new SQLException("Database type cannot be null or empty");
        }
        return new UnifiedDatabaseOperation(dbType, config);
    }

    private static String getUrlFormat(String dbType) {
        return switch (dbType) {
            case "oracle" -> "jdbc:oracle:thin:@//%s:%d/freepdb1?SERVICE_NAME=freepdb1";
            case "postgresql" -> "jdbc:postgresql://%s:%d/%s";
            case "mysql" -> "jdbc:mysql://%s:%d/%s";
            case "sqlserver" -> "jdbc:sqlserver://%s:%d;databaseName=%s";
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbType);
        };
    }

    /**
     * Gets a connection to the database. If a connection already exists, it is returned.
     * Otherwise, a new connection is created.
     *
     * @return a database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, connectionProperties);
        }
        return connection;
    }

    /**
     * Executes a query and returns the results as a list of maps.
     *
     * @param sql the SQL query to execute
     * @param params the query parameters
     * @return a list of maps containing the query results
     * @throws SQLException if a database access error occurs
     */
    public List<Map<String, Object>> executeQuery(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<Map<String, Object>> results = new ArrayList<>();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    results.add(row);
                }
                return results;
            }
        }
    }

    /**
     * Executes an update statement (INSERT, UPDATE, DELETE).
     *
     * @param sql the SQL statement to execute
     * @param params the statement parameters
     * @return the number of rows affected
     * @throws SQLException if a database access error occurs
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        }
    }

    /**
     * Calls a stored procedure and returns the first output parameter.
     *
     * @param procedureName the name of the stored procedure
     * @param params the procedure parameters
     * @return the first output parameter value
     * @throws SQLException if a database access error occurs
     */
    public Object callStoredProcedure(String procedureName, Object... params) throws SQLException {
        try (CallableStatement stmt = getConnection().prepareCall(procedureName)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.execute();
            return stmt.getObject(1);
        }
    }

    /**
     * Executes a transaction with the provided callback.
     *
     * @param callback the transaction callback
     * @param <T> the return type
     * @return the result of the transaction
     * @throws SQLException if a database access error occurs
     */
    public <T> T executeTransaction(ConnectionCallback<T> callback) throws SQLException {
        Connection conn = getConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        try {
            conn.setAutoCommit(false);
            T result = callback.execute(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(originalAutoCommit);
        }
    }

    /**
     * Gets the database type.
     *
     * @return the database type
     */
    public String getDatabaseType() {
        return dbType;
    }

    /**
     * Gets the connection configuration.
     *
     * @return the connection configuration
     */
    public ConnectionConfig getConnectionConfig() {
        return config;
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Functional interface for database operations that require a connection.
     *
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface ConnectionCallback<T> {
        /**
         * Executes the operation with the given connection.
         *
         * @param connection the database connection
         * @return the result of the operation
         * @throws SQLException if a database access error occurs
         */
        T execute(Connection connection) throws SQLException;
    }
} 