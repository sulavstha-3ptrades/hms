package com.group4.services;

import com.group4.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

/**
 * Service for handling password reset functionality.
 */
public class PasswordResetService {
    private static final Logger LOGGER = Logger.getLogger(PasswordResetService.class.getName());
    private final UserService userService;

    public PasswordResetService() {
        this.userService = new UserService();
    }

    /**
     * Initiates the password reset process by generating an OTP.
     * 
     * @param email The email address of the user requesting a password reset
     * @return The generated OTP if email exists, null otherwise
     */
    public String initiatePasswordReset(String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                LOGGER.warning("Password reset requested for non-existent email: " + email);
                // Return null to indicate email doesn't exist
                return null;
            }

            // Generate and return OTP
            String otp = OTPService.generateOTP(email);
            LOGGER.info("Generated OTP for " + email + ": " + otp);
            return otp;
        } catch (Exception e) {
            LOGGER.severe("Error initiating password reset: " + e.getMessage());
            return null;
        }
    }

    /**
     * Resets a user's password after OTP verification.
     * 
     * @param email The user's email
     * @param otp The OTP for verification
     * @param newPassword The new password
     * @return true if the password was reset successfully, false otherwise
     */
    public boolean resetPassword(String email, String otp, String newPassword) {
        try {
            // Validate OTP first
if (!OTPService.validateOTP(email, otp)) {
                LOGGER.warning("Invalid or expired OTP provided for email: " + email);
                return false;
            }

            // Get the user
            User user = userService.getUserByEmail(email);
            if (user == null) {
                LOGGER.warning("Password reset attempted for non-existent email: " + email);
                return false;
            }

            // Update the password
            user.setPassword(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            boolean success = userService.updateUser(user);
            
            if (success) {
                LOGGER.info("Password reset successful for user: " + email);
            } else {
                LOGGER.warning("Failed to update password for user: " + email);
            }
            
            return success;
        } catch (Exception e) {
            LOGGER.severe("Error resetting password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates an OTP for password reset.
     * 
     * @param email The user's email
     * @param otp The OTP to validate
     * @return true if OTP is valid, false otherwise
     */
    public boolean validateOTP(String email, String otp) {
        return OTPService.validateOTP(email, otp);
    }

    // OTP generation is now handled by OTPService
}
