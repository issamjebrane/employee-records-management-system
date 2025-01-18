package com.employeemanagement.ui;

import javax.swing.*;
import java.awt.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import net.miginfocom.swing.MigLayout;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.net.http.*;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

public class AddEmployeeDialog extends JDialog {
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField jobTitleField;
    private JTextField salaryField;
    private JComboBox<DepartmentItem> departmentComboBox;
    private JComboBox<String> statusComboBox;
    private JSpinner hireDateSpinner;

    private final User currentUser;
    private boolean isSuccess = false;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private static final String API_URL = "http://localhost:8080/api/v1/employees";
    //deparement url
    private static final String API_URL_DEPARTMENT = "http://localhost:8080/api/v1/departments";
    public AddEmployeeDialog(Frame owner, User currentUser) {
        super(owner, "Add New Employee", true);
        this.currentUser = currentUser;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        setupDialog();
    }

    private void setupDialog() {
        setLayout(new MigLayout("fillx, insets 20", "[][grow]", "[]10[]"));

        // Initialize components
        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        jobTitleField = new JTextField(20);
        salaryField = new JTextField(20);

        // Date Spinner
        SpinnerDateModel dateModel = new SpinnerDateModel();
        hireDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(hireDateSpinner, "yyyy-MM-dd");
        hireDateSpinner.setEditor(dateEditor);
        hireDateSpinner.setValue(new java.util.Date());

        // Comboboxes
//        departmentComboBox = new JComboBox<>(new String[]{"IT", "HR", "Finance", "Sales"});
        departmentComboBox = new JComboBox<>();  // Initialize it empty first
        statusComboBox = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ON_LEAVE"});

        // Add components to panel
        add(new JLabel("First Name:*"), "right");
        add(firstNameField, "growx, wrap");

        add(new JLabel("Last Name:*"), "right");
        add(lastNameField, "growx, wrap");

        add(new JLabel("Email:*"), "right");
        add(emailField, "growx, wrap");

        add(new JLabel("Job Title:*"), "right");
        add(jobTitleField, "growx, wrap");

        add(new JLabel("Department:"), "right");
        add(departmentComboBox, "growx, wrap");

        add(new JLabel("Salary:"), "right");
        add(salaryField, "growx, wrap");

        add(new JLabel("Hire Date:*"), "right");
        add(hireDateSpinner, "growx, wrap");

        add(new JLabel("Status:"), "right");
        add(statusComboBox, "growx, wrap");

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> handleSave());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, "span 2, growx, wrap");

        pack();
        setLocationRelativeTo(getOwner());
        loadDepartments();
    }

    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            EmployeeRequest employee = createEmployeeFromForm();
            String jsonEmployee = objectMapper.writeValueAsString(employee);

            // Debug print - add this line
            System.out.println("Sending to server: " + jsonEmployee);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + getBasicAuthHeader())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonEmployee))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Add response debug - add this line
            System.out.println("Server response: " + response.body());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                isSuccess = true;
                JOptionPane.showMessageDialog(this,
                        "Employee added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add employee: " + response.body(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();  // Add this for more detailed error info
            JOptionPane.showMessageDialog(this,
                    "Error adding employee: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validateInput() {
        if (firstNameField.getText().trim().isEmpty()) {
            showError("First name is required");
            return false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            showError("Last name is required");
            return false;
        }
        if (!isValidEmail(emailField.getText().trim())) {
            showError("Invalid email format");
            return false;
        }
        if (jobTitleField.getText().trim().isEmpty()) {
            showError("Job title is required");
            return false;
        }

        try {
            if (!salaryField.getText().trim().isEmpty()) {
                Double.parseDouble(salaryField.getText().trim());
            }
        } catch (NumberFormatException e) {
            showError("Invalid salary format");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private EmployeeRequest createEmployeeFromForm() {
        EmployeeRequest request = new EmployeeRequest();

        request.setFirstName(firstNameField.getText().trim());
        request.setLastName(lastNameField.getText().trim());
        request.setEmail(emailField.getText().trim());
        request.setJobTitle(jobTitleField.getText().trim());

        // Format date as string "yyyy-MM-dd"
        java.util.Date date = (java.util.Date) hireDateSpinner.getValue();
        String formattedDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(date);
        request.setHireDate(formattedDate);


        // Handle department
        DepartmentItem selectedDept = (DepartmentItem) departmentComboBox.getSelectedItem();
        if (selectedDept != null && selectedDept.id() != null) {
            request.setDepartmentId(selectedDept.id());
        }

        // Handle salary
        if (!salaryField.getText().trim().isEmpty()) {
            request.setSalary(new BigDecimal(salaryField.getText().trim()));
        }

        request.setStatus(
                statusComboBox.getSelectedItem().toString()
        );

        return request;
    }

    private String getBasicAuthHeader() {
        String credentials = currentUser.getUsername() + ":" + currentUser.getPassword();
        return Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    public boolean isSuccessful() {
        return isSuccess;
    }

    private record DepartmentItem(Long id, String name) {

        @Override
            public String toString() {
                return name;
            }
        }
    private void loadDepartments() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/v1/departments"))
                    .header("Authorization", "Basic " + getBasicAuthHeader())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Department> departments = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<Department>>() {}
                );

                departmentComboBox.removeAllItems();
                departmentComboBox.addItem(new DepartmentItem(null, "Select Department"));

                for (Department dept : departments) {
                    departmentComboBox.addItem(new DepartmentItem(
                            dept.getDeptId(),
                            dept.getDeptName()
                    ));
                }
            } else {
                System.err.println("Failed to load departments: " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading departments: " + e.getMessage());
        }
    }

}

