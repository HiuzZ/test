package com.example.musicappclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class AddPodcastActivity extends AppCompatActivity {

    private static final int PICK_AUDIO_REQUEST = 1;
    private static final String SERVER_IP = "10.24.14.86"; // IP của server
    private static final int SERVER_PORT = 12346;

    private EditText titleEditText, authorEditText;
    private Uri audioFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_podcast);

        titleEditText = findViewById(R.id.uploadTitleEditText);
        authorEditText = findViewById(R.id.uploadAuthorEditText);
        Button selectFileButton = findViewById(R.id.selectFileButton);
        Button uploadButton = findViewById(R.id.uploadButton);

        selectFileButton.setOnClickListener(v -> openFileChooser());
        uploadButton.setOnClickListener(v -> new Thread(this::handleUpload).start());
    }

    // Chọn file âm thanh từ bộ nhớ điện thoại
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioFileUri = data.getData();
        }
    }

    // Upload podcast lên server
    private void handleUpload() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String title = titleEditText.getText().toString();
            String author = authorEditText.getText().toString();

            // Gửi metadata
            out.println("UPLOAD");
            out.println(title);
            out.println(author);

            // Sau khi upload thành công
            runOnUiThread(() -> Toast.makeText(AddPodcastActivity.this, "Upload thành công!", Toast.LENGTH_SHORT).show());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
