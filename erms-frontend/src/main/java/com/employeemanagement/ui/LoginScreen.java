package com.employeemanagement.ui;

import javax.swing.*;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.miginfocom.swing.MigLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    private static final String API_URL = "http://localhost:8080/api/v1/users/login";
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
        System.out.println("Login attempt started");

        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        System.out.println("Username entered: " + username);
        // Don't log passwords in production!

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            System.out.println("Login failed: Empty credentials");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setText("Logging in...");
        statusLabel.setForeground(Color.BLACK);

        try {
            // Create login request body
            LoginRequest loginRequest = new LoginRequest(username, password);
            String jsonRequest = objectMapper.writeValueAsString(loginRequest);
            System.out.println("Request JSON created: " + jsonRequest);

            System.out.println("Attempting to connect to: " + API_URL);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                    .build();

            // Make HTTP request
            try {
                System.out.println("Sending HTTP request...");
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response received. Status: " + response.statusCode());
                System.out.println("Response body: " + response.body());
                handleLoginResponse(response);
            } catch (Exception e) {
                System.err.println("HTTP request failed: " + e.getMessage());
                e.printStackTrace();
                handleLoginError(e);
            }

        } catch (Exception ex) {
            System.err.println("Error preparing request: " + ex.getMessage());
            ex.printStackTrace();
            handleLoginError(ex);
        } finally {
            loginButton.setEnabled(true);
        }
    }

    private void handleLoginResponse(HttpResponse<String> response) {
        loginButton.setEnabled(true);

        try {
            if (response.statusCode() == 200) {
                User user = objectMapper.readValue(response.body(), User.class);
                user.setPassword(new String(passwordField.getPassword()));
                proceedToMainApplication(user);


            } else {
                String errorMsg = response.statusCode() == 401 ?
                        "Invalid credentials" :
                        "Login failed: "  + response.body();
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

        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow(user);
                mainWindow.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error opening main window: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                // If error occurs, show login screen again
                new LoginScreen().setVisible(true);
            }
        });
    }
}
@Setter
@Getter
@Data
class User {
    // Getters and setters
    private Long userId;
    private String username;
    private String email;
    private UserRole role;
    private Department department;
    private String createdAt;
    private String lastLogin;
    private String password;
}

enum UserRole {
    ADMIN,
    HR,
    MANAGER,
    EMPLOYEE
}
@Setter
@Getter
@Data
class Department {
    private Long deptId;
    private String deptName;
    private LocalDateTime createdAt;

}

