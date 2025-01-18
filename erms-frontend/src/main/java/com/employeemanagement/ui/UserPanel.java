package com.employeemanagement.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import net.miginfocom.swing.MigLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class UserPanel extends JPanel {
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JButton addUserButton;
    private JButton refreshButton;
    private JButton deleteUserButton;
    private final User currentUser;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String API_BASE_URL = "http://localhost:8080/api/v1/users";

    public UserPanel(User user) {
        this.currentUser = user;
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        setLayout(new MigLayout("fill", "[grow]", "[grow]"));

        initializeComponents();
        setupTable();
        loadUsers();
    }

    private void initializeComponents() {
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new MigLayout("", "[][][][grow]"));

        addUserButton = new JButton("Add User");
        refreshButton = new JButton("Refresh");
        deleteUserButton = new JButton("Delete User");

        buttonsPanel.add(addUserButton, "");
        buttonsPanel.add(refreshButton, "");
        buttonsPanel.add(deleteUserButton, "wrap");

        // Setup button listeners
        refreshButton.addActionListener(e -> loadUsers());
        addUserButton.addActionListener(e -> showAddUserDialog());
        deleteUserButton.addActionListener(e -> deleteSelectedUser());

        add(buttonsPanel, "growx, wrap");
    }

    private void setupTable() {
        // Define column names
        String[] columnNames = {
                "ID", "Username", "Email", "Role",
                "Department", "Created At", "Last Login"
        };

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table with the model
        usersTable = new JTable(tableModel);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showUserDetailsDialog();
                }
            }
        });

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(usersTable);
        add(scrollPane, "grow");
    }

    private void loadUsers() {
        // Get base64-encoded credentials
        String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
        String encodedCredentials = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Clear existing rows
                tableModel.setRowCount(0);

                // Parse JSON response
                List<User> users = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<User>>(){}
                );

                for (User user : users) {
                    tableModel.addRow(new Object[]{
                            user.getUserId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole(),
                            user.getDepartment() != null ? user.getDepartment().getDeptName() : "N/A",
                            user.getCreatedAt(),
                            user.getLastLogin()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load users: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading users: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showAddUserDialog() {
        UserDialog dialog = new UserDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                currentUser
        );
        dialog.setVisible(true);

        if (dialog.isSuccessful()) {
            loadUsers(); // Refresh the list
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a user to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get the user ID from the selected row
        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this user?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Get base64-encoded credentials
                String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
                String encodedCredentials = Base64.getEncoder().encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/" + userId))
                        .header("Authorization", "Basic " + encodedCredentials)
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(
                            this,
                            "User deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadUsers(); // Refresh the list
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to delete user: " + response.body(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error deleting user: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void showUserDetailsDialog() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        // Get the user ID from the selected row
        Long userId = (Long) tableModel.getValueAt(selectedRow, 0);

        try {
            // Get base64-encoded credentials
            String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

            // Create request to fetch user details
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + userId))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse user details
                User user = objectMapper.readValue(response.body(), User.class);

                // Create and show details dialog
                UserDetailsDialog dialog = new UserDetailsDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        user,
                        true,
                        currentUser
                );
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to fetch user details: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error fetching user details: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}