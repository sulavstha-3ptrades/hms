package com.group4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import com.group4.models.User;
import com.group4.services.AuthenticationService;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.TaskUtils;

/**
 * Tests for the AuthenticationService class.
 */
public class UserAuthenticationTest {

    private AuthenticationService authService;
    private String testUsersFile;
    private String testEmail;
    private String testPassword;
    private String testUserId;

    @BeforeEach
    public void setup() throws Exception {
        // Set up test environment
        testUsersFile = "data/test_users.txt";

        // Ensure the test file directory exists
        new File("data").mkdirs();

        // Create a test instance with a unique email
        testUserId = UUID.randomUUID().toString();
        testEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        testPassword = "Password123!";

        // Ensure the test file is empty
        Files.writeString(Paths.get(testUsersFile), "");

        // Create a new authentication service
        authService = new AuthenticationService();
    }

    @AfterEach
    public void cleanup() throws Exception {
        // Delete the test file
        Files.deleteIfExists(Paths.get(testUsersFile));
    }

    @Test
    public void testRegistration() throws Exception {
        // Register a new user using TaskUtils to execute the task
        User user = TaskUtils.executeTask(authService.register(
                "Test", "User", testEmail, testPassword, "1234567890", "Customer"));

        // Assert registration was successful
        assertNotNull(user, "User should be registered successfully");
        assertEquals("Test", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals(testEmail, user.getEmail());
        assertEquals("1234567890", user.getContactNumber());
        assertEquals("Customer", user.getRole());
        assertEquals("ACTIVE", user.getStatus());

        // Verify the password is hashed
        assertTrue(BCrypt.checkpw(testPassword, user.getPassword()),
                "Password should be hashed and verifiable with BCrypt");

        // Read back from file to confirm storage
        List<String> lines = Files.readAllLines(Paths.get(FileConstants.USERS_FILE));
        boolean found = false;
        for (String line : lines) {
            if (line.contains(testEmail)) {
                found = true;
                break;
            }
        }
        assertTrue(found, "User should be stored in the users file");
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // First register a user
        User registeredUser = TaskUtils.executeTask(authService.register(
                "Test", "User", testEmail, testPassword, "1234567890", "Customer"));

        // Now try to log in
        User loggedInUser = TaskUtils.executeTask(authService.login(testEmail, testPassword));

        // Assert login was successful
        assertNotNull(loggedInUser, "User should be able to log in with correct credentials");
        assertEquals(registeredUser.getUserId(), loggedInUser.getUserId());
        assertEquals(testEmail, loggedInUser.getEmail());
    }

    @Test
    public void testLoginFailureWrongPassword() throws Exception {
        // First register a user
        TaskUtils.executeTask(authService.register(
                "Test", "User", testEmail, testPassword, "1234567890", "Customer"));

        // Try to log in with wrong password
        User loggedInUser = TaskUtils.executeTask(authService.login(testEmail, "wrongpassword"));

        // Assert login failed
        assertNull(loggedInUser, "User should not be able to log in with wrong password");
    }

    @Test
    public void testLoginFailureWrongEmail() throws Exception {
        // First register a user
        TaskUtils.executeTask(authService.register(
                "Test", "User", testEmail, testPassword, "1234567890", "Customer"));

        // Try to log in with wrong email
        User loggedInUser = TaskUtils.executeTask(authService.login("wrong@example.com", testPassword));

        // Assert login failed
        assertNull(loggedInUser, "User should not be able to log in with wrong email");
    }
}