package com.group4.controllers;

import com.group4.App;
import com.group4.models.User;
import com.group4.services.AdminService;
import com.group4.services.UserService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;
import com.group4.utils.ViewManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Platform;

import com.group4.utils.ImageUtils;
import java.util.logging.Level;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the Admin Dashboard.
 */
import java.util.logging.Logger;

/**
 * Controller for the Admin Dashboard.
 */
public class AdminDashboardController {
    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> userIdColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> contactNumberColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private Button addUserButton;

    @FXML
    private Button editUserButton;

    @FXML
    private Button blockUserButton;

    @FXML
    private Button refreshButton;

    @FXML
    private ComboBox<String> roleFilterComboBox;
    
    // Profile components
    @FXML private ImageView profileImageView;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label profileFullName;
    @FXML private Label profileEmail;
    @FXML private Label profileRole;
    @FXML private ImageView profileImageLarge;
    @FXML private TabPane tabPane;

    private AdminService adminService;
    private UserService userService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private User currentUser;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        adminService = new AdminService();
        userService = new UserService();

        // Load current user data
        loadCurrentUser();

        // Initialize profile tab
        initializeProfileTab();

        // Initialize table columns using direct property references
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty());
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        contactNumberColumn.setCellValueFactory(cellData -> cellData.getValue().contactNumberProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Initialize role filter
        roleFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Admin", "Manager", "Scheduler", "Customer"));
        roleFilterComboBox.setValue("All");

        // Add listener to the role filter
        roleFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                filterUsersByRole(newValue);
            }
        });

        // Load all users
        loadAllUsers();

        // Initialize profile image
        initializeProfileImage();
    }

    /**
     * Loads all users from the database.
     */
    private void loadAllUsers() {
        TaskUtils.executeTaskWithProgress(
                adminService.getAllUsers(),
                users -> {
                    usersList.clear();
                    usersList.addAll(users);
                    filterUsersByRole(roleFilterComboBox.getValue());
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + error.getMessage()));
    }

    /**
     * Filters users by role.
     * 
     * @param role The role to filter by or "All" to show all users
     */
    private void filterUsersByRole(String role) {
        if (role == null || role.equals("All")) {
            usersTable.setItems(usersList);
            return;
        }

        ObservableList<User> filteredList = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getRole().equalsIgnoreCase(role)) {
                filteredList.add(user);
            }
        }

        usersTable.setItems(filteredList);
    }

    /**
     * Handles the add user button click.
     */
    @FXML
    private void handleAddUser() {
        showUserDialog(null);
    }

    /**
     * Handles the edit user button click.
     */
    @FXML
    private void handleEditUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to edit.");
            return;
        }

        showUserDialog(selectedUser);
    }

    /**
     * Handles the toggle user status (block/unblock) button click.
     */
    /**
     * Loads the current user's data.
     */
    private void loadCurrentUser() {
        String currentUserId = SessionManager.getInstance().getCurrentUser().getUserId();
        currentUser = userService.getUserById(currentUserId);
        if (currentUser != null) {
            updateProfileUI();
        } else {
            System.err.println("Failed to load current user with ID: " + currentUserId);
        }
    }
    
    /**
     * Initializes the profile tab with user data.
     */
    private void initializeProfileTab() {
        if (currentUser != null) {
            profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            profileEmail.setText(currentUser.getEmail());
            profileRole.setText(currentUser.getRole());
            
            // Update other profile fields if they exist
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole());
            }
        }
    }
    
    /**
     * Initializes the profile image using the centralized image loading logic.
     * This method will attempt to load the user's profile image if available,
     * and fall back to the default avatar if there are any issues.
     */
    private void initializeProfileImage() {
        try {
            String imagePath = (currentUser != null) ? currentUser.getProfilePicture() : null;
            Image image = ImageUtils.loadImage(imagePath);
            setProfileImages(image);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing profile image", e);
            // Even if there's an error, the loadImage method will return the default avatar
            setProfileImages(ImageUtils.loadDefaultAvatar());
        }
    }
    
    /**
     * Sets the profile images for both small and large image views
     */
    private void setProfileImages(Image image) {
        if (profileImageView != null) {
            profileImageView.setImage(image);
        }
        if (profileImageLarge != null) {
            profileImageLarge.setImage(image);
        }
    }
    
    // Default avatar handling is now managed by ImageUtils class
    
    /**
     * Handles profile section click to switch to the profile tab
     */
    @FXML
    private void handleProfileSectionClick() {
        try {
            logger.info("Profile section clicked. Current tabPane: " + (tabPane != null ? "found" : "null"));
            
            if (tabPane != null) {
                // Log all available tabs
                logger.info("Available tabs: " + tabPane.getTabs().size());
                for (int i = 0; i < tabPane.getTabs().size(); i++) {
                    logger.info("Tab " + i + ": " + tabPane.getTabs().get(i).getText());
                }
                
                // Find the profile tab by text
                for (Tab tab : tabPane.getTabs()) {
                    if ("My Profile".equals(tab.getText())) {
                        logger.info("Found profile tab, selecting it...");
                        Platform.runLater(() -> {
                            tabPane.getSelectionModel().select(tab);
                            logger.info("Profile tab selected");
                        });
                        return;
                    }
                }
                
                // Fallback to index if text search fails
                if (tabPane.getTabs().size() > 1) {
                    logger.info("Profile tab not found by text, trying index 1");
                    Platform.runLater(() -> {
                        tabPane.getSelectionModel().select(1);
                        logger.info("Tab at index 1 selected");
                    });
                } else {
                    logger.warning("Not enough tabs available. Total tabs: " + tabPane.getTabs().size());
                }
            } else {
                logger.warning("TabPane reference is null. Make sure fx:id='tabPane' is set in FXML.");
            }
        } catch (Exception e) {
            String errorMsg = "Error switching to profile tab: " + e.getMessage();
            logger.severe(errorMsg);
            e.printStackTrace();
            // Show error to user
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Navigation Error");
                alert.setHeaderText("Could not navigate to profile");
                alert.setContentText(errorMsg);
                alert.showAndWait();
            });
        }
    }
    
    /**
     * Handles changing the profile photo.
     */
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
        toastLabel.setWrapText(true);
        toastLabel.setMaxWidth(300);
        
        // Create the scene with a transparent background
        Scene scene = new Scene(new StackPane(toastLabel));
        scene.setFill(Color.TRANSPARENT);
        
        // Apply the stylesheet if available
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
    
    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                // Save the image and get the relative path
                String imagePath = ImageUtils.saveProfileImage(selectedFile, currentUser.getUserId());
                
                if (imagePath != null) {
                    // Update user with the new profile picture path
                    currentUser.setProfilePicture(imagePath);
                    boolean updateSuccess = userService.updateUser(currentUser);
                    
                    if (updateSuccess) {
                        // Update UI with the new image
                        Image image = ImageUtils.loadImage(imagePath);
                        setProfileImages(image);
                        
                        // Show success toast
                        showToast("Profile photo updated successfully!", "success-toast");
                        
                        // Refresh the view
                        Platform.runLater(this::updateProfileUI);
                    } else {
                        throw new IOException("Failed to update user profile in the database");
                    }
                } else {
                    showToast("Failed to process the selected image. Please try another one.", "error-toast");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error updating profile photo", e);
                showToast("Failed to update profile photo: " + e.getMessage(), "error-toast");
            }
        }
    }
    
    /**
     * Handles editing the user's profile.
     */
    @FXML
    private void handleEditProfile() {
        try {
            // Load the edit profile view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/EditProfile.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the current user data
            Object controller = loader.getController();
            if (controller instanceof EditProfileController) {
                ((EditProfileController) controller).setUserData(currentUser);
                
                // Create the scene and apply styles
                Scene scene = new Scene(root);
                
                // Apply the main stylesheet
                try {
                    String css = getClass().getResource("/com/group4/css/styles.css").toExternalForm();
                    scene.getStylesheets().add(css);
                } catch (Exception e) {
                    System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
                }
                
                // Show the edit profile dialog
                Stage stage = new Stage();
                stage.setTitle("Edit Profile");
                stage.setScene(scene);
                stage.showAndWait();
                
                // Refresh profile data after editing
                loadCurrentUser();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid profile editor implementation");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit profile: " + e.getMessage());
        }
    }
    
    /**
     * Updates the profile UI with current user data.
     */
    private void updateProfileUI() {
        if (currentUser != null) {
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole());
            }
            if (profileFullName != null) {
                profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (profileEmail != null) {
                profileEmail.setText(currentUser.getEmail());
            }
            if (profileRole != null) {
                profileRole.setText(currentUser.getRole());
            }
            
            // Update profile image
            initializeProfileImage();
        }
    }
    
    @FXML
    private void handleToggleUserStatus() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to block/unblock.");
            return;
        }

        String currentStatus = selectedUser.getStatus();
        String newStatus = currentStatus.equals("ACTIVE") ? "BLOCKED" : "ACTIVE";
        String actionText = currentStatus.equals("ACTIVE") ? "block" : "unblock";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Status Change");
        confirm.setHeaderText("Are you sure you want to " + actionText + " this user?");
        confirm.setContentText("User: " + selectedUser.getFirstName() + " " + selectedUser.getLastName() + " ("
                + selectedUser.getEmail() + ")");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            TaskUtils.executeTaskWithProgress(
                    adminService.updateUserStatus(selectedUser.getUserId(), newStatus),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "User status updated successfully.");
                            loadAllUsers();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update user status.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to update user status: " + error.getMessage()));
        }
    }

    /**
     * Handles the refresh button click.
     */
    @FXML
    private void handleRefresh() {
        loadAllUsers();
    }

    /**
     * Handles the logout button click.
     */
    @FXML
    private void handleLogout() {
        try {
            // Clear the current user session
            SessionManager.getInstance().logout();

            // Load the login screen using ViewManager
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
            Parent root = loader.load();
            ViewManager.switchView(root);
        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Logout Failed");
            alert.setContentText("An error occurred while trying to log out: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Shows the user dialog for adding or editing a user.
     * 
     * @param user The user to edit, or null to add a new user
     */
    private void showUserDialog(User user) {
        // Create the dialog
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle(user == null ? "Add User" : "Edit User");
        dialog.setHeaderText(user == null ? "Create a new user" : "Edit user information");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField contactNumberField = new TextField();
        contactNumberField.setPromptText("Contact Number");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Admin", "Manager", "Scheduler", "Customer"));
        roleComboBox.setValue("Customer");

        // If editing a user, populate the fields
        if (user != null) {
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailField.setText(user.getEmail());
            contactNumberField.setText(user.getContactNumber());
            roleComboBox.setValue(user.getRole());
        }

        // Add fields to the grid
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Contact Number:"), 0, 4);
        grid.add(contactNumberField, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleComboBox, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a user when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate fields
                if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                        emailField.getText().isEmpty() || contactNumberField.getText().isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "All fields must be filled in.");
                    return null;
                }

                if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
                    return null;
                }

                // Create or update the user
                User newUser = user == null ? new User() : user;
                newUser.setFirstName(firstNameField.getText());
                newUser.setLastName(lastNameField.getText());
                newUser.setEmail(emailField.getText());
                if (!passwordField.getText().isEmpty()) {
                    newUser.setPassword(passwordField.getText());
                }
                newUser.setContactNumber(contactNumberField.getText());
                newUser.setRole(roleComboBox.getValue());
                newUser.setStatus("ACTIVE");

                return newUser;
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(updatedUser -> {
            TaskUtils.executeTaskWithProgress(
                    user == null ? adminService.createUser(updatedUser) : adminService.updateUser(updatedUser),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success",
                                    user == null ? "User created successfully." : "User updated successfully.");
                            loadAllUsers();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    user == null ? "Failed to create user." : "Failed to update user.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error",
                            (user == null ? "Failed to create user: " : "Failed to update user: ")
                                    + error.getMessage()));
        });
    }

    /**
     * Shows an alert dialog.
     * 
     * @param alertType the type of alert
     * @param title     the alert title
     * @param message   the alert message
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}