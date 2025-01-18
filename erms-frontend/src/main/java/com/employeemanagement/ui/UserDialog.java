package com.employeemanagement.ui;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserDialog extends JDialog {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<UserRole> roleComboBox;
    private JComboBox<Department> departmentComboBox;
    private JCheckBox activeCheckBox;

    private User user;
    private boolean isNewUser;
    private boolean submitted = false;
    private final User currentUser;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String API_BASE_URL = "http://localhost:8080/api/v1/users";
    private static final String DEPARTMENTS_API_URL = "http://localhost:8080/api/v1/departments";

    public UserDialog(Window parent, User currentUser) {
        super(parent, "Add New User", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.user = new User();
        this.isNewUser = true;

        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        initComponents();
        setupLayout();
        loadDepartments();

        setSize(400, 500);
        setLocationRelativeTo(parent);
    }

    public UserDialog(User existingUser, User currentUser) {
        super((Window) null, "Edit User", ModalityType.APPLICATION_MODAL);
        this.currentUser = currentUser;
        this.user = existingUser;
        this.isNewUser = false;

        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        initComponents();
        setupLayout();
        loadDepartments();
        populateFields();

        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        emailField = new JTextField(20);
        passwordField = new JPasswordField(20);

        // Role selection
        roleComboBox = new JComboBox<>(UserRole.values());

        // Department selection
        departmentComboBox = new JComboBox<>();

        // Active status
        activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setSelected(true);
    }

    private void setupLayout() {
        setLayout(new MigLayout("wrap 2, gap 10", "[right][:200:]"));

        // Username
        add(new JLabel("Username:"));
        add(usernameField);

        // Email
        add(new JLabel("Email:"));
        add(emailField);

        // Password (only for new users)
        if (isNewUser) {
            add(new JLabel("Password:"));
            add(passwordField);
        }

        // Role
        add(new JLabel("Role:"));
        add(roleComboBox);

        // Department
        add(new JLabel("Department:"));
        add(departmentComboBox);

        // Active Status
        add(new JLabel("Status:"));
        add(activeCheckBox);

        // Buttons
        JButton saveButton = new JButton(isNewUser ? "Add User" : "Update User");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());

        add(saveButton, "span 2, split 2, align center");
        add(cancelButton);
    }

    private void loadDepartments() {
        try {
            // Get base64-encoded credentials
            String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DEPARTMENTS_API_URL))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Department[] departments = objectMapper.readValue(
                        response.body(),
                        Department[].class
                );

                // Populate department combo box
                departmentComboBox.removeAllItems();
                departmentComboBox.addItem(null); // Allow no department selection
                for (Department dept : departments) {
                    departmentComboBox.addItem(dept);
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load departments: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading departments: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void populateFields() {
        if (user != null) {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            roleComboBox.setSelectedItem(user.getRole());

            // Set department
            if (user.getDepartment() != null) {
                departmentComboBox.setSelectedItem(user.getDepartment());
            }
        }
    }

    private void saveUser() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Get base64-encoded credentials
            String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

            // Update user object
            user.setUsername(usernameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole((UserRole) roleComboBox.getSelectedItem());
            user.setDepartment((Department) departmentComboBox.getSelectedItem());

            // For new users, set password
            if (isNewUser) {
                user.setPassword(new String(passwordField.getPassword()));
            }

            // Convert to JSON
            String jsonUser = objectMapper.writeValueAsString(user);

            // Prepare HTTP request
            HttpRequest request;
            if (isNewUser) {
                // POST for new user
                request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL))
                        .header("Authorization", "Basic " + encodedCredentials)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonUser))
                        .build();
            } else {
                // PUT for existing user
                request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/" + user.getUserId()))
                        .header("Authorization", "Basic " + encodedCredentials)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(jsonUser))
                        .build();
            }

            // Send request
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Handle response
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                submitted = true;
                JOptionPane.showMessageDialog(
                        this,
                        isNewUser ? "User added successfully." : "User updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to " + (isNewUser ? "add" : "update") + " user: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error " + (isNewUser ? "adding" : "updating") + " user: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean validateInputs() {
        // Username validation
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Username cannot be empty.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.contains("@")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid email address.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        // Password validation (only for new users)
        if (isNewUser) {
            String password = new String(passwordField.getPassword());
            if (password.isEmpty() || password.length() < 6) {
                JOptionPane.showMessageDialog(
                        this,
                        "Password must be at least 6 characters long.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }
        }

        // Role validation
        if (roleComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user role.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return false;
        }

        return true;
    }

    public User getUser() {
        return user;
    }

    public boolean isSuccessful() {
        return submitted;
    }
}