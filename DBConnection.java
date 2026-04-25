import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // This URL points exactly to the database you just made in Workbench
    private static final String URL = "jdbc:mysql://localhost:3306/BankingSystem";
    private static final String USER = "root"; 
    private static final String PASSWORD = "#justhappy123"; // Leave blank unless you set a MySQL password

    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
        return conn;
    }

    // A small test to see if it works!
    public static void main(String[] args) {
        getConnection();
    }
}