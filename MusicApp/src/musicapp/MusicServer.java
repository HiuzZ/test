package musicapp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MusicServer {
    private static final int PORT = 12346; // Cổng cho server
    private static Connection connection;  // Kết nối tới MySQL Database

    // Kết nối với MySQL Database
public static void main(String[] args) {
    try {
        // Kết nối với MySQL Database
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3307/podcast_db", "root", "17062003");
        System.out.println("Server is running...");

        // Test the connection
        testDatabaseConnection();

        // Tạo socket server lắng nghe các kết nối từ client
        ServerSocket serverSocket = new ServerSocket(PORT);

        // Lắng nghe liên tục cho các client kết nối
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected!");
            // Tạo luồng xử lý client riêng biệt
            new ClientHandler(clientSocket, connection).start();
        }
    } catch (IOException | SQLException e) {
        e.printStackTrace();
    } finally {
        closeConnection(); // Đóng kết nối khi kết thúc server
    }
}

// Phương thức kiểm tra kết nối đến database
private static void testDatabaseConnection() {
    try (Statement stmt = connection.createStatement()) {
        ResultSet rs = stmt.executeQuery("SELECT CURRENT_DATE();"); // Query đơn giản
        if (rs.next()) {
            System.out.println("Database connection successful. Current date: " + rs.getDate(1));
        }
    } catch (SQLException e) {
        System.out.println("Failed to execute test query: " + e.getMessage());
    }
}


    // Đóng kết nối với MySQL Database
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}