package com.group4.services;

import com.group4.models.User;
import com.group4.utils.FileConstants;
import com.group4.utils.PasswordUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling user-related operations.
 */
public class UserService {
    private static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";
    private static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    private static final String DEFAULT_ADMIN_FIRST_NAME = "Admin";
    private static final String DEFAULT_ADMIN_LAST_NAME = "User";
    
    private String getUsersFilePath() {
        return FileConstants.getUsersFilePath();
    }

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
     * Retrieves a user by their email address.
     *
     * @param email The email address of the user to find
     * @return The User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Empty or null email provided");
            return null;
        }
        
        System.out.println("Looking up user with email: " + email);
        List<User> users = getAllUsers();
        System.out.println("Total users loaded: " + users.size());
        
        for (User user : users) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email.trim())) {
                System.out.println("Found user: " + user.getEmail() + " with role: " + user.getRole());
                return user;
            }
        }
        
        System.out.println("No user found with email: " + email);
        return null;
    }
    
    /**
     * Retrieves all users from the data store.
     *
     * @return A list of all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String usersFilePath = getUsersFilePath();
        File usersFile = new File(usersFilePath);
        
        // Ensure parent directory exists
        File parentDir = usersFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                System.err.println("Failed to create data directory: " + parentDir.getAbsolutePath());
                return users;
            }
        }
        

        
        System.out.println("Reading users from: " + usersFile.getAbsolutePath());
        
        // If file doesn't exist or is empty, create default admin user
        if (!usersFile.exists() || usersFile.length() == 0) {
            System.out.println((!usersFile.exists() ? "Users file does not exist" : "Users file is empty") + ". Creating default admin user...");
            users.add(createDefaultAdminUser());
            saveAllUsers(users);
            return users;
        }
        
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
                    // First, handle escaped pipes in the line
                    StringBuilder processedLine = new StringBuilder();
                    boolean escape = false;
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        if (c == '\\' && !escape) {
                            escape = true;
                            continue;
                        }
                        if (escape && c == '|') {
                            processedLine.append('|');
                            escape = false;
                        } else if (escape) {
                            processedLine.append('\\').append(c);
                            escape = false;
                        } else if (c == '|') {
                            processedLine.append('\u0000'); // Use a temporary delimiter
                        } else {
                            processedLine.append(c);
                        }
                    }
                    
                    // Split on the temporary delimiter
                    String[] parts = processedLine.toString().split("\u0000", -1);
                    
                    // Unescape any remaining backslashes
                    for (int i = 0; i < parts.length; i++) {
                        parts[i] = parts[i].replace("\\\\", "\\");
                    }
                    
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
                        } else {
                            user.setProfilePicture(""); // Ensure empty string instead of null
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
     * Creates a default admin user with predefined credentials.
     * @return A new User instance with admin privileges
     */
    public User createDefaultAdminUser() {
        System.out.println("Creating default admin user...");
        String userId = "ADMIN-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        String hashedPassword = PasswordUtils.hashPassword(DEFAULT_ADMIN_PASSWORD);
        
        System.out.println("Creating admin user with ID: " + userId);
        System.out.println("Admin email: " + DEFAULT_ADMIN_EMAIL);
        System.out.println("Admin password hash: " + hashedPassword);
        
        User admin = new User(
            userId,
            DEFAULT_ADMIN_FIRST_NAME,
            DEFAULT_ADMIN_LAST_NAME,
            "Admin",  // Role with capital A to match expected role names
            DEFAULT_ADMIN_EMAIL,
            hashedPassword,
            "1234567890",
            "ACTIVE"  // Uppercase to match status checks
        );
        admin.setProfilePicture("");
        
        System.out.println("Admin user created: " + admin);
        return admin;
    }
    
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
    
    /**
     * Escapes pipe characters in a string and handles null values
     * @param input The input string to escape
     * @return The escaped string, or empty string if input is null
     */
    private String escapePipe(String input) {
        if (input == null) {
            return "";
        }
        // Replace any existing escape sequences first to avoid double-escaping
        return input.replace("\\", "\\\\").replace("|", "\\|");
    }
    
    /**
     * Saves all users to the data file.
     * @param users The list of users to save
     * @return true if the save was successful, false otherwise
     */
    public boolean saveAllUsers(List<User> users) {
        String usersFilePath = getUsersFilePath();
        System.out.println("Attempting to save " + users.size() + " users to: " + usersFilePath);
        
        // Ensure parent directory exists
        File parentDir = new File(usersFilePath).getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            boolean dirsCreated = parentDir.mkdirs();
            if (!dirsCreated) {
                System.err.println("Failed to create data directory: " + parentDir.getAbsolutePath());
                return false;
            }
        }
        
        // Create a temporary file first to ensure atomic write
        Path tempPath = Paths.get(usersFilePath + ".tmp");
        Path usersPath = Paths.get(usersFilePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(tempPath, StandardOpenOption.CREATE, 
                                                           StandardOpenOption.TRUNCATE_EXISTING)) {
            System.out.println("Successfully opened file for writing");
            for (User user : users) {
                String hashedPassword = user.getPassword();
                // If password is not already hashed (unlikely in update scenario but just in case)
                if (!hashedPassword.startsWith("$2a$")) {
                    hashedPassword = PasswordUtils.hashPassword(hashedPassword);
                }
                
                // Escape pipe characters in each field and handle null values
                String[] userData = {
                    escapePipe(user.getUserId()),
                    escapePipe(user.getFirstName()),
                    escapePipe(user.getLastName()),
                    escapePipe(user.getRole()),
                    escapePipe(user.getEmail()),
                    escapePipe(hashedPassword),
                    escapePipe(user.getContactNumber()),
                    escapePipe(user.getStatus()),
                    escapePipe(user.getProfilePicture())
                };
                
                // Join with pipe delimiter
                String userLine = String.join("|", userData);
                writer.write(userLine);
                writer.newLine();
            }
            
            // Close the writer before moving the file
            writer.close();
            
            // Atomically replace the old file with the new one
            Files.move(tempPath, usersPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            
            System.out.println("Successfully saved users to: " + usersPath);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to users file: " + e.getMessage());
            e.printStackTrace();
            try {
                // Clean up temp file if it exists
                if (Files.exists(tempPath)) {
                    Files.deleteIfExists(tempPath);
                }
            } catch (IOException ex) {
                System.err.println("Failed to clean up temp file: " + ex.getMessage());
            }
            return false;
        }
    }
}
