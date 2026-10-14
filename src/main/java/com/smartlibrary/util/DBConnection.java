package com.smartlibrary.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central place that opens JDBC connections to the smart_library MySQL
 * database. Credentials are read once from db.properties on the
 * classpath (WEB-INF/classes/db.properties after packaging) so they
 * never appear in a JSP, servlet, or JavaScript file.
 */
public final class DBConnection {

    private static final Properties PROPS = new Properties();
    private static volatile boolean driverLoaded = false;

    static {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new ExceptionInInitializerError(
                        "db.properties not found on classpath. Make sure it is under src/main/resources.");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load db.properties: " + e.getMessage());
        }
    }

    private DBConnection() {
        // utility class - no instances
    }

    private static void loadDriverOnce() throws SQLException {
        if (!driverLoaded) {
            synchronized (DBConnection.class) {
                if (!driverLoaded) {
                    try {
                        Class.forName(PROPS.getProperty("db.driver"));
                        driverLoaded = true;
                    } catch (ClassNotFoundException e) {
                        throw new SQLException("MySQL JDBC driver not found on classpath.", e);
                    }
                }
            }
        }
    }

    /**
     * Opens a brand-new JDBC connection. Callers are responsible for
     * closing it — always use try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        loadDriverOnce();
        String url = PROPS.getProperty("db.url");
        String user = PROPS.getProperty("db.username");
        String pass = PROPS.getProperty("db.password");
        return DriverManager.getConnection(url, user, pass);
    }
}
