package com.group4.services;

import com.group4.models.User;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.SessionManager;
import javafx.concurrent.Task;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service for user authentication and registration.
 */
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    /**
     * Attempts to authenticate a user with the provided credentials.
     * 
     * @param email    The user's email
     * @param password The user's password
     * @return A Task that returns the authenticated User if successful, null
     *         otherwise
     */
    public Task<User> login(String email, String password) {
        return new Task<User>() {
            @Override
            protected User call() throws Exception {
                LOGGER.info("Attempting login for user: " + email);
                List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);

                LOGGER.info("Read " + lines.size() + " lines from users file");
                for (String line : lines) {
                    LOGGER.fine("Processing line: " + line);
                    User user = User.fromDelimitedString(line);

                    if (user != null) {
                        LOGGER.fine("User parsed: " + user.getEmail() + ", status: " + user.getStatus());

                        if (user.getEmail().equalsIgnoreCase(email) && user.getStatus().equalsIgnoreCase("ACTIVE")) {
                            LOGGER.info("Found matching user: " + user.getEmail());
                            LOGGER.info("Stored password hash: " + user.getPassword());

                            // Check password using BCrypt
                            boolean passwordMatch = false;
                            try {
                                LOGGER.info("Attempting to verify password...");
                                passwordMatch = BCrypt.checkpw(password, user.getPassword());
                                LOGGER.info("Password check result: " + passwordMatch);
                            } catch (Exception e) {
                                LOGGER.log(Level.SEVERE, "Error checking password: ", e);
                            }

                            if (passwordMatch) {
                                // Set the current user in the session
                                SessionManager.getInstance().setCurrentUser(user);
                                LOGGER.info("Login successful for user: " + user.getEmail());
                                return user;
                            } else {
                                LOGGER.info("Password mismatch for user: " + user.getEmail());
                            }
                        }
                    } else {
                        LOGGER.warning("Could not parse user from line: " + line);
                    }
                }

                LOGGER.info("Login failed for user: " + email);
                return null;
            }
        };
    }

    /**
     * Registers a new user with the provided information.
     * 
     * @param firstName     The user's first name
     * @param lastName      The user's last name
     * @param email         The user's email
     * @param password      The user's password
     * @param contactNumber The user's contact number
     * @param role          The user's role (default is "Customer")
     * @return A Task that returns the registered User if successful, null otherwise
     */
    public Task<User> register(String firstName, String lastName, String email,
            String password, String contactNumber, String role) {
        return new Task<User>() {
            @Override
            protected User call() throws Exception {
                // Check if email already exists
                if (emailExists(email)) {
                    return null;
                }

                // Generate a unique user ID
                String userId = UUID.randomUUID().toString();

                // Hash the password using BCrypt
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

                // Create a new user
                User user = new User(userId, firstName, lastName, role, email,
                        hashedPassword, contactNumber, "ACTIVE");

                // Save the user to the file
                FileHandler.appendLine(FileConstants.USERS_FILE, user.toDelimitedString());

                return user;
            }
        };
    }

    /**
     * Checks if an email already exists in the users file.
     * 
     * @param email The email to check
     * @return True if the email exists, false otherwise
     * @throws IOException If an I/O error occurs
     */
    private boolean emailExists(String email) throws IOException {
        List<String> lines = FileHandler.readLines(FileConstants.USERS_FILE);

        for (String line : lines) {
            User user = User.fromDelimitedString(line);
            if (user != null && user.getEmail().equals(email)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Updates a user's status (ACTIVE/BLOCKED).
     * 
     * @param userId The ID of the user to update
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
                    if (user != null && user.getUserId().equals(userId)) {
                        user.setStatus(status);
                        updatedLines.add(user.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
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