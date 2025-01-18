package com.employeemanagement.ui;

import javax.swing.*;
import java.awt.*;

import lombok.Getter;
import net.miginfocom.swing.MigLayout;

public class UserDetailsDialog extends JDialog {
    // Getter to retrieve potentially modified user
    @Getter
    private final User user;
    private final User currentUser;
    private boolean isEditable;

    // Text fields for potential editing
    private JTextField usernameField;
    private JTextField emailField;
    private JComboBox<UserRole> roleComboBox;
    private JCheckBox activeCheckBox;

    // Employee-related fields
    private JTextField employeeNameField;
    private JTextField employeeDepartmentField;
    private JTextField employeePositionField;

    public UserDetailsDialog(Window parent, User user, boolean isEditable, User currentUser) {
        super(parent, "User Details", ModalityType.APPLICATION_MODAL);
        this.user = user;
        this.isEditable = isEditable;
        this.currentUser = currentUser;
        initComponents();
        layoutComponents();
        populateFields();

        setSize(400, 600);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Initialize text fields and components
        usernameField = createTextField();
        emailField = createTextField();

        // Role selection
        roleComboBox = new JComboBox<>(UserRole.values());
        roleComboBox.setEnabled(isEditable);

        // Active status
        activeCheckBox = new JCheckBox("Active");
        activeCheckBox.setEnabled(isEditable);

        // Employee-related fields
        employeeNameField = createTextField();
        employeeDepartmentField = createTextField();
        employeePositionField = createTextField();

        // Disable employee fields
        employeeNameField.setEditable(false);
        employeeDepartmentField.setEditable(false);
        employeePositionField.setEditable(false);
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setEditable(isEditable);
        return textField;
    }

    private void populateFields() {
        if (user == null) return;

        // User details
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        roleComboBox.setSelectedItem(user.getRole());

    }

    private void layoutComponents() {
        setLayout(new MigLayout("wrap 2, gap 10", "[right][:200:]"));

        // User Details Section
        add(new JLabel("User Details:"), "span 2, align center");

        add(new JLabel("User ID:"));
        add(new JLabel(user.getUserId().toString()));

        add(new JLabel("Username:"));
        add(usernameField);

        add(new JLabel("Email:"));
        add(emailField);

        add(new JLabel("Role:"));
        add(roleComboBox);

        add(new JLabel("Status:"));
        add(activeCheckBox);

        // Employee Details Section
        add(new JLabel("Employee Details:"), "span 2, align center, gaptop 10");

        add(new JLabel("Name:"));
        add(employeeNameField);

        add(new JLabel("Department:"));
        add(employeeDepartmentField);

        add(new JLabel("Position:"));
        add(employeePositionField);

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
        // Update user object with form values
        user.setUsername(usernameField.getText());
        user.setEmail(emailField.getText());
        user.setRole((UserRole) roleComboBox.getSelectedItem());

        // Here you would typically:
        // 1. Validate input
        // 2. Send update request to backend
        // For now, just close the dialog
        JOptionPane.showMessageDialog(
                this,
                "Save functionality will be implemented soon.",
                "Coming Soon",
                JOptionPane.INFORMATION_MESSAGE
        );
        dispose();
    }

}