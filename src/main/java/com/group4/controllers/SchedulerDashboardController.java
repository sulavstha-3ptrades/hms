package com.group4.controllers;

import com.group4.utils.SessionManager;
import com.group4.utils.ViewManager;
import com.group4.models.User;
import com.group4.services.UserService;
import com.group4.utils.AlertUtils;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/**
 * Controller for the Scheduler Dashboard with tabbed interface.
 */
public class SchedulerDashboardController {

    @FXML
    private TabPane mainTabPane;
    
    @FXML
    private Tab dashboardTab;
    
    @FXML
    private Tab reportsTab;
    
    @FXML
    private Button logoutButton;
    
    // Profile Section
    @FXML
    private ImageView profileImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    
    // Profile Tab
    @FXML
    private ImageView profileImageLarge;
    @FXML
    private Label profileFullName;
    @FXML
    private Label profileEmail;
    @FXML
    private Label profileRole;
    @FXML
    private Label profileContact;
    @FXML
    private Label profileFirstName;
    @FXML
    private Label profileLastName;
    @FXML
    private Label profileContactField;
    @FXML
    private Label profileCreatedDate;
    
    private User currentUser;
    private UserService userService;
    private File selectedImageFile;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        try {
            userService = new UserService();
            loadUserData();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to initialize dashboard", e.getMessage());
        }
    }
    
    private void loadUserData() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Update header
            String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
            userNameLabel.setText(fullName);
            userRoleLabel.setText(currentUser.getRole().toString());
            
            // Update profile tab
            profileFullName.setText(fullName);
            profileEmail.setText(currentUser.getEmail());
            profileRole.setText(currentUser.getRole().toString());
            profileContact.setText(currentUser.getContactNumber());
            
            // Update account information
            profileFirstName.setText(currentUser.getFirstName());
            profileLastName.setText(currentUser.getLastName());
            profileContactField.setText(currentUser.getContactNumber());
            profileCreatedDate.setText("2023-01-01"); // TODO: Add created date to User model
            
            // Load profile image if exists
            loadProfileImage();
        }
    }
    
    private void loadProfileImage() {
        if (currentUser != null) {
            try {
                String profilePicturePath = currentUser.getProfilePicture();
                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    // If it's a base64 encoded image
                    if (profilePicturePath.startsWith("data:image")) {
                        String base64Image = profilePicturePath.split(",")[1];
                        byte[] imageData = Base64.getDecoder().decode(base64Image);
                        Image image = new Image(new ByteArrayInputStream(imageData));
                        profileImageView.setImage(image);
                        profileImageLarge.setImage(image);
                    } else {
                        // If it's a file path
                        Image image = new Image(profilePicturePath);
                        profileImageView.setImage(image);
                        profileImageLarge.setImage(image);
                    }
                } else {
                    // Load default avatar
                    Image defaultImage = new Image("/com/group4/assets/images/users/default-avatar.jpg");
                    profileImageView.setImage(defaultImage);
                    profileImageLarge.setImage(defaultImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading profile image: " + e.getMessage());
                // Set default avatar on error
                Image defaultImage = new Image("/com/group4/assets/images/users/default-avatar.jpg");
                profileImageView.setImage(defaultImage);
                profileImageLarge.setImage(defaultImage);
            }
        }
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleProfileSectionClick() {
        try {
            // Switch to the profile tab when clicking on the profile section
            if (mainTabPane != null) {
                mainTabPane.getSelectionModel().select(mainTabPane.getTabs().size() - 1); // Select profile tab (last tab)
            } else {
                System.err.println("Error: mainTabPane is not initialized");
            }
        } catch (Exception e) {
            System.err.println("Error switching to profile tab: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/EditProfile.fxml"));
            Parent root = loader.load();
            
            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);
            
            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
            
            // Reload user data after editing
            loadUserData();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to load edit profile form", e.getMessage());
        }
    }
    
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) profileImageLarge.getScene().getWindow();
        selectedImageFile = fileChooser.showOpenDialog(stage);
        
        if (selectedImageFile != null) {
            try {
                // Load and resize the image
                BufferedImage bufferedImage = ImageIO.read(selectedImageFile);
                Image image = SwingFXUtils.toFXImage(bufferedImage, null);
                
                // Update both image views
                profileImageView.setImage(image);
                profileImageLarge.setImage(image);
                
                // Save the image to the user's profile
                saveProfileImage(selectedImageFile);
                
            } catch (IOException e) {
                AlertUtils.showError("Image Error", "Failed to load image", e.getMessage());
            }
        }
    }
    
    private void saveProfileImage(File imageFile) {
        try {
            // Convert image to base64
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
            
            // Update user's profile picture
            currentUser.setProfilePicture("data:image/png;base64," + base64Image);
            userService.updateUser(currentUser);
            
            // Update session
            SessionManager.getInstance().setCurrentUser(currentUser);
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to save profile image", e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Clear the current user session
            SessionManager.getInstance().logout();

            // Load the login screen using ViewManager
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Error", "Failed to logout", e.getMessage());
        }
    }
}