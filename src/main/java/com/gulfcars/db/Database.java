package com.gulfcars.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    private static String url;
    private static String user;
    private static String password;

    static {
        try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
            Properties p = new Properties();
            if (in != null) p.load(in);
            url = p.getProperty("db.url", "jdbc:mysql://localhost:3306/gulfcars");
            user = p.getProperty("db.user", "root");
            password = p.getProperty("db.password", "");
            try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException ignore) {}
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
