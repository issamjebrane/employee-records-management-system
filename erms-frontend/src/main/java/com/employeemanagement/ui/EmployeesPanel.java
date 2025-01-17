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

public class EmployeesPanel extends JPanel {
    private JTable employeesTable;
    private DefaultTableModel tableModel;
    private JButton addEmployeeButton;
    private JButton refreshButton;
    private JButton deleteEmployeeButton;
    private final User currentUser;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private static final String API_BASE_URL = "http://localhost:8080/api/v1/employees";

    public EmployeesPanel(User user) {
        this.currentUser = user;
        httpClient = HttpClient.newHttpClient();
        objectMapper = new ObjectMapper();

        setLayout(new MigLayout("fill", "[grow]", "[grow]"));

        initializeComponents();
        setupTable();
        loadEmployees();
    }

    private void initializeComponents() {
        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new MigLayout("", "[][][][grow]"));

        addEmployeeButton = new JButton("Add Employee");
        refreshButton = new JButton("Refresh");
        deleteEmployeeButton = new JButton("Delete Employee");

        buttonsPanel.add(addEmployeeButton, "");
        buttonsPanel.add(refreshButton, "");
        buttonsPanel.add(deleteEmployeeButton, "wrap");

        // Setup button listeners
        refreshButton.addActionListener(e -> loadEmployees());
        addEmployeeButton.addActionListener(e -> showAddEmployeeDialog());
        deleteEmployeeButton.addActionListener(e -> deleteSelectedEmployee());

        add(buttonsPanel, "growx, wrap");
    }

    private void setupTable() {
        // Define column names
        String[] columnNames = {
                "ID", "Name", "Email", "Job Title",
                "Department", "Hire Date", "Status"
        };

        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Create table with the model
        employeesTable = new JTable(tableModel);
        employeesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showEmployeeDetailsDialog();
                }
            }
        });
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(employeesTable);
        add(scrollPane, "grow");
    }

    private void loadEmployees() {
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
                EmployeePageResponse employeeResponse = objectMapper.readValue(
                        response.body(),
                        EmployeePageResponse.class
                );

                for (Employee employee : employeeResponse.getEmployees()) {
                    tableModel.addRow(new Object[]{
                            employee.getEmpId(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            employee.getEmail(),
                            employee.getJobTitle(),
                            employee.getDepartmentName(),
                            employee.getHireDate(),
                            employee.getStatus()
                    });
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to load employees: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading employees: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private void updateStatusWithPaginationInfo(EmployeePageResponse response) {
        String statusText = String.format(
                "Showing %d of %d employees (Page %d of %d)",
                response.getNumberOfElements(),
                response.getTotalElements(),
                response.getNumber() + 1,
                response.getTotalPages()
        );


    }
    private void showAddEmployeeDialog() {
        AddEmployeeDialog dialog = new AddEmployeeDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                currentUser
        );
        dialog.setVisible(true);

        if (dialog.isSuccessful()) {
            loadEmployees(); // Refresh the list
        }
    }

    private void deleteSelectedEmployee() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select an employee to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Get the employee ID from the selected row
        Long employeeId = (Long) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this employee?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_BASE_URL + "/" + employeeId))
                        .DELETE()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() == 200) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Employee deleted successfully.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadEmployees(); // Refresh the list
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to delete employee: " + response.body(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error deleting employee: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
    private void showEmployeeDetailsDialog() {
        int selectedRow = employeesTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        // Get the employee ID from the selected row
        Long employeeId = (Long) tableModel.getValueAt(selectedRow, 0);

        try {
            // Get base64-encoded credentials
            String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(
                    credentials.getBytes(StandardCharsets.UTF_8)
            );

            // Create request to fetch employee details
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + employeeId))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse employee details
                Employee employee = objectMapper.readValue(response.body(), Employee.class);

                // Create and show details dialog
                EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        employee,
                        true
                        , currentUser
                );
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to fetch employee details: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error fetching employee details: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}