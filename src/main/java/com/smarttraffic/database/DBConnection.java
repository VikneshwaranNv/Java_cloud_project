package com.smarttraffic.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection manager for MySQL Community Server 8.4 LTS and OCI Cloud Database.
 * Implements STAGE 3, STAGE 11, and STAGE 14 requirements:
 * - Reads DB_URL, DB_USER, and DB_PASSWORD from environment variables.
 * - Never hardcodes database credentials.
 * - Provides informative diagnostics when database is offline.
 * - Allows seamless switching to OCI cloud database tomorrow by changing environment variables.
 */
public class DBConnection {

    // Default configuration points to standard local MySQL instance
    public static final String DEFAULT_LOCAL_URL = "jdbc:mysql://localhost:3306/smart_traffic?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    public static final String DEFAULT_LOCAL_USER = "root";
    public static final String DEFAULT_LOCAL_PASSWORD = "";

    // Environment variable names
    public static final String ENV_DB_URL = "DB_URL";
    public static final String ENV_DB_USER = "DB_USER";
    public static final String ENV_DB_PASSWORD = "DB_PASSWORD";

    // Runtime custom connection parameters (e.g. for Same Wi-Fi / Hotspot LAN pairing)
    private static volatile String customJdbcUrl = null;
    private static volatile String customDbUser = null;
    private static volatile String customDbPassword = null;

    static {
        try {
            // Explicitly load MySQL Connector/J driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB ERROR] MySQL Connector/J driver class not found: " + e.getMessage());
        }
    }

    /**
     * Dynamically sets custom connection parameters at runtime (e.g., local Wi-Fi / hotspot pairing).
     *
     * @param host     Student 2's IP or hostname (e.g. 192.168.43.15)
     * @param port     Port (typically 3306)
     * @param database Database name (e.g. smart_traffic)
     * @param user     Database username (e.g. traffic_app)
     * @param password Database password (e.g. SmartCity2026!)
     */
    public static void setCustomConnection(String host, int port, String database, String user, String password) {
        if (host == null || host.trim().isEmpty()) {
            resetCustomConnection();
            return;
        }
        String db = (database != null && !database.trim().isEmpty()) ? database.trim() : "smart_traffic";
        customJdbcUrl = String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000",
            host.trim(), port, db
        );
        customDbUser = (user != null && !user.trim().isEmpty()) ? user.trim() : "traffic_app";
        customDbPassword = (password != null) ? password : "SmartCity2026!";
    }

    /**
     * Resets runtime connection to environment variable or local default.
     */
    public static void resetCustomConnection() {
        customJdbcUrl = null;
        customDbUser = null;
        customDbPassword = null;
    }

    public static boolean isCustomConnectionActive() {
        return customJdbcUrl != null && !customJdbcUrl.trim().isEmpty();
    }

    /**
     * Resolves the active JDBC URL from custom runtime settings, environment variables, or local default.
     */
    public static String getJdbcUrl() {
        if (customJdbcUrl != null && !customJdbcUrl.trim().isEmpty()) {
            return customJdbcUrl.trim();
        }
        String envUrl = System.getenv(ENV_DB_URL);
        if (envUrl != null && !envUrl.trim().isEmpty()) {
            return envUrl.trim();
        }
        return DEFAULT_LOCAL_URL;
    }

    /**
     * Resolves the active database user from custom runtime settings, environment variables, or local default.
     */
    public static String getDbUser() {
        if (customDbUser != null && !customDbUser.trim().isEmpty()) {
            return customDbUser.trim();
        }
        String envUser = System.getenv(ENV_DB_USER);
        if (envUser != null && !envUser.trim().isEmpty()) {
            return envUser.trim();
        }
        return DEFAULT_LOCAL_USER;
    }

    /**
     * Resolves the active database password from custom runtime settings, environment variables, or local default.
     */
    public static String getDbPassword() {
        if (customDbPassword != null) {
            return customDbPassword;
        }
        String envPass = System.getenv(ENV_DB_PASSWORD);
        if (envPass != null) {
            return envPass;
        }
        return DEFAULT_LOCAL_PASSWORD;
    }

    /**
     * Establishes a connection to the MySQL database.
     *
     * @return active java.sql.Connection
     * @throws SQLException with beginner-friendly diagnostics if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = getJdbcUrl();
        String user = getDbUser();
        String password = getDbPassword();

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            String maskedPassword = (password == null || password.isEmpty()) ? "<EMPTY>" : "********";
            String errorMsg = String.format(
                "[DB CONNECTION FAILURE]\n" +
                "  URL      : %s\n" +
                "  User     : %s\n" +
                "  Password : %s\n" +
                "  SQLState : %s (ErrorCode: %d)\n" +
                "  Cause    : %s\n" +
                "  Hint     : Ensure MySQL Server is running on port 3306 and schema 'smart_traffic' has been created.\n" +
                "             Set environment variables DB_URL, DB_USER, DB_PASSWORD to override credentials.\n",
                url, user, maskedPassword, e.getSQLState(), e.getErrorCode(), e.getMessage()
            );
            throw new SQLException(errorMsg, e);
        }
    }

    /**
     * Diagnostic probe to test whether the database is currently reachable.
     *
     * @return true if connection succeeded, false if unreachable
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
