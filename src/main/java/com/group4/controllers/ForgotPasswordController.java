package com.group4.controllers;

import com.group4.services.PasswordResetService;
import com.group4.utils.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for handling the password reset functionality.
 */
public class ForgotPasswordController {
    @FXML
    private TextField emailField;
    
    @FXML
    private Label errorLabel;
    
    @FXML
    private Label successLabel;
    
    @FXML
    private Button resetButton;
    
    private final PasswordResetService passwordResetService;
    
    /**
     * Default constructor.
     */
    public ForgotPasswordController() {
        this.passwordResetService = new PasswordResetService();
    }
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Add listeners to clear error/success messages when typing
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
            successLabel.setVisible(false);
        });
    }
    
    /**
     * Handles the password reset button click.
     */
    @FXML
    public void handleResetPassword() {
        String email = emailField.getText().trim();
        
        // Validate email
        if (email.isEmpty()) {
            showError("Email is required");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            return;
        }
        
        // Disable the button to prevent multiple clicks
        resetButton.setDisable(true);
        
        try {
            // In a real application, this would send an email with a reset link
            boolean success = passwordResetService.initiatePasswordReset(email);
            
            if (success) {
                // Show success message (in a real app, we wouldn't confirm if the email exists)
                showSuccess("If an account with that email exists, we've sent a password reset link.");
            } else {
                showError("Failed to initiate password reset. Please try again later.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred: " + e.getMessage());
        } finally {
            resetButton.setDisable(false);
        }
    }
    
    /**
     * Handles the back to login link click.
     */
    @FXML
    public void handleBackToLogin() {
        try {
            ViewManager.switchToView("com/group4/view/Login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Could not return to login screen: " + e.getMessage());
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
        successLabel.setVisible(false);
    }
    
    /**
     * Shows a success message.
     * 
     * @param message The success message to show
     */
    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }
}
