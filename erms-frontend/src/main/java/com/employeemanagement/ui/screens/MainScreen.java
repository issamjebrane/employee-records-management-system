package com.employeemanagement.ui.screens;

import net.miginfocom.swing.MigLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainScreen extends JFrame {
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JToolBar toolbar;
    private String currentUser;

    public MainScreen(String username) {
        this.currentUser = username;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setupFrame();
    }

    private void initializeComponents() {
        // Initialize Sidebar
        sidebarPanel = createSidebarPanel();

        // Initialize Toolbar
        toolbar = createToolbar();

        // Initialize Content Panel
        contentPanel = new JPanel(new MigLayout("fill"));
        contentPanel.setBackground(Color.WHITE);

        // Add welcome message to content panel
        JLabel welcomeLabel = new JLabel("Welcome to ERMS", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        contentPanel.add(welcomeLabel, "dock center");
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel(new MigLayout("fillx", "[grow]", "[]10[]"));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(new Color(51, 51, 51));

        // Add navigation buttons
        String[] menuItems = {
                "Dashboard", "Employees", "Departments",
                "Reports", "Settings"
        };

        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebar.add(menuButton, "growx, wrap");
        }

        return sidebar;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(51, 51, 51));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(75, 75, 75));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(51, 51, 51));
            }
        });

        // Add click handler
        button.addActionListener(e -> handleMenuClick(text));

        return button;
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(Color.WHITE);

        // Add toolbar buttons
        JButton newButton = new JButton("New Employee");
        JButton refreshButton = new JButton("Refresh");
        JButton exportButton = new JButton("Export");

        // Add user info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel userLabel = new JLabel("Logged in as: " + currentUser);
        JButton logoutButton = new JButton("Logout");

        // Add components to toolbar
        toolbar.add(newButton);
        toolbar.addSeparator();
        toolbar.add(refreshButton);
        toolbar.add(exportButton);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(userLabel);
        toolbar.addSeparator();
        toolbar.add(logoutButton);

        // Add action listeners
        newButton.addActionListener(e -> showNewEmployeeDialog());
        logoutButton.addActionListener(e -> handleLogout());

        return toolbar;
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Add toolbar at the top
        add(toolbar, BorderLayout.NORTH);

        // Create split pane for sidebar and content
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                sidebarPanel,
                contentPanel
        );
        splitPane.setDividerLocation(200);
        splitPane.setDividerSize(1);
        add(splitPane, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }

    private void setupFrame() {
        setTitle("Employee Records Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
    }

    private void handleMenuClick(String menuItem) {
        // TODO: Implement navigation logic
        switch (menuItem) {
            case "Dashboard":
                showDashboard();
                break;
            case "Employees":
                showEmployeeList();
                break;
            case "Departments":
                showDepartments();
                break;
            case "Reports":
                showReports();
                break;
            case "Settings":
                showSettings();
                break;
        }
    }

    private void showDashboard() {
        // TODO: Implement dashboard view
        updateContent("Dashboard");
    }

    private void showEmployeeList() {
        // TODO: Implement employee list view
        updateContent("Employees");
    }

    private void showDepartments() {
        // TODO: Implement departments view
        updateContent("Departments");
    }

    private void showReports() {
        // TODO: Implement reports view
        updateContent("Reports");
    }

    private void showSettings() {
        // TODO: Implement settings view
        updateContent("Settings");
    }

    private void updateContent(String title) {
        contentPanel.removeAll();
        JLabel label = new JLabel(title + " content coming soon...", SwingConstants.CENTER);
        contentPanel.add(label, "dock center");
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showNewEmployeeDialog() {
        // TODO: Implement new employee dialog
        JOptionPane.showMessageDialog(this,
                "New Employee form will be implemented soon.",
                "New Employee",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            dispose();
            new LoginScreen().setVisible(true);
        }
    }
}