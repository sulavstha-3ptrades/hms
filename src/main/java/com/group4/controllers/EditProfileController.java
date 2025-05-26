package com.group4.controllers;

import com.group4.models.User;
import com.group4.services.UserService;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.io.*;
import java.nio.file.*;
import java.util.regex.Pattern;

/**
 * Controller for the Edit Profile dialog
 */
public class EditProfileController {

    // Form fields
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField contactNumberField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private User currentUser;
    private UserService userService;
    private String tempProfileImagePath; // Temporary storage for new profile picture

    public EditProfileController() {
        this.userService = new UserService();
    }

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        // Set up button actions
        saveButton.setOnAction(event -> saveProfile());
        cancelButton.setOnAction(event -> closeWindow());

        // Disable password fields by default until current password is entered
        passwordField.setDisable(true);
        confirmPasswordField.setDisable(true);

        // Enable new password fields only when current password is entered
        currentPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean disable = newVal == null || newVal.trim().isEmpty();
            passwordField.setDisable(disable);
            confirmPasswordField.setDisable(disable);
        });
    }

    /**
     * Sets the user data to be edited
     * 
     * @param user The user whose profile will be edited
     */
    public void setUserData(User user) {
        this.currentUser = user;
        populateUserData();
    }

    /**
     * Populates the form with user data
     */
    private void populateUserData() {
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            contactNumberField.setText(currentUser.getContactNumber());

            // Load profile picture
            loadProfileImage();
        }
    }
    
    /**
     * Loads the profile image, falling back to default if needed
     */
    private void loadProfileImage() {
        try {
            String imagePath = currentUser.getProfilePicture();
            Image profileImage = null;
            
            // Try to load the user's profile image if it exists
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                try {
                    // First try to load as a resource (for packaged JAR)
                    if (imagePath.startsWith("/")) {
                        // Try with leading slash (absolute path in resources)
                        profileImage = new Image(getClass().getResourceAsStream(imagePath));
                    } else {
                        // Try as a relative path
                        profileImage = new Image(getClass().getResourceAsStream("/" + imagePath));
                    }
                    
                    // If loading as resource failed, try as a file
                    if (profileImage == null || profileImage.isError()) {
                        File imageFile = new File(imagePath);
                        if (imageFile.exists()) {
                            profileImage = new Image(imageFile.toURI().toString());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading profile image from path " + imagePath + ": " + e.getMessage());
                }
            }
            
            // If we still don't have an image, load the default
            if (profileImage == null || profileImage.isError()) {
                profileImage = loadDefaultImage();
                // Set the default image path if we're using the default
                if (profileImage != null && (imagePath == null || imagePath.trim().isEmpty())) {
                    currentUser.setProfilePicture("/com/group4/assets/images/users/default-avatar.jpg");
                }
            }
            
            // Set the image in the view
            if (profileImage != null) {
                profileImageView.setImage(profileImage);
            } else {
                System.err.println("Failed to load any profile image");
            }
        } catch (Exception e) {
            System.err.println("Unexpected error loading profile image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates the form fields
     * 
     * @return true if validation passes, false otherwise
     */
    private boolean validateInput() {
        // Basic field validation
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty()) {
            showError("First name, last name, and email are required");
            return false;
        }

        // Email validation
        if (!isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address");
            return false;
        }

        // Phone number validation (if provided)
        String phone = contactNumberField.getText().trim();
        if (!phone.isEmpty() && !isValidPhoneNumber(phone)) {
            showError("Please enter a valid phone number");
            return false;
        }

        // Password change validation (only if current password is provided)
        if (!currentPasswordField.getText().trim().isEmpty()) {
            String newPassword = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();

            // Verify current password
            if (!userService.verifyPassword(currentUser.getUserId(), currentPasswordField.getText().trim())) {
                showError("Current password is incorrect");
                return false;
            }

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError("Please fill in all password fields");
                return false;
            }

            if (newPassword.length() < 6) {
                showError("Password must be at least 6 characters long");
                return false;
            }

            if (!newPassword.equals(confirmPassword)) {
                showError("New passwords do not match");
                return false;
            }
        }

        clearError();
        return true;
    }

    /**
     * Validates email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    /**
     * Validates phone number format
     */
    private boolean isValidPhoneNumber(String phone) {
        // Simple validation - allows numbers, spaces, +, -, and ()
        return phone.matches("^[0-9\\+\\(\\)\\-\\s]*$");
    }

    /**
     * Displays an error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Clears the error message
     */
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Handles changing the profile picture
     */
    @FXML
    public void changeProfilePicture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        Window stage = profileImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Create the users directory if it doesn't exist
                File usersDir = new File("src/main/resources/com/group4/assets/images/users/");
                if (!usersDir.exists()) {
                    usersDir.mkdirs();
                }

                // Generate unique filename
                String fileName = "user_" + currentUser.getUserId() + "_" + System.currentTimeMillis() + "."
                        + getFileExtension(file);
                String relativePath = "images/users/" + fileName;
                File destFile = new File(usersDir, fileName);
                
                // Copy the file
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Store the relative path for database storage
                tempProfileImagePath = "/com/group4/assets/" + relativePath;

                // Update the image view
                Image image = new Image(destFile.toURI().toString());
                profileImageView.setImage(image);

            } catch (IOException e) {
                e.printStackTrace();
                showToast("Failed to update profile picture: " + e.getMessage(), "error-toast");
            }
        }
    }

    /**
     * Gets the file extension from a file
     */
    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot == -1 ? "" : name.substring(lastDot + 1);
    }
    
    /**
     * Loads the default profile image
     */
    private Image loadDefaultImage() {
        try {
            // Try to load from the resources first
            String defaultImagePath = "/com/group4/assets/images/users/default-avatar.jpg";
            InputStream defaultImageStream = getClass().getResourceAsStream(defaultImagePath);
            if (defaultImageStream != null) {
                return new Image(defaultImageStream);
            } else {
                // Fallback to file system if not found in resources
                File defaultImageFile = new File("src/main/resources/com/group4/assets/images/users/default-avatar.jpg");
                if (defaultImageFile.exists()) {
                    return new Image(defaultImageFile.toURI().toString());
                }
                System.err.println("Default avatar not found at: " + defaultImagePath);
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error loading default profile image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Saves the user profile
     */
    @FXML
    public void saveProfile() {
        if (currentUser == null) {
            showError("No user data available");
            return;
        }

        if (!validateInput()) {
            return;
        }

        try {
            // Update user details
            currentUser.setFirstName(firstNameField.getText().trim());
            currentUser.setLastName(lastNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setContactNumber(contactNumberField.getText().trim());

            // Update password if changed
            if (!currentPasswordField.getText().trim().isEmpty()) {
                currentUser.setPassword(passwordField.getText().trim());
            }

            // Update profile picture if changed, otherwise keep the existing one
            if (tempProfileImagePath != null && !tempProfileImagePath.isEmpty()) {
                currentUser.setProfilePicture(tempProfileImagePath);
            } else if (currentUser.getProfilePicture() == null || currentUser.getProfilePicture().trim().isEmpty()) {
                // If no image was selected and there's no existing image, set the default
                currentUser.setProfilePicture("/com/group4/assets/images/users/default-avatar.jpg");
            }

            // Save to database
            boolean success = userService.updateUser(currentUser);
            if (success) {
                showToast("Profile updated successfully!", "success-toast");
                // Close the window after a short delay to show the toast
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        Platform.runLater(this::closeWindow);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            } else {
                showToast("Failed to update profile. Please try again.", "error-toast");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast("An error occurred: " + e.getMessage(), "error-toast");
        }
    }

    /**
     * Closes the edit profile window
     */
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Shows a toast message to the user.
     * 
     * @param message The message to display
     * @param type The type of toast (success-toast, error-toast, etc.)
     */
    private void showToast(String message, String type) {
        // Create a new stage for the toast
        Stage toastStage = new Stage();
        toastStage.initOwner(profileImageView.getScene().getWindow());
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);
        
        // Create and style the label
        Label toastLabel = new Label(message);
        toastLabel.getStyleClass().addAll("toast", type);
        
        // Create the scene with a transparent background
        StackPane root = new StackPane(toastLabel);
        root.setStyle("-fx-background-color: transparent;");
        Scene scene = new Scene(root);
        
        // Apply the stylesheet
        try {
            scene.getStylesheets().add(getClass().getResource("/com/group4/css/styles.css").toExternalForm());
        } catch (Exception e) {
            // Fallback to inline styles if stylesheet loading fails
            toastLabel.setStyle(
                "-fx-padding: 15px;" +
                "-fx-background-radius: 5;" +
                "-fx-background-color: " + (type.equals("error-toast") ? "#ff4444" : "#4CAF50") + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 14px;"
            );
        }
        
        toastStage.setScene(scene);
        
        // Position the toast at the bottom center of the window
        Window window = profileImageView.getScene().getWindow();
        double centerX = window.getX() + (window.getWidth() / 2) - 150;
        double bottomY = window.getY() + window.getHeight() - 100;
        
        toastStage.setX(centerX);
        toastStage.setY(bottomY);
        
        // Show the toast
        toastStage.show();
        
        // Auto-close the toast after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(toastStage::close);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
