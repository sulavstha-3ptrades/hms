package com.group4.controllers;

import com.group4.App;
import com.group4.models.*;
import com.group4.services.BookingService;
import com.group4.services.HallService;
import com.group4.services.UserService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;
import com.group4.utils.ViewManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Customer Dashboard.
 */
public class CustomerDashboardController {

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab myBookingsTab;

    @FXML
    private Tab bookHallTab;

    @FXML
    private Tab myProfileTab;

    @FXML
    private Button logoutButton;

    @FXML
    private TableView<Booking> bookingsTable;

    @FXML
    private TableColumn<Booking, String> bookingIdColumn;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private TableColumn<Booking, String> hallIdColumn;

    @FXML
    private TableColumn<Booking, LocalDateTime> startDateTimeColumn;

    @FXML
    private TableColumn<Booking, LocalDateTime> endDateTimeColumn;

    @FXML
    private TableColumn<Booking, Double> totalCostColumn;

    @FXML
    private TableColumn<Booking, BookingStatus> bookingStatusColumn;

    @FXML
    private Button cancelBookingButton;

    @FXML
    private Button refreshBookingsButton;

    @FXML
    private Button reportIssueButton;

    @FXML
    private Button editProfileButton;

    private BookingService bookingService;
    private ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private User currentUser;
    private UserService userService;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Load and display current user information
        loadCurrentUserInfo();

        // Set up the profile image view
        setupProfileImageView();
        bookingService = new BookingService();
        userService = new UserService();

