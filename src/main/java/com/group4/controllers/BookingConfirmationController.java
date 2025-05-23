package com.group4.controllers;

import com.group4.models.Booking;
import com.group4.models.Hall;
import com.group4.models.User;
import com.group4.services.BookingService;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for booking confirmation.
 */
public class BookingConfirmationController implements Initializable {

    @FXML
    private Label hallIdLabel;

    @FXML
    private Label hallTypeLabel;

    @FXML
    private Label capacityLabel;

    @FXML
    private Label rateLabel;

    @FXML
    private DatePicker bookingDatePicker;

    @FXML
    private ComboBox<String> startTimeComboBox;

    @FXML
    private ComboBox<String> endTimeComboBox;

    @FXML
    private Label durationLabel;

    @FXML
    private Label totalCostLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private Hall selectedHall;
    private final BookingService bookingService = new BookingService();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize time combo boxes with business hours (8 AM - 6 PM)
        populateTimeComboBoxes();

        // Add listeners for time selection to calculate duration and cost
        startTimeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> calculateDurationAndCost());

        endTimeComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> calculateDurationAndCost());

        bookingDatePicker.valueProperty().addListener(
                (observable, oldValue, newValue) -> calculateDurationAndCost());

        // Set default date to today
        bookingDatePicker.setValue(LocalDate.now());
    }

    /**
     * Sets the hall to book.
     * 
     * @param hall the hall to book
     */
    public void setHall(Hall hall) {
        this.selectedHall = hall;

        // Display hall details
        hallIdLabel.setText(hall.getHallId());
        hallTypeLabel.setText(hall.getType().toString());
        capacityLabel.setText(String.valueOf(hall.getCapacity()));
        rateLabel.setText(String.format("$%.2f per hour", hall.getRatePerHour()));
    }

    /**
     * Populates the time combo boxes with business hours (8 AM - 6 PM).
     */
    private void populateTimeComboBoxes() {
        // Business hours: 8 AM - 6 PM
        LocalTime startTime = LocalTime.of(8, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        // Create time slots at hourly intervals
        while (!startTime.isAfter(endTime)) {
            String timeStr = startTime.format(TIME_FORMATTER);
            startTimeComboBox.getItems().add(timeStr);

            // End time combo box should not include the first time slot
            if (!startTime.equals(startTime.withHour(8).withMinute(0))) {
                endTimeComboBox.getItems().add(timeStr);
            }

            startTime = startTime.plusHours(1);
        }

        // Add the last end time (6 PM)
        endTimeComboBox.getItems().add(endTime.format(TIME_FORMATTER));

        // Set default selections
        startTimeComboBox.getSelectionModel().select(0); // 8 AM
        endTimeComboBox.getSelectionModel().select(2); // 10 AM (2 hour booking by default)
    }

    /**
     * Calculates the booking duration and total cost.
     */
    private void calculateDurationAndCost() {
        if (selectedHall == null || bookingDatePicker.getValue() == null ||
                startTimeComboBox.getValue() == null || endTimeComboBox.getValue() == null) {
            return;
        }

        try {
            LocalDate bookingDate = bookingDatePicker.getValue();

            // Parse times
            LocalTime startTime = LocalTime.parse(startTimeComboBox.getValue(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(endTimeComboBox.getValue(), TIME_FORMATTER);

            // Create full date-time
            startDateTime = LocalDateTime.of(bookingDate, startTime);
            endDateTime = LocalDateTime.of(bookingDate, endTime);

            // Calculate duration in hours
            if (startDateTime.isBefore(endDateTime)) {
                Duration duration = Duration.between(startDateTime, endDateTime);
                long hours = duration.toHours();

                durationLabel.setText(String.format("%d hours", hours));

                // Calculate total cost
                double totalCost = selectedHall.getRatePerHour() * hours;
                totalCostLabel.setText(String.format("$%.2f", totalCost));

                // Enable confirm button if all is well
                confirmButton.setDisable(false);
            } else {
                durationLabel.setText("Invalid time selection");
                totalCostLabel.setText("$0.00");
                confirmButton.setDisable(true);
            }
        } catch (Exception e) {
            durationLabel.setText("Error calculating duration");
            totalCostLabel.setText("$0.00");
            confirmButton.setDisable(true);
        }
    }

    /**
     * Handles the confirm booking button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleConfirmBooking(ActionEvent event) {
        if (selectedHall == null || startDateTime == null || endDateTime == null
                || startDateTime.isAfter(endDateTime)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Booking", "Please select valid date and time for booking.");
            return;
        }

        // Calculate total cost
        Duration duration = Duration.between(startDateTime, endDateTime);
        long hours = duration.toHours();
        double totalCost = selectedHall.getRatePerHour() * hours;

        // Get current user from session
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "You must be logged in to make a booking.");
            return;
        }

        // Create the booking
        Task<Booking> bookingTask = new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                // Create booking using the available method
                Task<Booking> createTask = bookingService.createBooking(selectedHall.getHallId(), startDateTime,
                        endDateTime);
                return TaskUtils.executeTask(createTask);
            }
        };

        TaskUtils.executeTaskWithProgress(bookingTask,
                booking -> {
                    if (booking != null) {
                        showReceipt(booking);
                        closeWindow();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Booking Error",
                                "Failed to create booking. The hall may not be available at the selected time.");
                    }
                },
                error -> showAlert(Alert.AlertType.ERROR, "Booking Error",
                        "Failed to create booking: " + error.getMessage()));
    }

    /**
     * Handles the cancel button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    /**
     * Shows the booking receipt.
     * 
     * @param booking the booking to show
     */
    private void showReceipt(Booking booking) {
        try {
            // Load the receipt FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/Receipt.fxml"));
            Parent root = loader.load();

            // Get the controller and set the booking
            ReceiptController controller = loader.getController();
            controller.setBooking(booking);

            // Create a new stage for the receipt
            Stage receiptStage = new Stage();
            receiptStage.initModality(Modality.APPLICATION_MODAL);
            receiptStage.setTitle("Booking Receipt");
            receiptStage.setScene(new Scene(root));
            receiptStage.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load receipt: " + e.getMessage());
        }
    }

    /**
     * Closes the current window.
     */
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
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