package com.group4.controllers;

import com.group4.models.User;
import com.group4.services.UserService;
import com.group4.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the user profile view.
 */
public class ProfileController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField contactNumberField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private UserService userService;
    private User currentUser;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userService = new UserService();
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            messageLabel.setText("Error: No user logged in");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }
        loadUserData();
    }

    /**
     * Loads the current user's data into the form.
     */
    private void loadUserData() {
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            contactNumberField.setText(currentUser.getContactNumber());
            // Clear password fields
            passwordField.clear();
            confirmPasswordField.clear();
        }
        messageLabel.setText("");
    }

    /**
     * Handles the update profile button action.
     */
    @FXML
    private void handleUpdateProfile() {
        System.out.println("=== Starting profile update ===");
        
        // Validate input
        if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
            String errorMsg = "First name and last name are required.";
            System.err.println("Validation error: " + errorMsg);
            showMessage(errorMsg, true);
            return;
        }

        // Validate password if provided
        if (!passwordField.getText().isEmpty()) {
            if (passwordField.getText().length() < 6) {
                String errorMsg = "Password must be at least 6 characters long.";
                System.err.println("Validation error: " + errorMsg);
                showMessage(errorMsg, true);
                return;
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                String errorMsg = "Passwords do not match.";
                System.err.println("Validation error: " + errorMsg);
                showMessage(errorMsg, true);
                return;
            }
        }

        try {
            System.out.println("Updating user with ID: " + currentUser.getUserId());
            System.out.println("Current email: " + currentUser.getEmail());
            System.out.println("New first name: " + firstNameField.getText().trim());
            System.out.println("New last name: " + lastNameField.getText().trim());
            
            // Update user object
            currentUser.setFirstName(firstNameField.getText().trim());
            currentUser.setLastName(lastNameField.getText().trim());
            currentUser.setContactNumber(contactNumberField.getText().trim());
            
            // Only update password if a new one was provided
            if (!passwordField.getText().isEmpty()) {
                System.out.println("Password field has content, updating password");
                currentUser.setPassword(passwordField.getText());
            } else {
                System.out.println("No password change requested");
            }

            // Save changes
            System.out.println("Calling userService.updateUser()...");
            boolean success = userService.updateUser(currentUser);
            System.out.println("Update operation result: " + success);
            
            if (success) {
                String successMsg = "Profile updated successfully!";
                System.out.println(successMsg);
                showMessage(successMsg, false);
                // Clear password fields on success
                passwordField.clear();
                confirmPasswordField.clear();
                
                // Update the session with the latest user data
                SessionManager.getInstance().setCurrentUser(currentUser);
            } else {
                String errorMsg = "Failed to update profile. Please check the logs for details.";
                System.err.println(errorMsg);
                showMessage(errorMsg, true);
            }
        } catch (Exception e) {
            String errorMsg = "An error occurred: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            showMessage(errorMsg, true);
        }
    }

    /**
     * Displays a message to the user.
     *
     * @param message The message to display
     * @param isError Whether the message is an error (red) or success (green)
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setStyle(
            "-fx-text-fill: " + (isError ? "#e74c3c" : "#27ae60") + ";"
        );
    }
}
