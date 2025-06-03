package com.group4.controllers;

import com.group4.App;
import com.group4.models.Hall;
import com.group4.models.HallAvailability;
import com.group4.models.Maintenance;
import com.group4.models.User;
import com.group4.services.AdminService;
import com.group4.services.HallAvailabilityService;
import com.group4.services.HallService;
import com.group4.services.MaintenanceService;
import com.group4.services.UserService;
import com.group4.services.BookingService;
import com.group4.models.Booking;
import com.group4.models.BookingStatus;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;
import com.group4.utils.ViewManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.control.TableCell;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.PasswordField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ButtonBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;

import com.group4.utils.ImageUtils;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for the Admin Dashboard.
 */
public class AdminDashboardController {
    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());

    // Booking History Tab Components
    @FXML
    private TableView<Booking> bookingsTable;
    
    // Filter fields
    @FXML
    private TextField hallIdFilter;
    
    @FXML
    private TextField customerIdFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private TableColumn<Booking, String> bookingIdColumn;
    @FXML
    private TableColumn<Booking, String> bookingCustomerIdColumn;
    @FXML
    private TableColumn<Booking, String> bookingHallIdColumn;
    @FXML
    private TableColumn<Booking, String> bookingStartDateColumn;
    @FXML
    private TableColumn<Booking, String> bookingEndDateColumn;
    @FXML
    private TableColumn<Booking, Double> bookingTotalCostColumn;
    @FXML
    private TableColumn<Booking, BookingStatus> bookingStatusColumn;
    @FXML
    private Button refreshBookingsButton;

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

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private TextField nameSearchField;

    @FXML
    private TextField emailSearchField;

    @FXML
    private TextField contactSearchField;

    @FXML
    private Button searchButton;

    // Profile components
    @FXML
    private ImageView profileImageView;
    @FXML
    private ImageView profileImageLarge;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label profileFullName;
    @FXML
    private Label profileEmail;
    @FXML
    private Label profileRole;
    @FXML
    private Label profileContact;
    @FXML
    private Label profileUsername;
    @FXML
    private Label profileEmailField;
    @FXML
    private Label profileContactField;
    @FXML
    private TabPane tabPane;

    // Hall Management components
    @FXML
    private TableView<Hall> hallsTable;
    @FXML
    private TableColumn<Hall, String> hallIdColumn;
    @FXML
    private TableColumn<Hall, String> hallTypeColumn;
    @FXML
    private TableColumn<Hall, Integer> capacityColumn;
    @FXML
    private TableColumn<Hall, Double> rateColumn;
    @FXML
    private Button addHallButton;
    @FXML
    private Button editHallButton;
    @FXML
    private Button deleteHallButton;
    @FXML
    private Button setAvailabilityButton;
    @FXML
    private Button quickMaintenanceButton;

    // Maintenance components
    @FXML
    private TableView<Maintenance> maintenanceTable;
    @FXML
    private TableColumn<Maintenance, String> maintenanceIdColumn;
    @FXML
    private TableColumn<Maintenance, String> maintenanceHallIdColumn;
    @FXML
    private TableColumn<Maintenance, String> maintenanceDescriptionColumn;
    @FXML
    private TableColumn<Maintenance, String> maintenanceStartColumn;
    @FXML
    private TableColumn<Maintenance, String> maintenanceEndColumn;
    @FXML
    private Button addMaintenanceButton;
    @FXML
    private Button editMaintenanceButton;
    @FXML
    private Button deleteMaintenanceButton;

    private AdminService adminService;
    private UserService userService;
    private HallService hallService;
    private MaintenanceService maintenanceService;
    private HallAvailabilityService hallAvailabilityService;
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Hall> hallsList = FXCollections.observableArrayList();
    private ObservableList<Maintenance> maintenanceList = FXCollections.observableArrayList();
    private User currentUser;

    /**
     * Initializes the controller.
     */
    @FXML
    private void initialize() {
        adminService = new AdminService();
        userService = new UserService();
        hallService = new HallService();
        maintenanceService = new MaintenanceService();
        hallAvailabilityService = new HallAvailabilityService();
        currentUser = SessionManager.getInstance().getCurrentUser();

        // Initialize all tabs
        initializeUserManagementTab();
        initializeHallManagementTab();
        initializeProfileTab();
        initializeBookingHistoryTab();
        loadAllUsers();
        loadAllHalls();
        loadAllMaintenance();
        loadAllBookings();
        
        // Initialize profile image
        initializeProfileImage();
    }

    /**
     * Initializes the user management tab.
     */
    private void initializeUserManagementTab() {
        // Initialize table columns using direct property references
        userIdColumn.setCellValueFactory(cellData -> cellData.getValue().userIdProperty());
        firstNameColumn.setCellValueFactory(cellData -> cellData.getValue().firstNameProperty());
        lastNameColumn.setCellValueFactory(cellData -> cellData.getValue().lastNameProperty());
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        contactNumberColumn.setCellValueFactory(cellData -> cellData.getValue().contactNumberProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Custom cell factory for status column
        statusColumn.setCellFactory(column -> new TableCell<User, String>() {
            private final HBox container = new HBox(5);
            private final Circle statusDot = new Circle(5);
            private final Label statusLabel = new Label();

            {
                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(statusDot, statusLabel);
                statusLabel.setStyle("-fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    boolean isActive = "Active".equalsIgnoreCase(status);
                    String color = isActive ? "#2ecc71" : "#e74c3c";

                    statusDot.setFill(Color.web(color));
                    statusLabel.setText(status);
                    statusLabel.setTextFill(Color.web(color));

                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Initialize role filter
        roleFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Manager", "Scheduler", "Customer"));
        roleFilterComboBox.setValue("All");

        // Initialize status filter
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                "All", "Active", "Block"));
        statusFilterComboBox.setValue("All");

        // Add listeners to filters
        roleFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        statusFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        // Add search button handler
        searchButton.setOnAction(event -> applyFilters());

        // Add Enter key listener to search fields
        nameSearchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                applyFilters();
            }
        });

        emailSearchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                applyFilters();
            }
        });

        contactSearchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                applyFilters();
            }
        });

    }

    /**
     * Initializes the hall management tab.
     */
    private void initializeHallManagementTab() {
        // Initialize hall table columns
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        hallTypeColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        rateColumn.setCellValueFactory(cellData -> cellData.getValue().ratePerHourProperty().asObject());

        // Set hall table data source
        hallsTable.setItems(hallsList);

        // Add hall table selection listener
        hallsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean hallSelected = newValue != null;
                    editHallButton.setDisable(!hallSelected);
                    deleteHallButton.setDisable(!hallSelected);
                    setAvailabilityButton.setDisable(!hallSelected);
                    quickMaintenanceButton.setDisable(!hallSelected);
                });

        // Initialize maintenance table columns
        maintenanceIdColumn.setCellValueFactory(cellData -> cellData.getValue().maintenanceIdProperty());
        maintenanceHallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        maintenanceDescriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());

        // Format date-time columns
        maintenanceStartColumn.setCellValueFactory(cellData -> {
            LocalDateTime startTime = cellData.getValue().getStartTime();
            return new SimpleStringProperty(startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        maintenanceEndColumn.setCellValueFactory(cellData -> {
            LocalDateTime endTime = cellData.getValue().getEndTime();
            return new SimpleStringProperty(endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });

        // Set maintenance table data source
        maintenanceTable.setItems(maintenanceList);

        // Add maintenance table selection listener
        maintenanceTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    boolean maintenanceSelected = newValue != null;
                    editMaintenanceButton.setDisable(!maintenanceSelected);
                    deleteMaintenanceButton.setDisable(!maintenanceSelected);
                });

        // Disable buttons initially
        editHallButton.setDisable(true);
        deleteHallButton.setDisable(true);
        setAvailabilityButton.setDisable(true);
        quickMaintenanceButton.setDisable(true);
        editMaintenanceButton.setDisable(true);
        deleteMaintenanceButton.setDisable(true);
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
                    applyFilters();
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load users: " + error.getMessage()));
    }

    /**
     * Loads all halls from the database.
     */
    private void loadAllHalls() {
        TaskUtils.executeTaskWithProgress(
                hallService.getAllHalls(),
                halls -> {
                    hallsList.clear();
                    hallsList.addAll(halls);
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load halls: " + error.getMessage()));
    }

    /**
     * Loads all maintenance schedules from the database.
     */
    private void loadAllMaintenance() {
        TaskUtils.executeTaskWithProgress(
                maintenanceService.getAllMaintenanceSchedules(),
                maintenance -> {
                    maintenanceList.clear();
                    maintenanceList.addAll(maintenance);
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to load maintenance schedules: " + error.getMessage()));
    }

    /**
     * Applies all active filters to the users table.
     * Filters users based on role, status, name, email, and contact number.
     */
    private void applyFilters() {
        String roleFilter = roleFilterComboBox.getValue();
        String statusFilter = statusFilterComboBox.getValue();
        String nameSearch = nameSearchField.getText().toLowerCase();
        String emailSearch = emailSearchField.getText().toLowerCase();
        String contactSearch = contactSearchField.getText().toLowerCase();

        ObservableList<User> filteredList = FXCollections.observableArrayList();

        for (User user : usersList) {
            // Skip admin users
            if ("Admin".equalsIgnoreCase(user.getRole())) {
                continue;
            }

            // Apply role filter
            if (roleFilter != null && !roleFilter.equals("All") && !user.getRole().equals(roleFilter)) {
                continue;
            }

            // Apply status filter
            if (statusFilter != null && !statusFilter.equals("All")) {
                String userStatus = user.getStatus() != null ? user.getStatus().toUpperCase() : "";

                if (statusFilter.equalsIgnoreCase("Active") && !"ACTIVE".equals(userStatus)) {
                    continue;
                }
                if (statusFilter.equalsIgnoreCase("Block") && !"BLOCKED".equals(userStatus)) {
                    continue;
                }
            }

            // Apply name search
            if (!nameSearch.isEmpty()) {
                String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                if (!fullName.contains(nameSearch)) {
                    continue;
                }
            }

            // Apply email search
            if (!emailSearch.isEmpty() && !user.getEmail().toLowerCase().contains(emailSearch)) {
                continue;
            }

            // Apply contact search
            if (!contactSearch.isEmpty() && !user.getContactNumber().toLowerCase().contains(contactSearch)) {
                continue;
            }

            filteredList.add(user);
        }

        usersTable.setItems(filteredList);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
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
            // Force reload the current user data to get the latest profile picture
            if (currentUser != null) {
                currentUser = userService.getUserById(currentUser.getUserId());
            }

            String imagePath = (currentUser != null) ? currentUser.getProfilePicture() : null;
            logger.info("Loading profile image from path: " + imagePath);

            // Clear the image cache first
            if (profileImageView != null) {
                profileImageView.setImage(null);
            }

            // Load the new image
            Image image = ImageUtils.loadImage(imagePath);
            setProfileImages(image);

            // Force a UI refresh
            if (profileImageView != null) {
                profileImageView.setImage(image);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing profile image", e);
            // Even if there's an error, the loadImage method will return the default avatar
            Image defaultAvatar = ImageUtils.loadDefaultAvatar();
            setProfileImages(defaultAvatar);
            if (profileImageView != null) {
                profileImageView.setImage(defaultAvatar);
            }
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
     * @param type    The type of toast (success-toast, error-toast, etc.)
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
                            "-fx-font-size: 14px;");
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
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

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
            // Store the current user data before showing the dialog
            User originalUser = new User();
            originalUser.setUserId(currentUser.getUserId());
            originalUser.setFirstName(currentUser.getFirstName());
            originalUser.setLastName(currentUser.getLastName());
            originalUser.setEmail(currentUser.getEmail());
            originalUser.setContactNumber(currentUser.getContactNumber());
            originalUser.setRole(currentUser.getRole());
            originalUser.setStatus(currentUser.getStatus());
            originalUser.setProfilePicture(currentUser.getProfilePicture());

            // Load the edit profile view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/EditProfile.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current user data
            Object controller = loader.getController();
            if (controller instanceof EditProfileController) {
                EditProfileController editController = (EditProfileController) controller;
                editController.setUserData(currentUser);

                // Create and show the dialog
                Stage stage = new Stage();
                stage.setTitle("Edit Profile");
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);

                // Show the dialog and wait for it to close
                stage.showAndWait();

                // After dialog is closed, refresh the current user data
                User updatedUser = userService.getUserById(currentUser.getUserId());
                if (updatedUser != null) {
                    // Check if any profile data has changed
                    boolean profileChanged = !originalUser.equals(updatedUser);

                    if (profileChanged) {
                        // Update the current user reference
                        currentUser = updatedUser;

                        // Update the session user if it's the same user
                        if (SessionManager.getInstance().getCurrentUser().getUserId().equals(currentUser.getUserId())) {
                            SessionManager.getInstance().setCurrentUser(currentUser);
                        }

                        // Force update the UI
                        Platform.runLater(() -> {
                            try {
                                // Clear existing images
                                if (profileImageView != null) {
                                    profileImageView.setImage(null);
                                }
                                if (profileImageLarge != null) {
                                    profileImageLarge.setImage(null);
                                }

                                // Force garbage collection
                                System.gc();

                                // Add a small delay to ensure the UI has time to update
                                new java.util.Timer().schedule(
                                        new java.util.TimerTask() {
                                            @Override
                                            public void run() {
                                                Platform.runLater(() -> {
                                                    try {
                                                        // Reload the profile image with cache busting
                                                        String imagePath = currentUser.getProfilePicture();
                                                        String timestamp = "?t=" + System.currentTimeMillis();
                                                        logger.info("Reloading profile image from: " + imagePath
                                                                + timestamp);

                                                        // Load the image
                                                        Image newImage = ImageUtils.loadImage(imagePath);

                                                        // Update UI elements
                                                        updateProfileUI();

                                                        // Set the new images after UI is updated
                                                        if (profileImageView != null) {
                                                            profileImageView.setImage(newImage);
                                                        }
                                                        if (profileImageLarge != null) {
                                                            profileImageLarge.setImage(newImage);
                                                        }

                                                        logger.info("Profile UI refreshed successfully");

                                                        // Show success message
                                                        showToast("Profile updated successfully!", "success-toast");

                                                    } catch (Exception e) {
                                                        logger.log(Level.SEVERE, "Error updating profile UI:", e);
                                                        // Fall back to default avatar on error
                                                        Image defaultAvatar = ImageUtils.loadDefaultAvatar();
                                                        if (profileImageView != null)
                                                            profileImageView.setImage(defaultAvatar);
                                                        if (profileImageLarge != null)
                                                            profileImageLarge.setImage(defaultAvatar);

                                                        showToast("Error updating profile: " + e.getMessage(),
                                                                "error-toast");
                                                    }
                                                });
                                            }
                                        },
                                        100 // 100ms delay
                                );

                            } catch (Exception e) {
                                logger.log(Level.SEVERE, "Error in profile update handler:", e);
                                showToast("Error updating profile: " + e.getMessage(), "error-toast");
                            }
                        });
                    }
                } else {
                    logger.warning("Failed to load updated user data");
                    showToast("Failed to load updated profile data", "error-toast");
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error opening edit profile dialog:", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit profile: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in handleEditProfile:", e);
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Updates the profile UI with current user data.
     */
    private void updateProfileUI() {
        if (currentUser != null) {
            // Update header labels
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(currentUser.getRole());
            }

            // Update profile tab fields
            if (profileFullName != null) {
                profileFullName.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (profileEmail != null) {
                profileEmail.setText(currentUser.getEmail());
                if (profileEmailField != null) {
                    profileEmailField.setText(currentUser.getEmail());
                }
            }
            if (profileRole != null) {
                profileRole.setText(currentUser.getRole());
            }
            if (profileContact != null) {
                profileContact.setText(currentUser.getContactNumber());
                if (profileContactField != null) {
                    profileContactField.setText(currentUser.getContactNumber());
                }
            }

            // Update profile images
            Platform.runLater(() -> {
                try {
                    // Reload the user to get the latest data
                    currentUser = userService.getUserById(currentUser.getUserId());

                    // Load and set the profile images
                    String imagePath = currentUser.getProfilePicture();
                    logger.info("Updating profile UI with image path: " + imagePath);

                    // Clear existing images
                    if (profileImageView != null) {
                        profileImageView.setImage(null);
                    }
                    if (profileImageLarge != null) {
                        profileImageLarge.setImage(null);
                    }

                    // Load and set new images
                    Image image = ImageUtils.loadImage(imagePath);
                    if (profileImageView != null) {
                        profileImageView.setImage(image);
                    }
                    if (profileImageLarge != null) {
                        profileImageLarge.setImage(image);
                    }

                    logger.info("Profile images updated successfully");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error updating profile UI:", e);
                    // Fall back to default avatar if there's an error
                    Image defaultAvatar = ImageUtils.loadDefaultAvatar();
                    if (profileImageView != null)
                        profileImageView.setImage(defaultAvatar);
                    if (profileImageLarge != null)
                        profileImageLarge.setImage(defaultAvatar);
                }
            });
        } else {
            logger.warning("Current user is null in updateProfileUI");
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
        String newStatus = "Active".equals(currentStatus) ? "Block" : "Active";
        String actionText = "Active".equals(currentStatus) ? "block" : "unblock";

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
        // Reset all filters
        roleFilterComboBox.setValue("All");
        statusFilterComboBox.setValue("All");
        nameSearchField.clear();
        emailSearchField.clear();
        contactSearchField.clear();

        // Reload all users
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
     * Handles the add hall button click.
     */
    @FXML
    private void handleAddHall() {
        try {
            // Load the add hall form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/AddHallForm.fxml"));
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add New Hall");
            dialog.setDialogPane(loader.load());

            // Get the controller
            AddHallFormController controller = loader.getController();

            // Get the dialog pane and buttons
            DialogPane dialogPane = dialog.getDialogPane();
            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // Get the save button and disable it initially
            Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
            saveButton.setDisable(true);

            // Add validation listeners to enable/disable save button
            Runnable validateForm = () -> {
                boolean isValid = controller.isFormValid();
                saveButton.setDisable(!isValid);
            };

            controller.getHallTypeComboBox().valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
            controller.getCapacityField().textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
            controller.getRatePerHourField().textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());

            // Initial validation
            validateForm.run();

            // Show the dialog and process the result
            Optional<ButtonType> result = dialog.showAndWait();

            result.ifPresent(buttonType -> {
                if (buttonType != saveButtonType) {
                    return;
                }

                Hall newHall = controller.getHall();
                if (newHall == null) {
                    return;
                }
                if (newHall != null) {
                    // Create a task to add the hall
                    Task<Hall> addHallTask = hallService.addHall(
                            newHall.getType(),
                            newHall.getCapacity(),
                            newHall.getRatePerHour());

                    // Handle successful addition
                    addHallTask.setOnSucceeded(event -> {
                        Hall addedHall = addHallTask.getValue();
                        if (addedHall != null) {
                            Platform.runLater(() -> {
                                showToast("Hall added successfully!", "success-toast");
                                // Add the new hall to the table
                                hallsList.add(addedHall);
                                // Sort the list to maintain order
                                hallsList.sort(Comparator.comparing(Hall::getHallId));
                                // Refresh the table
                                hallsTable.refresh();
                            });
                        } else {
                            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error",
                                    "Failed to add hall. The hall may already exist."));
                        }
                    });

                    // Handle addition failure
                    addHallTask.setOnFailed(event -> {
                        Platform.runLater(() -> {
                            Throwable ex = addHallTask.getException();
                            String errorMessage = ex != null ? ex.getMessage() : "Unknown error";
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Error adding hall: " + errorMessage);
                        });
                    });

                    // Start the task in a new thread
                    new Thread(addHallTask, "Add-Hall-Task").start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error loading add hall form: " + e.getMessage());
        }
    }

    /**
     * Handles the edit hall button click.
     */
    @FXML
    private void handleEditHall() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a hall to edit.");
            return;
        }

        try {
            // Create and configure the dialog first
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Hall");

            // Load the AddHallForm FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/AddHallForm.fxml"));
            Parent root = loader.load();

            // Set the content in the dialog pane
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Get the controller and initialize it with the selected hall's data
            // This is done after the dialog pane is set up
            AddHallFormController controller = loader.getController();
            controller.initEditMode(selectedHall);

            // Set the dialog in the controller if needed
            try {
                Method setDialogMethod = controller.getClass().getMethod("setDialog", Dialog.class);
                setDialogMethod.invoke(controller, dialog);
            } catch (Exception e) {
                // Method not found or invocation failed, which is fine
                logger.log(Level.WARNING, "Could not set dialog on controller", e);
            }

            // Set the result converter to return the hall if the form is valid
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK && controller.isFormValid()) {
                    return buttonType;
                }
                return null;
            });

            // Show the dialog and wait for user action
            Optional<ButtonType> result = dialog.showAndWait();

            // Handle the result
            result.ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    // Get the updated hall data from the form
                    Hall updatedHall = controller.getHall();
                    if (updatedHall != null) {
                        // Update the hall in the database
                        Task<Boolean> updateTask = hallService.updateHall(updatedHall);
                        updateTask.setOnSucceeded(e -> {
                            if (updateTask.getValue()) {
                                // Refresh the halls table
                                loadAllHalls();
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Hall updated successfully!");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update hall. Please try again.");
                            }
                        });
                        updateTask.setOnFailed(e -> {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update hall: " +
                                    (updateTask.getException() != null ? updateTask.getException().getMessage()
                                            : "Unknown error"));
                        });
                        new Thread(updateTask).start();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Error opening edit form: " + e.getMessage());
        }
    }

    /**
     * Handles the delete hall button click.
     */
    @FXML
    private void handleDeleteHall() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a hall to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Are you sure you want to delete this hall?");
        confirm.setContentText("Hall ID: " + selectedHall.getHallId() + "\nType: " + selectedHall.getType());

        if (confirm.showAndWait().get() == ButtonType.OK) {
            TaskUtils.executeTaskWithProgress(
                    hallService.deleteHall(selectedHall.getHallId()),
                    success -> {
                        if (success) {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Hall deleted successfully.");
                                loadAllHalls();
                            });
                        } else {
                            Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete hall.");
                            });
                        }
                    },
                    error -> Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Error deleting hall: " + error.getMessage());
                    }));
        }
    }

    /**
     * Handles the set availability button click.
     */
    @FXML
    private void handleSetAvailability() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a hall to set availability.");
            return;
        }

        showAvailabilityDialog(selectedHall);
    }

    /**
     * Shows the availability dialog for a hall.
     * 
     * @param hall The hall to set availability for
     */
    private void showAvailabilityDialog(Hall hall) {
        // Create the dialog
        Dialog<HallAvailability> dialog = new Dialog<>();
        dialog.setTitle("Set Hall Availability");
        dialog.setHeaderText("Set availability for hall: " + hall.getHallId());

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Create date-time pickers for start and end times
        DatePicker startDatePicker = new DatePicker(LocalDate.now());
        ComboBox<String> startHourComboBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            startHourComboBox.getItems().add(String.format("%02d:00", i));
        }
        startHourComboBox.setValue("09:00");

        DatePicker endDatePicker = new DatePicker(LocalDate.now());
        ComboBox<String> endHourComboBox = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            endHourComboBox.getItems().add(String.format("%02d:00", i));
        }
        endHourComboBox.setValue("17:00");

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("AVAILABLE", "UNAVAILABLE");
        statusComboBox.setValue("AVAILABLE");

        // Add fields to the grid
        grid.add(new Label("Start Date:"), 0, 0);
        grid.add(startDatePicker, 1, 0);
        grid.add(new Label("Start Time:"), 0, 1);
        grid.add(startHourComboBox, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);
        grid.add(new Label("End Time:"), 0, 3);
        grid.add(endHourComboBox, 1, 3);
        grid.add(new Label("Status:"), 0, 4);
        grid.add(statusComboBox, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to an availability record when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Parse the date and time values
                    LocalDate startDate = startDatePicker.getValue();
                    String[] startTimeParts = startHourComboBox.getValue().split(":");
                    LocalDateTime startDateTime = startDate.atTime(Integer.parseInt(startTimeParts[0]), 0);

                    LocalDate endDate = endDatePicker.getValue();
                    String[] endTimeParts = endHourComboBox.getValue().split(":");
                    LocalDateTime endDateTime = endDate.atTime(Integer.parseInt(endTimeParts[0]), 0);

                    // Validate that end time is after start time
                    if (!endDateTime.isAfter(startDateTime)) {
                        showAlert(Alert.AlertType.WARNING, "Invalid Time Range", "End time must be after start time.");
                        return null;
                    }

                    // Create the availability record
                    return new HallAvailability("", hall.getHallId(), startDateTime, endDateTime,
                            statusComboBox.getValue());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter valid date and time values.");
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        Optional<HallAvailability> result = dialog.showAndWait();
        result.ifPresent(availability -> {
            TaskUtils.executeTaskWithProgress(
                    hallAvailabilityService.addAvailability(
                            availability.getHallId(),
                            availability.getStartTime(),
                            availability.getEndTime(),
                            availability.getStatus()),
                    newAvailability -> {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Hall availability set successfully.");
                        });
                    },
                    error -> Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Error setting hall availability: " + error.getMessage());
                    }));
        });
    }

    /**
     * Handles the add maintenance button click.
     */
    @FXML
    private void handleAddMaintenance() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a hall to schedule maintenance.");
            return;
        }

        showMaintenanceDialog(selectedHall, null);
    }

    /**
     * Handles the edit maintenance button click.
     */
    @FXML
    private void handleEditMaintenance() {
        Maintenance selectedMaintenance = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selectedMaintenance == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a maintenance schedule to edit.");
            return;
        }

        // Find the hall for this maintenance
        TaskUtils.executeTaskWithProgress(
                hallService.getHallById(selectedMaintenance.getHallId()),
                hall -> {
                    if (hall != null) {
                        Platform.runLater(() -> showMaintenanceDialog(hall, selectedMaintenance));
                    } else {
                        Platform.runLater(() -> {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Could not find the hall for this maintenance schedule.");
                        });
                    }
                },
                error -> Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error", "Error loading hall: " + error.getMessage());
                }));
    }

    /**
     * Handles the delete user button click.
     * Deletes the currently selected user after confirmation.
     */
    @FXML
    private void handleDeleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a user to delete.");
            return;
        }

        // Prevent deleting the current user
        if (selectedUser.getUserId().equals(currentUser.getUserId())) {
            showAlert(Alert.AlertType.WARNING, "Cannot Delete", "You cannot delete your own account while logged in.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete user: " +
                selectedUser.getFirstName() + " " + selectedUser.getLastName() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = adminService.deleteUser(selectedUser.getUserId());

            deleteTask.setOnSucceeded(e -> {
                if (Boolean.TRUE.equals(deleteTask.getValue())) {
                    Platform.runLater(() -> {
                        usersList.remove(selectedUser);
                        showToast("User deleted successfully.", "success-toast");
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to delete user. The user may have associated records.");
                    });
                }
            });

            deleteTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "An error occurred while deleting the user.");
                });
                logger.log(Level.SEVERE, "Error deleting user", deleteTask.getException());
            });

            new Thread(deleteTask).start();
        }
    }

    /**
     * Handles the delete maintenance button click.
     * Deletes the selected maintenance schedule after confirmation.
     */
    @FXML
    private void handleDeleteMaintenance() {
        Maintenance selectedMaintenance = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selectedMaintenance == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a maintenance schedule to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Maintenance Schedule");
        alert.setContentText("Are you sure you want to delete the maintenance schedule for " +
                selectedMaintenance.getHallId() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Boolean> deleteTask = maintenanceService.deleteMaintenance(selectedMaintenance.getMaintenanceId());

            deleteTask.setOnSucceeded(e -> {
                if (Boolean.TRUE.equals(deleteTask.getValue())) {
                    Platform.runLater(() -> {
                        maintenanceList.remove(selectedMaintenance);
                        showToast("Maintenance schedule deleted successfully.", "success-toast");
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to delete maintenance schedule. It may be referenced by other records.");
                    });
                }
            });

            deleteTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "An error occurred while deleting the maintenance schedule.");
                });
                logger.log(Level.SEVERE, "Error deleting maintenance schedule", deleteTask.getException());
            });

            new Thread(deleteTask).start();
        }
    }

    /**
     * Shows the maintenance dialog for a hall.
     * 
     * @param hall        The hall to schedule maintenance for
     * @param maintenance The maintenance schedule to edit, or null to add a new one
     */
    private void showMaintenanceDialog(Hall hall, Maintenance maintenance) {
        boolean isEditMode = maintenance != null;
        // Create a final reference that can be used in the lambda
        final Maintenance[] finalMaintenanceRef = { maintenance };

        // Create the dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEditMode ? "Edit Maintenance Schedule" : "Schedule Maintenance");
        dialog.setHeaderText(
                (isEditMode ? "Edit maintenance for" : "Schedule maintenance for") + " hall: " + hall.getHallId());

        try {
            // Load the FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/MaintenanceForm.fxml"));
            Parent root = loader.load();

            // Get the controller and set the hall
            MaintenanceFormController controller = loader.getController();
            controller.setHall(hall);

            // If editing, set the maintenance data
            if (isEditMode) {
                controller.initializeWithMaintenance(maintenance);
            }

            // Set the dialog content
            dialog.getDialogPane().setContent(root);

            // Add buttons
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // Set the result converter to handle the OK button
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        // Get the maintenance data from the form
                        String description = controller.getDescription();
                        LocalDateTime startDateTime = controller.getStartDateTime();
                        LocalDateTime endDateTime = controller.getEndDateTime();

                        // Validate the input
                        if (description == null || description.trim().isEmpty()) {
                            showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a description.");
                            return null;
                        }

                        if (startDateTime == null || endDateTime == null) {
                            showAlert(Alert.AlertType.WARNING, "Invalid Input",
                                    "Please select valid start and end times.");
                            return null;
                        }

                        if (!endDateTime.isAfter(startDateTime)) {
                            showAlert(Alert.AlertType.WARNING, "Invalid Time Range",
                                    "End time must be after start time.");
                            return null;
                        }

                        // Create or update the maintenance object
                        if (isEditMode) {
                            finalMaintenanceRef[0].setDescription(description);
                            finalMaintenanceRef[0].setStartTime(startDateTime);
                            finalMaintenanceRef[0].setEndTime(endDateTime);
                            return ButtonType.OK;
                        } else {
                            // Create a new maintenance object
                            finalMaintenanceRef[0] = new Maintenance(
                                    "",
                                    hall.getHallId(),
                                    description,
                                    startDateTime,
                                    endDateTime);
                            return ButtonType.OK;
                        }
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
                        return null;
                    }
                }
                return null;
            });

            // Show the dialog and process the result
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (isEditMode) {
                    // Update existing maintenance schedule
                    TaskUtils.executeTaskWithProgress(
                            maintenanceService.updateMaintenance(finalMaintenanceRef[0]),
                            success -> {
                                if (success) {
                                    Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                                "Maintenance schedule updated successfully.");
                                        loadAllMaintenance();
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        showAlert(Alert.AlertType.ERROR, "Error",
                                                "Failed to update maintenance schedule.");
                                    });
                                }
                            },
                            error -> Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        "Error updating maintenance schedule: " + error.getMessage());
                            }));
                } else {
                    // Add new maintenance schedule
                    TaskUtils.executeTaskWithProgress(
                            maintenanceService.addMaintenance(
                                    finalMaintenanceRef[0].getHallId(),
                                    finalMaintenanceRef[0].getDescription(),
                                    finalMaintenanceRef[0].getStartTime(),
                                    finalMaintenanceRef[0].getEndTime()),
                            newMaintenance -> {
                                Platform.runLater(() -> {
                                    showAlert(Alert.AlertType.INFORMATION, "Success",
                                            "Maintenance schedule added successfully.");
                                    loadAllMaintenance();
                                });
                            },
                            error -> Platform.runLater(() -> {
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        "Error adding maintenance schedule: " + error.getMessage());
                            }));
                }
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load maintenance form: " + e.getMessage());
        }
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

    /**
     * Initializes the booking history tab with table columns and cell factories.
     */
    private void initializeBookingHistoryTab() {
        // Initialize booking table columns
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        bookingCustomerIdColumn.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        bookingHallIdColumn.setCellValueFactory(new PropertyValueFactory<>("hallId"));
        
        // Format date/time columns
        bookingStartDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStartDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
        
        bookingEndDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEndDateTime()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        );
        
        bookingTotalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("bookingStatus"));
        
        // Set cell factory for status column to add color coding
        bookingStatusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BookingStatus status, boolean empty) {
                super.updateItem(status, empty);
                
                if (status == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    
                    switch (status) {
                        case BOOKED:
                            setTextFill(Color.GREEN);
                            break;
                        case CANCELLED:
                            setTextFill(Color.RED);
                            break;
                        default:
                            setTextFill(Color.BLACK);
                    }
                }
            }
        });
    }
    
    /**
     * Loads all bookings from the database and displays them in the table.
     */
    private void loadAllBookings() {
        Task<List<Booking>> task = new BookingService().getAllBookings();
        
        task.setOnSucceeded(e -> {
            List<Booking> bookings = task.getValue();
            if (bookings != null) {
                // Sort bookings by start date (newest first)
                bookings.sort((b1, b2) -> b2.getStartDateTime().compareTo(b1.getStartDateTime()));
                applyBookingFilters(bookings);
            }
        });
        
        task.setOnFailed(e -> {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load bookings: " + task.getException().getMessage());
            task.getException().printStackTrace();
        });
        
        new Thread(task).start();
    }
    
    /**
     * Applies the current filters to the booking list
     * @param bookings The list of all bookings to filter
     */
    private void applyBookingFilters(List<Booking> bookings) {
        if (bookings == null) return;
        
        String hallId = hallIdFilter.getText().trim().toLowerCase();
        String customerId = customerIdFilter.getText().trim().toLowerCase();
        String status = statusFilter.getValue() != null ? statusFilter.getValue() : "ALL";
        
        List<Booking> filteredBookings = bookings.stream()
            .filter(booking -> 
                (hallId.isEmpty() || booking.getHallId().toLowerCase().contains(hallId)) &&
                (customerId.isEmpty() || booking.getCustomerId().toLowerCase().contains(customerId)) &&
                ("ALL".equals(status) || booking.getBookingStatus().name().equals(status))
            )
            .collect(Collectors.toList());
            
        bookingsTable.getItems().setAll(filteredBookings);
    }
    
    /**
     * Handles the apply filters button click
     */
    @FXML
    public void handleApplyBookingFilters() {
        // Reload all bookings to apply filters
        loadAllBookings();
    }
    
    /**
     * Handles the clear filters button click
     */
    @FXML
    public void handleClearBookingFilters() {
        hallIdFilter.clear();
        customerIdFilter.clear();
        statusFilter.setValue("ALL");
        loadAllBookings();
    }
    
    /**
     * Handles the refresh bookings button click.
     */
    @FXML
    public void handleRefreshBookings() {
        loadAllBookings();
    }
}