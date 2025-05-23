package com.group4.controllers;

import com.group4.models.Booking;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

/**
 * Controller for displaying a booking receipt.
 */
public class ReceiptController {

    @FXML
    private Label bookingIdLabel;

    @FXML
    private Label hallIdLabel;

    @FXML
    private Label customerIdLabel;

    @FXML
    private Label startDateTimeLabel;

    @FXML
    private Label endDateTimeLabel;

    @FXML
    private Label totalCostLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Button closeButton;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Sets the booking information to display.
     * 
     * @param booking the booking to display
     */
    public void setBooking(Booking booking) {
        if (booking == null) {
            return;
        }

        bookingIdLabel.setText(booking.getBookingId());
        hallIdLabel.setText(booking.getHallId());
        customerIdLabel.setText(booking.getCustomerId());
        startDateTimeLabel.setText(booking.getStartDateTime().format(DATETIME_FORMATTER));
        endDateTimeLabel.setText(booking.getEndDateTime().format(DATETIME_FORMATTER));
        totalCostLabel.setText(String.format("$%.2f", booking.getTotalCost()));
        statusLabel.setText(booking.getStatus().toString());
    }

    /**
     * Handles the print button click.
     */
    @FXML
    private void handlePrint() {
        // In a real application, this would send the receipt to a printer
        // For this demo, we'll just show a message
        System.out.println("Printing receipt...");
    }

    /**
     * Handles the close button click.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}