package com.employeemanagement.ui;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;
import java.util.function.Consumer;

public class NavigationPanel extends JPanel {
    private final Consumer<String> navigationHandler;
    private JButton employeesButton;
    private JButton userManagementButton;
    private JButton settingsButton;

    public NavigationPanel(Consumer<String> navigationHandler) {
        this.navigationHandler = navigationHandler;
        setupPanel();
        createButtons();
    }

    private void setupPanel() {
        setLayout(new MigLayout("fillx, wrap 1", "[grow]", "[]10[]"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private void createButtons() {
        employeesButton = createNavButton("Employees", "EMPLOYEES");
        userManagementButton = createNavButton("User Management", "USERS");
        settingsButton = createNavButton("Settings", "SETTINGS");

        userManagementButton.setVisible(false);
        settingsButton.setVisible(false);
    }

    private JButton createNavButton(String text, String destination) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.addActionListener(e -> navigationHandler.accept(destination));
        button.setPreferredSize(new Dimension(180, 30));
        add(button, "growx");
        return button;
    }

    public void enableAdminFeatures() {
        employeesButton.setVisible(true);
        userManagementButton.setVisible(true);
        settingsButton.setVisible(true);
    }

    public void enableHRFeatures() {
        employeesButton.setVisible(true);
        userManagementButton.setVisible(false);
        settingsButton.setVisible(false);
    }

    public void enableManagerFeatures() {
        employeesButton.setText("Department Employees");
        employeesButton.setVisible(true);
        userManagementButton.setVisible(false);
        settingsButton.setVisible(false);
    }
}