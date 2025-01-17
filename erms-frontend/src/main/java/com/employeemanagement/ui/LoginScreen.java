package com.employeemanagement.ui;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CompletableFuture;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    private static final String API_URL = "http://localhost:8080/api/auth/login"; // Adjust to your backend URL
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public LoginScreen() {
        super("Employee Management System - Login");

        // Initialize HTTP client and JSON mapper
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        setupUI();
        setupListeners();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void setupUI() {
        // Main panel with MigLayout
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]"));

        // Login form panel
        JPanel formPanel = new JPanel(new MigLayout("wrap 2, fillx, insets 20", "[][grow,fill]"));
        formPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        // Username field
        formPanel.add(new JLabel("Username:"), "right");
        usernameField = new JTextField(20);
        formPanel.add(usernameField);

        // Password field
        formPanel.add(new JLabel("Password:"), "right");
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField);

        // Login button
        loginButton = new JButton("Login");
        formPanel.add(loginButton, "span 2, align center");

        // Status label for messages
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, "span 2, align center");

        // Add form panel to main panel
        mainPanel.add(formPanel, "align center, center");

        // Add main panel to frame
        add(mainPanel);

        // Set minimum size
        setMinimumSize(new Dimension(400, 300));
    }

    private void setupListeners() {
        loginButton.addActionListener(e -> handleLogin());

        // Handle Enter key in password field
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLACK);

        // Create login request body
        LoginRequest loginRequest = new LoginRequest(username, password);

        try {
            String jsonRequest = objectMapper.writeValueAsString(loginRequest);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            // Make async HTTP request
            CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(
                    request, HttpResponse.BodyHandlers.ofString());

            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    handleLoginResponse(response);
                });
            }).exceptionally(ex -> {
                SwingUtilities.invokeLater(() -> {
                    handleLoginError(ex);
                });
                return null;
            });

        } catch (Exception ex) {
            handleLoginError(ex);
        }
    }

    private void handleLoginResponse(HttpResponse<String> response) {
        loginButton.setEnabled(true);

        try {
            if (response.statusCode() == 200) {
                LoginResponse loginResponse = objectMapper.readValue(
                        response.body(), LoginResponse.class);

                // TODO: Store the JWT token from loginResponse
                proceedToMainApplication(loginResponse.getUser());

            } else {
                String errorMsg = response.statusCode() == 401 ?
                        "Invalid credentials" :
                        "Login failed: " + response.body();
                showError(errorMsg);
            }
        } catch (Exception ex) {
            handleLoginError(ex);
        }
    }

    private void handleLoginError(Throwable ex) {
        loginButton.setEnabled(true);
        showError("Login failed: " + ex.getMessage());
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setForeground(Color.RED);
    }

    private void proceedToMainApplication(User user) {
        dispose(); // Close login window

        // Show main application window based on user role
        SwingUtilities.invokeLater(() -> {
            // TODO: Initialize main application window with user role
            JOptionPane.showMessageDialog(null,
                    "Login successful! User role: " + user.getRole());
        });
    }
}

// Data transfer objects
class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters required for JSON serialization
    public String getUsername() { return username; }
    public String getPassword() { return password; }
}

class LoginResponse {
    private String token;
    private User user;

    // Getters and setters required for JSON deserialization
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}

// Main method for testing
