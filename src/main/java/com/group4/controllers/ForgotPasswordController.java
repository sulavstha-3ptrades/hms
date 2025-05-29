package com.group4.controllers;

import com.group4.services.PasswordResetService;
import com.group4.utils.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;

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
    private TextField otpField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label otpLabel;

    @FXML
    private Label newPasswordLabel;

    @FXML
    private Label confirmPasswordLabel;

    private String currentEmail;
    private String currentOTP;

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
        // Hide OTP and password fields initially
        setOTPFieldsVisible(false);

        // Add listeners to clear error/success messages when typing
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
            successLabel.setVisible(false);
        });

        otpField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });
    }

    /**
     * Handles the password reset button click.
     */
    @FXML
    public void handleResetPassword() {
        if (currentOTP == null) {
            // First step: Request OTP
            requestOTP();
        } else {
            // Second step: Verify OTP and reset password
            verifyOTPAndResetPassword();
        }
    }

    private void requestOTP() {
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
            // Generate and display OTP in console
            String otp = passwordResetService.initiatePasswordReset(email);

            if (otp != null) {
                // Store email and OTP for verification
                currentEmail = email;
                currentOTP = otp;

                // Show OTP and password fields
                setOTPFieldsVisible(true);

                // Update UI
                resetButton.setText("Reset Password");
                emailField.setDisable(true);
                showSuccess("OTP has been generated and shown in the console.");

                // Log the OTP to console
                System.out.println("\n=== PASSWORD RESET OTP ===");
                System.out.println("For email: " + email);
                System.out.println("OTP: " + otp);
                System.out.println("This OTP is valid for 5 minutes.");
                System.out.println("==========================\n");
            } else {
                showError("No account found with that email address.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred: " + e.getMessage());
        } finally {
            resetButton.setDisable(false);
        }
    }

    private void verifyOTPAndResetPassword() {
        String otp = otpField.getText().trim();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Password must be at least 8 characters long");
            return;
        }

        // Disable the button to prevent multiple clicks
        resetButton.setDisable(true);

        try {
            // Reset password using the OTP
            boolean success = passwordResetService.resetPassword(currentEmail, otp, newPassword);

            if (success) {
                showSuccess("Password has been reset successfully! Redirecting to login...");
                // Delay before navigation to show success message
                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(event -> {
                    try {
                        ViewManager.switchToView("com/group4/view/Login.fxml");
                    } catch (IOException e) {
                        e.printStackTrace();
                        showError("Error navigating to login: " + e.getMessage());
                    }
                });
                delay.play();
            } else {
                showError("Invalid or expired OTP. Please try again.");
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

    /**
     * Shows or hides the OTP and password fields.
     * 
     * @param visible Whether the fields should be visible
     */
    private void setOTPFieldsVisible(boolean visible) {
        otpLabel.setVisible(visible);
        otpField.setVisible(visible);
        newPasswordLabel.setVisible(visible);
        newPasswordField.setVisible(visible);
        confirmPasswordLabel.setVisible(visible);
        confirmPasswordField.setVisible(visible);
    }
}