        // Initialize table columns
        bookingIdColumn.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());

        // Format date/time columns
        startDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startDateTime"));
        startDateTimeColumn.setCellFactory(column -> new TableCell<Booking, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(DATETIME_FORMATTER));
                }
            }
        });

        endDateTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endDateTime"));
        endDateTimeColumn.setCellFactory(column -> new TableCell<Booking, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(item.format(DATETIME_FORMATTER));
                }
            }
        });

        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());

        // Initialize booking status column
        bookingStatusColumn.setCellValueFactory(cellData -> cellData.getValue().bookingStatusProperty());

        // Load customer bookings
        loadCustomerBookings();
    }

    /**
     * Loads and displays the current user's information in the header.
     */
    private void loadCurrentUserInfo() {
        try {
            // Get the current user from the session
            currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Set user name and role
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                userRoleLabel.setText(currentUser.getRole());

                // Load profile picture
                try {
                    String profilePicPath = currentUser.getProfilePicture();
                    // Load image from classpath
                    Image profileImage = new Image(
                            getClass().getResourceAsStream(profilePicPath),
                            60, 60, true, true);
                    profileImageView.setImage(profileImage);
                } catch (Exception e) {
                    System.err.println("Error loading profile image: " + e.getMessage());
                    // Fall back to default avatar
                    loadDefaultAvatar();
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading user info: " + e.getMessage());
            e.printStackTrace();
            loadDefaultAvatar();
        }
    }

    /**
     * Loads the default avatar image.
     */
    private void loadDefaultAvatar() {
        try {
            Image defaultAvatar = new Image(
                    getClass().getResourceAsStream("/com/group4/assets/images/users/default-avatar.jpg"),
                    60, 60, true, true);
            profileImageView.setImage(defaultAvatar);
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
            // If we can't load the default avatar, set a blank image to avoid NPE
            profileImageView.setImage(null);
        }
    }

    /**
     * Sets up the profile image view with a circular mask and click handler.
     */
    private void setupProfileImageView() {
        // Make the image view circular
        profileImageView.setClip(new javafx.scene.shape.Circle(30, 30, 30));

        // Add hover effect
        profileImageView.setOnMouseEntered(e -> {
            profileImageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        });

        profileImageView.setOnMouseExited(e -> {
            profileImageView.setStyle("-fx-effect: null;");
        });

        // Add click handler to open profile
        profileImageView.setOnMouseClicked(e -> handleProfileSectionClick());
    }

    /**
     * Loads bookings for the current customer.
     */
    private void loadCustomerBookings() {
        String customerId = SessionManager.getInstance().getCurrentUser().getUserId();

        TaskUtils.executeTaskWithProgress(
                bookingService.getBookingsByCustomerId(customerId),
                bookings -> {
                    bookingsList.clear();
                    bookingsList.addAll(bookings);
                    bookingsTable.setItems(bookingsList);
                },
                error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load bookings: " + error.getMessage()));
    }

    /**
     * Handles the view bookings button click.
     * Refreshes the bookings list.
     */
    @FXML
    private void handleViewBookings() {
        loadCustomerBookings();
    }

    /**
     * Handles the cancel booking button click.
     */
    @FXML
    private void handleCancelBooking() {
        // Get the selected booking
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
            return;
        }

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Are you sure you want to cancel this booking?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                TaskUtils.executeTaskWithProgress(
                        bookingService.cancelBooking(selectedBooking.getBookingId()),
                        success -> {
                            if (success) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully.");
                                loadCustomerBookings(); // Refresh the list
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error",
                                        "Could not cancel booking. Please try again later.");
                            }
                        },
                        error -> showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to cancel booking: " + error.getMessage()));
            }
        });
    }

    /**
     * Handles the click event on the profile section in the header.
     * Navigates to the profile tab.
     */
    @FXML
    private void handleProfileSectionClick() {
        if (myProfileTab != null) {
            mainTabPane.getSelectionModel().select(myProfileTab);
        } else {
            System.err.println("Profile tab is not initialized");
        }
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load login screen: " + e.getMessage());
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
     * Handles the report issue button click.
     * Shows a dialog for the user to report a new issue.
     */
    @FXML
    private void handleReportIssue() {
        try {
            // Get the selected booking
            Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();

            if (selectedBooking == null) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to report an issue for.");
                return;
            }

            String selectedHallId = selectedBooking.getHallId();

            if (selectedHallId == null || selectedHallId.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Selected booking does not have a valid hall ID.");
                return;
            }

            // Create and show loading dialog
            Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
            loadingAlert.setTitle("Loading");
            loadingAlert.setHeaderText(null);
            loadingAlert.setContentText("Loading hall information...");
            loadingAlert.initModality(Modality.APPLICATION_MODAL);

            // Show the loading dialog in a separate thread
            new Thread(() -> {
                Platform.runLater(loadingAlert::show);
            }).start();

            // Get hall details
            HallService hallService = new HallService();
            TaskUtils.executeTaskWithProgress(
                    hallService.getHallById(selectedHallId),
                    hall -> Platform.runLater(() -> {
                        // Close the loading dialog
                        loadingAlert.close();

                        String hallType = "Unknown Type";
                        if (hall != null && hall.getType() != null) {
                            // Convert enum to display-friendly string (e.g., "MEETING_ROOM" -> "Meeting
                            // Room")
                            String[] parts = hall.getType().name().toLowerCase().split("_");
                            StringBuilder displayType = new StringBuilder();
                            for (String part : parts) {
                                if (!part.isEmpty()) {
                                    displayType.append(Character.toUpperCase(part.charAt(0)))
                                            .append(part.substring(1)).append(" ");
                                }
                            }
                            hallType = displayType.toString().trim();
                        }

                        // Create and show the dialog with the hall information
                        ReportIssueDialogController dialog = new ReportIssueDialogController(selectedHallId, hallType);
                        dialog.showAndWait();
                    }),
                    error -> Platform.runLater(() -> {
                        // Close the loading dialog
                        loadingAlert.close();

                        // Show error but still allow to proceed with just the hall ID
                        showAlert(Alert.AlertType.WARNING, "Warning",
                                "Could not load hall type. You can still report the issue with just the hall ID.");
                        ReportIssueDialogController dialog = new ReportIssueDialogController(selectedHallId,
                                "Unknown Type");
                        dialog.showAndWait();
                    }));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open report issue dialog: " + e.getMessage());
        }
    }

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
            FXMLLoader loader = new FXMLLoader(App.class.getResource("view/EditProfile.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the current user data
            EditProfileController controller = loader.getController();
            controller.setUserData(currentUser);

            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

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

                            // Force garbage collection
                            System.gc();

                            // Add a small delay to ensure the UI has time to update
                            new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            Platform.runLater(() -> {
                                                try {
                                                    // Update UI elements
                                                    loadCurrentUserInfo();

                                                    // Show success message
                                                    showAlert(Alert.AlertType.INFORMATION, "Success",
                                                            "Profile updated successfully!");
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    showAlert(Alert.AlertType.ERROR, "Error",
                                                            "Error updating profile: " + e.getMessage());
                                                }
                                            });
                                        }
                                    },
                                    100 // 100ms delay
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Error in profile update handler: " + e.getMessage());
                        }
                    });
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load updated profile data");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open edit profile: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }
}
