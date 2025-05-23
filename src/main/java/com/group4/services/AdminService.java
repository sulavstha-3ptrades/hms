package com.group4.services;

import com.group4.models.User;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import javafx.concurrent.Task;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service class for administrator operations related to user management.
 */
public class AdminService {

    /**
     * Gets all users from the system.
     * 
     * @return A Task that returns a list of all users
     */
    public Task<List<User>> getAllUsers() {
        return new Task<List<User>>() {
            @Override
            protected List<User> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);
                List<User> users = new ArrayList<>();

                for (String line : lines) {
                    User user = User.fromDelimitedString(line);
                    if (user != null) {
                        users.add(user);
                    }
                }

                return users;
            }
        };
    }

    /**
     * Creates a new user.
     * 
     * @param user The user to create
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> createUser(User user) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Generate a unique user ID if not present
                if (user.getUserId() == null || user.getUserId().isEmpty()) {
                    user.setUserId("USER-" + UUID.randomUUID().toString().substring(0, 8));
                }

                // Read existing users to check for duplicate emails
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);
                for (String line : lines) {
                    User existingUser = User.fromDelimitedString(line);
                    if (existingUser != null && existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                        return false;
                    }
                }

                // Hash the password if it hasn't been hashed yet
                if (!user.getPassword().startsWith("$2a$")) {
                    String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
                    user.setPassword(hashedPassword);
                }

                // Add the new user
                lines.add(user.toDelimitedString());
                FileHandler.writeLines(FileConstants.USERS_FILE, lines);
                return true;
            }
        };
    }

    /**
     * Updates an existing user.
     * 
     * @param user The user to update
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateUser(User user) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    User existingUser = User.fromDelimitedString(line);
                    if (existingUser != null) {
                        // Check if this is the user we want to update
                        if (existingUser.getUserId().equals(user.getUserId())) {
                            // Check if the email is being changed and if it conflicts with another user
                            if (!existingUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                                for (String checkLine : lines) {
                                    User checkUser = User.fromDelimitedString(checkLine);
                                    if (checkUser != null &&
                                            !checkUser.getUserId().equals(user.getUserId()) &&
                                            checkUser.getEmail().equalsIgnoreCase(user.getEmail())) {
                                        return false;
                                    }
                                }
                            }

                            // Update the user
                            updatedLines.add(user.toDelimitedString());
                            found = true;
                        } else {
                            updatedLines.add(line);
                        }
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.USERS_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Updates the status of a user (ACTIVE/BLOCKED).
     * 
     * @param userId The ID of the user
     * @param status The new status
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateUserStatus(String userId, String status) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    User user = User.fromDelimitedString(line);
                    if (user != null) {
                        if (user.getUserId().equals(userId)) {
                            user.setStatus(status);
                            updatedLines.add(user.toDelimitedString());
                            found = true;
                        } else {
                            updatedLines.add(line);
                        }
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.USERS_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Deletes a user from the system.
     * 
     * @param userId The ID of the user to delete
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> deleteUser(String userId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    User user = User.fromDelimitedString(line);
                    if (user != null && !user.getUserId().equals(userId)) {
                        updatedLines.add(line);
                    } else {
                        found = true;
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.USERS_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }
}