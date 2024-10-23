package com.example.musicappclient;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class RegisterActivity extends AppCompatActivity {
    private EditText regUsername, regPassword, regEmail, regDob, regGender;
    private static final String SERVER_IP = "10.24.14.86"; // Replace with correct IP
    private static final int SERVER_PORT = 12346;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regEmail = findViewById(R.id.regEmail);
        regDob = findViewById(R.id.regDob);
        regGender = findViewById(R.id.regGender);
        Button registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> new Thread(this::register).start());
    }

    private void register() {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send registration data to the server
            out.println("REGISTER");
            out.println(regUsername.getText().toString());
            out.println(regPassword.getText().toString());
            out.println(regEmail.getText().toString());
            out.println(regDob.getText().toString());
            out.println(regGender.getText().toString());

            String response = in.readLine();

            // Handle server response
            if ("REGISTER_SUCCESS".equals(response)) {
                runOnUiThread(() -> {
                    Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();  // Close RegisterActivity to prevent back navigation
                });
            } else {
                runOnUiThread(() ->
                        Toast.makeText(RegisterActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() ->
                    Toast.makeText(RegisterActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }
}
