package musicapp;

import java.io.*;
import java.net.Socket;
import java.sql.*;

class ClientHandler extends Thread {
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private Connection connection; // Kết nối với MySQL Database

    // Constructor truyền vào Socket và Connection từ server
    public ClientHandler(Socket socket, Connection connection) {
        this.clientSocket = socket;
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request;
            while ((request = in.readLine()) != null) {
                switch (request) {
                    case "LOGIN":
                        handleLogin();
                        break;
                    case "REGISTER":
                        handleRegister();
                        break;
                    case "SEARCH":
                        handleSearch();
                        break;
                    case "NEXT":
                        out.println("Chuyển sang bài tiếp theo...");
                        break;
                    case "PREV":
                        out.println("Chuyển sang bài trước đó...");
                        break;
                    case "PAUSE":
                        out.println("Đã tạm dừng phát.");
                        break;
                    case "UPLOAD":
                        handleUpload();
                        break;
                    default:
                        out.println("Yêu cầu không hợp lệ.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    // Đóng các stream khi không còn sử dụng
    private void closeResources() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            System.out.println("Client disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Xử lý đăng nhập
    private void handleLogin() throws IOException {
        try {
            String username = in.readLine();
            String password = in.readLine();

            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ? AND password = ?");
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                out.println("SUCCESS");
            } else {
                out.println("FAILED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    // Xử lý đăng ký tài khoản
    private void handleRegister() throws IOException {
        try {
            String username = in.readLine();
            String password = in.readLine();
            String email = in.readLine();
            String dob = in.readLine(); // Ngày sinh
            String gender = in.readLine(); // Giới tính

            PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (username, password, email, dob, gender) VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, email);
            stmt.setString(4, dob);
            stmt.setString(5, gender);

            stmt.executeUpdate();
            out.println("REGISTER_SUCCESS");
        } catch (SQLException e) {
            out.println("REGISTER_FAILED");
            e.printStackTrace();
        }
    }

    // Xử lý tìm kiếm podcast
    private void handleSearch() throws IOException {
        try {
            String keyword = in.readLine();

            PreparedStatement stmt = connection.prepareStatement(
                "SELECT * FROM podcasts WHERE title LIKE ?");
            stmt.setString(1, "%" + keyword + "%");

            ResultSet rs = stmt.executeQuery();
            StringBuilder result = new StringBuilder();

            while (rs.next()) {
                result.append(rs.getString("title")).append("\n");
            }

            out.println(result.length() > 0 ? result.toString() : "NO_RESULTS");
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR");
        }
    }

    // Server side: xử lý upload file âm thanh
    private void handleUpload() throws IOException {
    try {
        String title = in.readLine();
        String author = in.readLine();
        String fileName = in.readLine();  // Tên file từ client

        // Tạo đường dẫn lưu file trên server
        String filePath = "uploads/" + fileName;
        FileOutputStream fos = new FileOutputStream(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        // Nhận file từ client và ghi vào hệ thống file
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = clientSocket.getInputStream().read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        bos.close();

        // Lưu thông tin podcast và đường dẫn file vào database
        PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO podcasts (title, author, file_path) VALUES (?, ?, ?)");
        stmt.setString(1, title);
        stmt.setString(2, author);
        stmt.setString(3, filePath);  // Lưu đường dẫn file vào DB
        stmt.executeUpdate();

        out.println("UPLOAD_SUCCESS");
    } catch (SQLException e) {
        out.println("UPLOAD_FAILED");
        e.printStackTrace();
    }
}

}