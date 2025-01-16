package com.employeemanagement.ui.screens;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginScreen() {
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
    }

    private void initializeComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");

        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(51, 153, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new MigLayout("fill, insets 20", "[grow]", "[]20[]20[]20[]"));

        JLabel logoLabel = new JLabel("Login");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(logoLabel, "cell 0 0, growx, center");

        JPanel inputPanel = new JPanel(new MigLayout("fillx", "[][grow]", "[]10[]"));
        inputPanel.add(new JLabel("Username:"), "cell 0 0");
        inputPanel.add(usernameField, "cell 1 0, growx");
        inputPanel.add(new JLabel("Password:"), "cell 0 1");
        inputPanel.add(passwordField, "cell 1 1, growx");

        mainPanel.add(inputPanel, "cell 0 1, growx");
        mainPanel.add(loginButton, "cell 0 2, growx");

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> handleLogin());

        passwordField.addActionListener(e -> handleLogin());
    }

    private void setupFrame() {
        setTitle("Employee Record Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setMinimumSize(new Dimension(350, 250));
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (validateInput(username, password)) {
            // TODO: Implement actual authentication
            if (authenticateUser(username, password)) {
                showMainScreen();
            } else {
                showErrorMessage("Invalid username or password");
            }
        }
    }

    private boolean validateInput(String username, String password) {
        if (username.trim().isEmpty()) {
            showErrorMessage("Please enter username");
            usernameField.requestFocus();
            return false;
        }

        if (password.trim().isEmpty()) {
            showErrorMessage("Please enter password");
            passwordField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean authenticateUser(String username, String password) {
        // TODO: Implement actual authentication logic
        // For testing purposes:
        return "admin".equals(username) && "admin".equals(password);
    }

    private void showMainScreen() {
        MainScreen mainScreen = new MainScreen(usernameField.getText());
        mainScreen.setVisible(true);
        this.dispose(); // Close login window
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}