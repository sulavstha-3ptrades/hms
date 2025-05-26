package com.group4.controllers;

import com.group4.App;
import com.group4.models.Booking;
import com.group4.services.BookingService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.group4.utils.ViewManager;
import com.group4.models.User;
import javafx.scene.control.Label;

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

    // Status column removed

    @FXML
    private Button cancelBookingButton;

    @FXML
    private Button refreshBookingsButton;

    private BookingService bookingService;
    private ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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

        // Load customer bookings
        loadCustomerBookings();
    }

    /**
     * Loads and displays the current user's information in the header.
     */
    private void loadCurrentUserInfo() {
        try {
            // Get the current user from the session
            User currentUser = SessionManager.getInstance().getCurrentUser();
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
                        60, 60, true, true
                    );
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
                60, 60, true, true
            );
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
                            showAlert(Alert.AlertType.ERROR, "Error", "Could not cancel booking. Please try again later.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to cancel booking: " + error.getMessage())
                );
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
}