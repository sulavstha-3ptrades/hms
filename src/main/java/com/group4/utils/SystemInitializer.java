package com.group4.utils;

import com.group4.models.User;
import com.group4.services.UserService;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles system initialization tasks such as creating default admin user.
 */
public class SystemInitializer {
    private static final Logger LOGGER = Logger.getLogger(SystemInitializer.class.getName());
    
    private SystemInitializer() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Initializes the system and ensures a default admin user exists.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeSystem() {
        try {
            LOGGER.info("Starting system initialization...");
            
            UserService userService = new UserService();
            
            // Check if admin user exists
            User admin = userService.getUserByEmail("admin@example.com");
            if (admin == null) {
                LOGGER.info("Admin user not found. Creating default admin user...");
                admin = userService.createDefaultAdminUser();
                
                // Get all existing users
                List<User> users = userService.getAllUsers();
                
                // Add the new admin user
                users.add(admin);
                
                // Save all users including the new admin
                if (!userService.saveAllUsers(users)) {
                    LOGGER.warning("Failed to save admin user to file");
                    return false;
                }
                
                LOGGER.info("Default admin user created successfully");
            } else {
                LOGGER.info("Admin user already exists");
            }
            
            LOGGER.info("System initialization completed successfully");
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during system initialization", e);
            return false;
        }
    }
}
