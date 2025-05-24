package com.group4.controllers;

import com.group4.App;
import com.group4.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import com.group4.utils.ViewManager;

import java.io.IOException;

/**
 * Controller for the Scheduler Dashboard.
 */
public class SchedulerDashboardController {

    @FXML
    private Button hallManagementButton;

    @FXML
    private Button logoutButton;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Initialization code if needed
    }

    /**
     * Handles the hall management button click.
     */
    @FXML
    private void handleHallManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/HallManagement.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message if needed
        }
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        try {
            // Clear the current user session
            SessionManager.getInstance().logout();

            // Load the login screen using ViewManager
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message if needed
        }
    }
}