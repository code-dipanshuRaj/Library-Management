package library_management.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DBConnector {
    private static String url;
    private static String user;
    private static String passcode;

    // Static block to load config once when class is loaded
    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("../config.properties")) {
            props.load(fis);
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            passcode = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Failed to load config file: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // JDBC driver
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(url, user, passcode);
    }
}
