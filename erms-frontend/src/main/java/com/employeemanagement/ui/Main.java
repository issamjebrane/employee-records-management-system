package com.employeemanagement.ui;

import com.employeemanagement.ui.screens.LoginScreen;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        setupLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(e);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void showErrorDialog(Exception e) {
        JOptionPane.showMessageDialog(null,
                "Error starting application: " + e.getMessage(),
                "Application Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}