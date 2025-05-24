package com.group4.controllers;

import com.group4.App;
import com.group4.models.Booking;
import com.group4.models.BookingStatus;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import com.group4.utils.ViewManager;

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
    private Button logoutButton;

    @FXML
    private TableView<Booking> bookingsTable;

    @FXML
    private TableColumn<Booking, String> bookingIdColumn;

    @FXML
    private TableColumn<Booking, String> hallIdColumn;

    @FXML
    private TableColumn<Booking, LocalDateTime> startDateTimeColumn;

    @FXML
    private TableColumn<Booking, LocalDateTime> endDateTimeColumn;

    @FXML
    private TableColumn<Booking, Double> totalCostColumn;

    @FXML
    private TableColumn<Booking, BookingStatus> statusColumn;

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
        bookingService = new BookingService();

        // Initialize table columns
        bookingIdColumn.setCellValueFactory(cellData -> cellData.getValue().bookingIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        startDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().startDateTimeProperty());
        endDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endDateTimeProperty());
        totalCostColumn.setCellValueFactory(cellData -> cellData.getValue().totalCostProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Load customer bookings
        loadCustomerBookings();
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
        Booking selectedBooking = bookingsTable.getSelectionModel().getSelectedItem();
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking to cancel.");
            return;
        }

        // Check if booking is already cancelled
        if (selectedBooking.getStatus() == BookingStatus.CANCELED) {
            showAlert(Alert.AlertType.WARNING, "Already Cancelled", "This booking has already been cancelled.");
            return;
        }

        // Show confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Booking");
        confirm.setHeaderText("Are you sure you want to cancel this booking?");
        confirm.setContentText(
                "Cancellation policy: Bookings can only be cancelled if they are more than 3 days before the start date.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            TaskUtils.executeTaskWithProgress(
                    bookingService.cancelBooking(selectedBooking.getBookingId()),
                    success -> {
                        if (success) {
                            showAlert(Alert.AlertType.INFORMATION, "Success", "Booking cancelled successfully.");
                            loadCustomerBookings(); // Refresh the list
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error",
                                    "Could not cancel booking. Ensure it's more than 3 days before the start date.");
                        }
                    },
                    error -> showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to cancel booking: " + error.getMessage()));
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