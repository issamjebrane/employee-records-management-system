package com.employeemanagement.ui;


import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EmployeeDetailsDialog extends JDialog {
    private static final String API_BASE_URL = "http://localhost:8080/api/v1/employees";
    private final Employee employee;
    private final boolean isEditable;
    private final User currentUser;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField hireDateField;
    private JTextField jobTitleField;
    private JTextField departmentField;
    private JTextField salaryField;
    private JTextField statusField;
    private JTextField managerNameField;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    public EmployeeDetailsDialog(Window parent, Employee employee, boolean isEditable, User currentUser) {
        super(parent, "Employee Details", ModalityType.APPLICATION_MODAL);
        this.employee = employee;
        this.isEditable = isEditable;
        this.currentUser = currentUser;
        this.objectMapper = new ObjectMapper();
        initComponents();
        layoutComponents();
        populateFields();

        setSize(400, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Initialize text fields
        firstNameField = createTextField();
        lastNameField = createTextField();
        emailField = createTextField();
        hireDateField = createTextField();
        jobTitleField = createTextField();
        departmentField = createTextField();
        salaryField = createTextField();
        statusField = createTextField();
        managerNameField = createTextField();

        // Disable editing if not in editable mode
        if (!isEditable) {
            setFieldsNonEditable();
        }
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setEditable(isEditable);
        return textField;
    }

    private void setFieldsNonEditable() {
        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        emailField.setEditable(false);
        hireDateField.setEditable(false);
        jobTitleField.setEditable(false);
        departmentField.setEditable(false);
        salaryField.setEditable(false);
        statusField.setEditable(false);
        managerNameField.setEditable(false);
    }

    private void populateFields() {
        if (employee == null) return;

        firstNameField.setText(employee.getFirstName());
        lastNameField.setText(employee.getLastName());
        emailField.setText(employee.getEmail());
        hireDateField.setText(employee.getHireDate());
        jobTitleField.setText(employee.getJobTitle());
        departmentField.setText(employee.getDepartmentName());
        salaryField.setText(String.format("$%.2f", employee.getSalary()));
        statusField.setText(employee.getStatus());
        managerNameField.setText(employee.getManagerName());
    }

    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2, gap 10", "[right][:200:]"));

        // Add components
        add(new JLabel("Employee ID:"));
        add(new JLabel(employee.getEmpId() != null ? employee.getEmpId().toString() : "N/A"));

        add(new JLabel("First Name:"));
        add(firstNameField);

        add(new JLabel("Last Name:"));
        add(lastNameField);

        add(new JLabel("Email:"));
        add(emailField);

        add(new JLabel("Hire Date:"));
        add(hireDateField);

        add(new JLabel("Job Title:"));
        add(jobTitleField);

        add(new JLabel("Department:"));
        add(departmentField);

        add(new JLabel("Salary:"));
        add(salaryField);

        add(new JLabel("Status:"));
        add(statusField);

        add(new JLabel("Manager:"));
        add(managerNameField);

        // Additional metadata
        add(new JLabel("Created At:"));
        add(new JLabel(employee.getCreatedAt() != null ? employee.getCreatedAt() : "N/A"));

        add(new JLabel("Created By:"));
        add(new JLabel(employee.getCreatedBy() != null ? employee.getCreatedBy() : "N/A"));

        // Buttons
        if (isEditable) {
            JButton saveButton = new JButton("Save");
            JButton cancelButton = new JButton("Cancel");

            saveButton.addActionListener(e -> saveChanges());
            cancelButton.addActionListener(e -> dispose());

            add(saveButton, "span 2, split 2, align center");
            add(cancelButton);
        } else {
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());
            add(closeButton, "span 2, align center");
        }
    }

    private void saveChanges() {
        // Get base64-encoded credentials
        String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
        String encodedCredentials = Base64.getEncoder().encodeToString(
                credentials.getBytes(StandardCharsets.UTF_8)
        );

        try {
            // Validate inputs (basic validation)
            if (firstNameField.getText().trim().isEmpty() ||
                    lastNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "First Name and Last Name are required.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Update employee object
            Employee updatedEmployee = getEmployee();

            // Convert to JSON
            String jsonEmployee = objectMapper.writeValueAsString(updatedEmployee);

            // Create HTTP request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/" + updatedEmployee.getEmpId()))
                    .header("Authorization", "Basic " + encodedCredentials)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonEmployee))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Handle response
            if (response.statusCode() == 200) {
                JOptionPane.showMessageDialog(
                        this,
                        "Employee updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose(); // Close the dialog
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update employee: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error updating employee: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Getter to retrieve potentially modified employee
    public Employee getEmployee() {
        // If editable, update employee object with field values
        if (isEditable) {
            employee.setFirstName(firstNameField.getText());
            employee.setLastName(lastNameField.getText());
            employee.setEmail(emailField.getText());
            employee.setHireDate(hireDateField.getText());
            employee.setJobTitle(jobTitleField.getText());
            employee.setDepartmentName(departmentField.getText());

            // Parse salary (remove $ and parse)
            try {
                employee.setSalary(Double.parseDouble(
                        salaryField.getText().replace("$", "").replace(",", "")
                ));
            } catch (NumberFormatException ex) {
                // Handle parsing error
            }

            employee.setStatus(statusField.getText());
            employee.setManagerName(managerNameField.getText());
        }
        return employee;
    }
}