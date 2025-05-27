package com.group4.services;

import java.util.UUID;
import com.group4.models.User;
import org.mindrot.jbcrypt.BCrypt;
import java.util.logging.Logger;

/**
 * Service for handling password reset functionality.
 */
public class PasswordResetService {
    private static final Logger LOGGER = Logger.getLogger(PasswordResetService.class.getName());
    private static final long RESET_TOKEN_EXPIRY_HOURS = 24; // Token expires in 24 hours

    private final UserService userService;

    public PasswordResetService() {
        this.userService = new UserService();
    }

    /**
     * Initiates the password reset process by generating a reset token.
     * 
     * @param email The email address of the user requesting a password reset
     * @return true if the reset process was initiated successfully, false otherwise
     */
    public boolean initiatePasswordReset(String email) {
        try {
            User user = userService.getUserByEmail(email);
            if (user == null) {
                LOGGER.warning("Password reset requested for non-existent email: " + email);
                // Return true to prevent email enumeration attacks
                return true;
            }

            // Generate a reset token
            String resetToken = generateResetToken();
            long expiryTime = System.currentTimeMillis() + (RESET_TOKEN_EXPIRY_HOURS * 60 * 60 * 1000);
            
            // In a real application, you would send an email with the reset link
            // For now, we'll just log it
            LOGGER.info("Password reset token for " + email + ": " + resetToken);
            LOGGER.info("This token will expire at: " + new java.util.Date(expiryTime));
            
            // Store the token and expiry time (in a real app, this would be in a database)
            // For now, we'll just log it since we don't have a token storage mechanism
            
            return true;
        } catch (Exception e) {
            LOGGER.severe("Error initiating password reset: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resets a user's password using a valid reset token.
     * 
     * @param email The user's email
     * @param token The reset token
     * @param newPassword The new password
     * @return true if the password was reset successfully, false otherwise
     */
    public boolean resetPassword(String email, String token, String newPassword) {
        try {
            // In a real application, you would validate the token here
            // For now, we'll just validate the user exists
            User user = userService.getUserByEmail(email);
            if (user == null) {
                LOGGER.warning("Password reset attempted for non-existent email: " + email);
                return false;
            }

            // In a real application, you would:
            // 1. Verify the token is valid and not expired
            // 2. Update the user's password
            // 3. Invalidate the token
            
            // For now, we'll just update the password
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
     * Generates a random reset token.
     * 
     * @return A random UUID string
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }
}
