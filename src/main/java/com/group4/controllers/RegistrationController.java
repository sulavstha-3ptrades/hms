package com.group4.controllers;

import com.group4.App;
import com.group4.services.AuthenticationService;
import com.group4.utils.TaskUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

/**
 * Controller for the registration screen.
 */
public class RegistrationController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField contactNumberField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    private AuthenticationService authService;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        authService = new AuthenticationService();

        // Populate role dropdown
        roleComboBox.setItems(FXCollections.observableArrayList(
                Arrays.asList("Customer", "Scheduler", "Manager", "Admin")));
        roleComboBox.setValue("Customer");

        // Add event listeners to clear error when user types
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
    }

    /**
     * Handles the register button click.
     * Attempts to register a new user with the provided information.
     */
    @FXML
    private void handleRegister() {
        // Get form values
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String contactNumber = contactNumberField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                contactNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required");
            return;
        }

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return;
        }

        // Validate password match
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        // Validate password strength
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long");
            return;
        }

        // Disable controls during registration
        setControlsDisabled(true);

        // Attempt registration
        TaskUtils.executeTaskWithProgress(
                authService.register(firstName, lastName, email, password, contactNumber, role),
                registeredUser -> {
                    if (registeredUser != null) {
                        // Registration successful
                        Platform.runLater(() -> {
                            showSuccess();
                            navigateToLogin();
                        });
                    } else {
                        // Registration failed
                        Platform.runLater(() -> {
                            showError("Registration failed. Email may already be in use.");
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
     * Handles the login button click.
     * Navigates back to the login screen.
     */
    @FXML
    private void handleLoginClick() {
        navigateToLogin();
    }

    /**
     * Navigates to the login screen.
     */
    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load login screen: " + e.getMessage());
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
     * Shows a success message.
     */
    private void showSuccess() {
        errorLabel.setText("Registration successful! Redirecting to login...");
        errorLabel.setStyle("-fx-text-fill: green;");
        errorLabel.setVisible(true);
    }

    /**
     * Sets the enabled state of the controls.
     * 
     * @param disabled True to disable controls, false to enable
     */
    private void setControlsDisabled(boolean disabled) {
        firstNameField.setDisable(disabled);
        lastNameField.setDisable(disabled);
        emailField.setDisable(disabled);
        contactNumberField.setDisable(disabled);
        roleComboBox.setDisable(disabled);
        passwordField.setDisable(disabled);
        confirmPasswordField.setDisable(disabled);
        registerButton.setDisable(disabled);
        loginButton.setDisable(disabled);
    }
}