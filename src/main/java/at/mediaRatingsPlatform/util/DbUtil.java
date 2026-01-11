package at.mediaRatingsPlatform.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbUtil {
    private static final String URL  = "jdbc:postgresql://localhost:5432/mrp";
    private static final String USER = "jess";
    private static final String PASS = "jess1password";

    private DbUtil() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
