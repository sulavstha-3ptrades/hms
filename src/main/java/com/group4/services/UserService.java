package com.group4.services;

import com.group4.models.User;
import com.group4.utils.PasswordUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling user-related operations.
 */
public class UserService {
    private static final String USERS_FILE = "data/users.txt";
    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "Admin";
    private static final String DEFAULT_ADMIN_LAST_NAME = "User";

    /**
     * Updates an existing user's information.
     *
     * @param user The user with updated information
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUser(User user) {
        System.out.println("Starting update for user: " + user.getUserId());
        List<User> users = getAllUsers();
        boolean userFound = false;
        
        System.out.println("Total users in system: " + users.size());
        
        for (int i = 0; i < users.size(); i++) {
            User current = users.get(i);
            System.out.println("Checking user: " + current.getUserId() + ", email: " + current.getEmail());
            
            if (current.getUserId().equals(user.getUserId())) {
                System.out.println("Found matching user, updating...");
                users.set(i, user);
                userFound = true;
                break;
            }
        }
        
        if (!userFound) {
            System.err.println("Error: User not found with ID: " + user.getUserId());
            return false;
        }
        
        boolean saveResult = saveAllUsers(users);
        System.out.println("Save operation result: " + saveResult);
        return saveResult;
    }
    
    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to find
     * @return The User object if found, null otherwise
     */
    public User getUserById(String userId) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUserId().equals(userId)) {
                return user;
            }
        }
        return null;
    }
    
    /**
     * Retrieves all users from the data store.
     *
     * @return A list of all users
     */
    private List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        File usersFile = new File(USERS_FILE);
        
        // If file doesn't exist, create it with default admin user
        if (!usersFile.exists()) {
            System.out.println("Users file does not exist. Creating with default admin user...");
            users.add(createDefaultAdminUser());
            saveAllUsers(users);
            return users;
        }
        
        // If file is empty, create default admin user
        if (usersFile.length() == 0) {
            System.out.println("Users file is empty. Creating default admin user...");
            users.add(createDefaultAdminUser());
            saveAllUsers(users);
            return users;
        }
        
        System.out.println("Reading users from: " + usersFile.getAbsolutePath());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                
                if (line.isEmpty()) {
                    continue; // Skip empty lines
                }
                
                System.out.println("Processing line " + lineNumber + ": " + line);
                
                try {
                    String[] parts = line.split("\\|", -1);
                    if (parts.length >= 8) { // Ensure we have all required fields
                        User user = new User(
                            parts[0], // userId
                            parts[1], // firstName
                            parts[2], // lastName
                            parts[3], // role
                            parts[4], // email
                            parts[5], // password
                            parts[6], // contactNumber
                            parts[7]  // status
                        );
                        // Set profile picture if available (9th field, index 8)
                        if (parts.length > 8 && !parts[8].trim().isEmpty()) {
                            user.setProfilePicture(parts[8]);
                        }
                        users.add(user);
                        System.out.println("Loaded user: " + user.getEmail() + " with profile picture: " + user.getProfilePicture());
                    } else {
                        System.err.println("Skipping malformed user data: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing user data at line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Total users loaded: " + users.size());
        return users;
    }
    
    /**
     * Saves all users to the data store.
     *
     * @param users The list of users to save
     * @return true if the save was successful, false otherwise
     */
    /**
     * Creates a default admin user with predefined credentials.
     * @return A new User instance with admin privileges
     */
    private User createDefaultAdminUser() {
        System.out.println("Creating default admin user...");
        User admin = new User(
            java.util.UUID.randomUUID().toString(), // Generate a new UUID
            DEFAULT_ADMIN_FIRST_NAME,
            DEFAULT_ADMIN_LAST_NAME,
            "admin",
            DEFAULT_ADMIN_EMAIL,
            PasswordUtils.hashPassword(DEFAULT_ADMIN_PASSWORD),
            "1234567890",
            "active"
        );
        System.out.println("Created default admin user with email: " + DEFAULT_ADMIN_EMAIL);
        return admin;
    }
    
    /**
     * Saves all users to the users file.
     * @param users List of users to save
     * @return true if save was successful, false otherwise
     */
    /**
     * Verifies if the provided password matches the user's stored password
     * @param userId The ID of the user to verify
     * @param password The password to verify
     * @return true if the password is correct, false otherwise
     */
    public boolean verifyPassword(String userId, String password) {
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
        return PasswordUtils.verifyPassword(password, user.getPassword());
    }
    
    private boolean saveAllUsers(List<User> users) {
        System.out.println("Attempting to save " + users.size() + " users to: " + USERS_FILE);
        
        // Create parent directories if they don't exist
        try {
            Files.createDirectories(Paths.get(USERS_FILE).getParent());
            System.out.println("Created necessary directories");
        } catch (IOException e) {
            System.err.println("Error creating directories: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            System.out.println("Successfully opened file for writing");
            for (User user : users) {
                String hashedPassword = user.getPassword();
                // If password is not already hashed (unlikely in update scenario but just in case)
                if (!hashedPassword.startsWith("$2a$")) {
                    hashedPassword = PasswordUtils.hashPassword(hashedPassword);
                }
                
                // Using pipe as delimiter without escaping for the join
                String userLine = String.join("|",
                    user.getUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getEmail(),
                    hashedPassword,
                    user.getContactNumber(),
                    user.getStatus(),
                    user.getProfilePicture()
                );
                writer.println(userLine);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to users file: " + e.getMessage());
            return false;
        }
    }
}
