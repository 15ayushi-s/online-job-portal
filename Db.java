package com.jobhub.util;

import com.jobhub.exception.DataAccessException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Central place that opens JDBC connections. Settings: db.properties, overridable by env vars. */
public final class Db {
    private static final Properties PROPS = load();

    private Db() {}

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = Db.class.getResourceAsStream("/db.properties")) {
            if (in != null) p.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read db.properties", e);
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver is not on the classpath", e);
        }
        return p;
    }

    private static String setting(String env, String key) {
        String v = System.getenv(env);
        return (v != null && !v.isBlank()) ? v : PROPS.getProperty(key);
    }

    /** Callers must close the connection (use try-with-resources). */
    public static Connection get() {
        try {
            return DriverManager.getConnection(
                setting("DB_URL", "db.url"), setting("DB_USER", "db.user"), setting("DB_PASSWORD", "db.password"));
        } catch (SQLException e) {
            throw new DataAccessException("Cannot connect to MySQL (check db.properties / DB_PASSWORD): " + e.getMessage(), e);
        }
    }

    /** MySQL error 1062 = duplicate entry for a UNIQUE key. */
    public static boolean isDuplicate(SQLException e) {
        return e.getErrorCode() == 1062;
    }
}
