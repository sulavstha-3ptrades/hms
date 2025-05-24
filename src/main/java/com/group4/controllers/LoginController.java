package com.group4.controllers;

import com.group4.App;
import com.group4.models.User;
import com.group4.services.AuthenticationService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import com.group4.utils.ViewManager;

/**
 * Controller for the login screen.
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    private AuthenticationService authService;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        authService = new AuthenticationService();

        // Add event listener to clear error when user types
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
    }

    /**
     * Handles the login button click.
     * Attempts to authenticate the user with provided credentials.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required");
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        // Disable controls during authentication
        setControlsDisabled(true);

        // Attempt login
        TaskUtils.executeTaskWithProgress(authService.login(email, password),
                user -> {
                    if (user != null) {
                        // Login successful
                        SessionManager.getInstance().setCurrentUser(user);
                        navigateToDashboard(user);
                    } else {
                        // Login failed
                        Platform.runLater(() -> {
                            showError("Invalid email or password");
                            setControlsDisabled(false);
                        });
                    }
                },
                error -> {
                    Platform.runLater(() -> {
                        showError("An error occurred: " + error.getMessage());
                        setControlsDisabled(false);
                    });
                });
    }

    /**
     * Handles the register button click.
     * Navigates to the registration screen.
     */
    @FXML
    private void handleRegisterClick() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Registration.fxml"));
            Parent root = loader.load();
            
            // Use ViewManager to switch to registration view
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load registration screen: " + e.getMessage());
        }
    }

    /**
     * Navigates to the appropriate dashboard based on user role.
     * 
     * @param user The authenticated user
     */
    private void navigateToDashboard(User user) {
        try {
            String dashboardPath;
            String role = user.getRole().toUpperCase();

            switch (role) {
                case "ADMIN":
                    dashboardPath = "view/AdminDashboard.fxml";
                    break;
                case "MANAGER":
                    dashboardPath = "view/ManagerDashboard.fxml";
                    break;
                case "SCHEDULER":
                    dashboardPath = "view/SchedulerDashboard.fxml";
                    break;
                case "CUSTOMER":
                    dashboardPath = "view/CustomerDashboard.fxml";
                    break;
                default:
                    dashboardPath = "view/CustomerDashboard.fxml";
                    break;
            }

            FXMLLoader loader = new FXMLLoader(App.class.getResource(dashboardPath));
            Parent root = loader.load();
            
            // Use ViewManager to switch views without creating a new Scene
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                showError("Could not load dashboard: " + e.getMessage());
                setControlsDisabled(false);
            });
        }
    }

    /**
     * Shows an error message.
     * 
     * @param message The error message to show
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Sets the enabled state of the controls.
     * 
     * @param disabled True to disable controls, false to enable
     */
    private void setControlsDisabled(boolean disabled) {
        emailField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
        registerButton.setDisable(disabled);
    }
}