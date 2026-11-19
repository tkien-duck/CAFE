package do_an;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Không chứa integratedSecurity=true, dùng tài khoản sa và mật khẩu 123456
    private static final String DB_URL = "jdbc:sqlserver://DESKTOP-6G70VA7\\SQLEXPRESS;databaseName=QuanLyQuanCafe;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASS = "123456";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy JDBC Driver cho SQL Server!", e);
        }
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}