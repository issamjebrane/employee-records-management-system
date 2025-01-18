package com.employeemanagement.ui;

import javax.swing.*;
import java.awt.*;
import net.miginfocom.swing.MigLayout;


public class MainWindow extends JFrame {
    private final User currentUser;
    private JPanel mainContent;
    private JPanel navigationPanel;
    private CardLayout cardLayout;
    private JLabel statusLabel;

    public MainWindow(User user) {
        this.currentUser = user;
        initializeWindow();
        createComponents();
        setupLayout();
        configureForUserRole();
    }

    private void initializeWindow() {
        setTitle("Employee Management System - " + currentUser.getRole());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 768));
        setLocationRelativeTo(null);
    }

    private void createComponents() {
        // Initialize layouts
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        navigationPanel = new NavigationPanel(this::handleNavigation);
        statusLabel = new JLabel("Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");

        // Create menu bar
        setJMenuBar(createMenuBar());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        logoutItem.addActionListener(e -> handleLogout());
        exitItem.addActionListener(e -> handleExit());

        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    private void setupLayout() {
        setLayout(new MigLayout("fill", "[200!][grow]", "[grow][30!]"));

        // Add components
        add(navigationPanel, "cell 0 0, grow");
        add(mainContent, "cell 1 0, grow");
        add(statusLabel, "cell 0 1 2 1, growx");
    }

    private void configureForUserRole() {
        NavigationPanel navPanel = (NavigationPanel) navigationPanel;

        switch (currentUser.getRole()) {
            case ADMIN:
                navPanel.enableAdminFeatures();
                EmployeesPanel employeesPanel = new EmployeesPanel(currentUser);
                UserPanel userManagementPanel = new UserPanel(currentUser);

                addContentPanel("EMPLOYEES", employeesPanel);
                addContentPanel("USERS", userManagementPanel);
                break;
            case HR:
                navPanel.enableHRFeatures();
                EmployeesPanel hrEmployeesPanel = new EmployeesPanel(currentUser);
                // TODO: Potentially disable some features for HR
                addContentPanel("EMPLOYEES", hrEmployeesPanel);
                break;
            case MANAGER:
                navPanel.enableManagerFeatures();
                break;
            default:
                throw new IllegalStateException("Unsupported user role: " + currentUser.getRole());
        }
    }

    private void handleNavigation(String destination) {
        cardLayout.show(mainContent, destination);
        statusLabel.setText("Current view: " + destination + " | User: " +
                currentUser.getUsername() + " (" + currentUser.getRole() + ")");
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Show login screen again
            new LoginScreen().setVisible(true);
        }
    }

    private void handleExit() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // Method to add new content panels
    public void addContentPanel(String name, JPanel panel) {
        mainContent.add(panel, name);
    }
}