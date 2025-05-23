package com.group4.controllers;

import com.group4.App;
import com.group4.utils.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

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

            Stage stage = (Stage) hallManagementButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
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
        // Clear the session
        SessionManager.getInstance().setCurrentUser(null);

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message if needed
        }
    }
}