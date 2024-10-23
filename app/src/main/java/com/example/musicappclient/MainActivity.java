package com.example.musicappclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private static final String SERVER_IP = "10.24.14.86"; // Thay bằng IP của bạn
    private static final int SERVER_PORT = 12346;
    private EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String username = getIntent().getStringExtra("username");
        TextView helloTextView = findViewById(R.id.helloTextView);
        helloTextView.setText("Hello: " + username);

        searchEditText = findViewById(R.id.searchEditText);
        Button searchButton = findViewById(R.id.searchButton);
        Button prevButton = findViewById(R.id.prevButton);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button nextButton = findViewById(R.id.nextButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        // Xử lý tìm kiếm podcast
        searchButton.setOnClickListener(v -> new Thread(this::handleSearch).start());

        // Xử lý các nút điều khiển phát nhạc
        prevButton.setOnClickListener(v -> new Thread(() -> sendCommandToServer("PREV")).start());
        pauseButton.setOnClickListener(v -> new Thread(() -> sendCommandToServer("PAUSE")).start());
        nextButton.setOnClickListener(v -> new Thread(() -> sendCommandToServer("NEXT")).start());

        // Chuyển sang trang thêm podcast khi nhấn nút Upload
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPodcastActivity.class);
            startActivity(intent);
        });
    }

    private void handleSearch() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String keyword = searchEditText.getText().toString();
            out.println("SEARCH");
            out.println(keyword);

            String response;
            StringBuilder result = new StringBuilder();
            while ((response = in.readLine()) != null && !response.equals("")) {
                result.append(response).append("\n");
            }

            String searchResults = result.length() > 0 ? result.toString() : "No results found";
            runOnUiThread(() -> Toast.makeText(MainActivity.this, searchResults, Toast.LENGTH_LONG).show());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCommandToServer(String command) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(command);
            String response = in.readLine();
            runOnUiThread(() -> Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
