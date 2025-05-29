package com.group4.controllers;

import com.group4.models.Booking;
import com.group4.models.Hall;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the booking confirmation dialog.
 * Handles UI logic and field bindings for the booking confirmation dialog.
 * This controller is specifically scoped to handle dialog UI elements and data
 * presentation.
 */
public class BookingConfirmationController implements Initializable {

    @FXML
    private Label hallNameLabel;
    @FXML
    private DatePicker bookingDatePicker;
    @FXML
    private ComboBox<String> startHourComboBox;
    @FXML
    private ComboBox<String> startMinuteComboBox;
    @FXML
    private ComboBox<String> startAmPmComboBox;
    @FXML
    private ComboBox<String> endHourComboBox;
    @FXML
    private ComboBox<String> endMinuteComboBox;
    @FXML
    private ComboBox<String> endAmPmComboBox;
    @FXML
    private Label durationLabel;
    @FXML
    private Label costLabel;
    @FXML
    private TextArea specialRequests;

    private Hall currentHall;
    private List<BookingTimeSlot> bookedTimeSlots;
    private static final Logger LOGGER = Logger.getLogger(BookingConfirmationController.class.getName());

    /**
     * Inner class to represent a booked time slot for a hall
     */
    private static class BookingTimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private String bookingId;

        public BookingTimeSlot(LocalTime startTime, LocalTime endTime, String bookingId) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.bookingId = bookingId;
        }

        /**
         * Checks if this time slot overlaps with the specified time range
         * 
         * @param start The start time to check
         * @param end   The end time to check
         * @return true if there is an overlap, false otherwise
         */
        public boolean overlaps(LocalTime start, LocalTime end) {
            return !(end.isBefore(startTime) || start.isAfter(endTime));
        }

        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
            return startTime.format(formatter) + " - " + endTime.format(formatter) + " (" + bookingId + ")";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize services
        bookedTimeSlots = new ArrayList<>();

        // Initialize time selection ComboBoxes
        initializeTimeComboBoxes();

        // Add listeners to update duration and cost when time selection changes
        addTimeChangeListeners();

        // Set minimum date to today
        bookingDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Add listener to date picker to check availability when date changes
        bookingDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && currentHall != null) {
                loadBookedTimeSlots(currentHall.getHallId(), newValue);
            }
        });
    }

    /**
     * Initializes the time selection ComboBoxes with appropriate values.
     */
    private void initializeTimeComboBoxes() {
        // Hours (12-hour format)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 1; i <= 12; i++) {
            hours.add(String.format("%d", i));
        }
        startHourComboBox.setItems(hours);
        endHourComboBox.setItems(hours);

        // Minutes (in 5-minute increments)
        ObservableList<String> minutes = FXCollections.observableArrayList();
        for (int i = 0; i < 60; i += 5) {
            minutes.add(String.format("%02d", i));
        }
        startMinuteComboBox.setItems(minutes);
        endMinuteComboBox.setItems(minutes);

        // AM/PM
        ObservableList<String> amPm = FXCollections.observableArrayList("AM", "PM");
        startAmPmComboBox.setItems(amPm);
        endAmPmComboBox.setItems(amPm);
    }

    /**
     * Adds change listeners to time selection controls to update duration and cost.
     */
    private void addTimeChangeListeners() {
        ChangeListener<Object> timeChangeListener = (observable, oldValue, newValue) -> {
            updateDurationAndCost();
        };

        startHourComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
        startMinuteComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
        startAmPmComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
        endHourComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
        endMinuteComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
        endAmPmComboBox.getSelectionModel().selectedItemProperty().addListener(timeChangeListener);
    }

    /**
     * Loads all booked time slots for the specified hall and date.
     * 
     * @param hallId The ID of the hall
     * @param date   The date to check
     */
    private void loadBookedTimeSlots(String hallId, LocalDate date) {
        bookedTimeSlots.clear();

        try {
            // Read all bookings from the file
            List<String> lines = FileHandler.readLines(FileConstants.getBookingsFilePath());

            for (String line : lines) {
                try {
                    Booking booking = Booking.fromDelimitedString(line);

                    if (booking != null &&
                            booking.getHallId().equals(hallId)) {

                        // Check if the booking is on the selected date
                        LocalDate bookingDate = booking.getStartDateTime().toLocalDate();

                        if (bookingDate.equals(date)) {
                            // Add this time slot to the list of booked slots
                            BookingTimeSlot slot = new BookingTimeSlot(
                                    booking.getStartDateTime().toLocalTime(),
                                    booking.getEndDateTime().toLocalTime(),
                                    booking.getBookingId());
                            bookedTimeSlots.add(slot);
                            LOGGER.info("Found booked time slot: " + slot);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error parsing booking: " + line, e);
                }
            }

            // Update UI to show booked time slots
            updateTimeSelectionUI();

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading bookings file", e);
        }
    }

    /**
     * Updates the time selection UI to reflect booked time slots.
     */
    private void updateTimeSelectionUI() {
        // Reset all time selection controls to default style
        resetTimeControlStyles();

        // Check if current selection conflicts with any booked slots
        checkAndUpdateTimeConflicts();
    }

    /**
     * Resets all time selection controls to default style.
     */
    private void resetTimeControlStyles() {
        startHourComboBox.setStyle("");
        startMinuteComboBox.setStyle("");
        startAmPmComboBox.setStyle("");
        endHourComboBox.setStyle("");
        endMinuteComboBox.setStyle("");
        endAmPmComboBox.setStyle("");
    }

    /**
     * Checks if the current time selection conflicts with any booked slots and
     * updates UI accordingly.
     */
    private void checkAndUpdateTimeConflicts() {
        LocalTime startTime = getSelectedStartTime();
        LocalTime endTime = getSelectedEndTime();

        if (startTime != null && endTime != null) {
            boolean hasConflict = false;

            for (BookingTimeSlot slot : bookedTimeSlots) {
                if (slot.overlaps(startTime, endTime)) {
                    hasConflict = true;
                    break;
                }
            }

            if (hasConflict) {
                // Mark time controls with conflict style
                String conflictStyle = "-fx-border-color: red; -fx-border-width: 2px;";
                startHourComboBox.setStyle(conflictStyle);
                startMinuteComboBox.setStyle(conflictStyle);
                startAmPmComboBox.setStyle(conflictStyle);
                endHourComboBox.setStyle(conflictStyle);
                endMinuteComboBox.setStyle(conflictStyle);
                endAmPmComboBox.setStyle(conflictStyle);

                // Show conflict tooltip
                Tooltip tooltip = new Tooltip("This time slot conflicts with an existing booking");
                startHourComboBox.setTooltip(tooltip);
                startMinuteComboBox.setTooltip(tooltip);
                startAmPmComboBox.setTooltip(tooltip);
                endHourComboBox.setTooltip(tooltip);
                endMinuteComboBox.setTooltip(tooltip);
                endAmPmComboBox.setTooltip(tooltip);
            } else {
                // Clear tooltips
                startHourComboBox.setTooltip(null);
                startMinuteComboBox.setTooltip(null);
                startAmPmComboBox.setTooltip(null);
                endHourComboBox.setTooltip(null);
                endMinuteComboBox.setTooltip(null);
                endAmPmComboBox.setTooltip(null);
            }
        }
    }

    /**
     * Updates the duration and cost based on the selected start and end times.
     * Also checks for time conflicts with existing bookings.
     */
    private void updateDurationAndCost() {
        LocalTime startTime = getSelectedStartTime();
        LocalTime endTime = getSelectedEndTime();

        if (startTime != null && endTime != null && currentHall != null) {
            // Ensure end time is after start time
            if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                // If end time is before or equal to start time, set end time to start time + 1
                // hour
                endTime = startTime.plusHours(1);
                updateEndTimeControls(endTime);
            }

            // Calculate and set duration
            long durationMinutes = Duration.between(startTime, endTime).toMinutes();
            double durationHours = durationMinutes / 60.0;
            durationLabel.setText(String.format("%.1f hours", durationHours));

            // Calculate and set cost
            double cost = currentHall.getRatePerHour() * durationHours;
            costLabel.setText(String.format("$%.2f", cost));

            // Check for conflicts with existing bookings
            checkAndUpdateTimeConflicts();
        }
    }

    /**
     * Gets the selected start time from the ComboBoxes.
     * 
     * @return The selected start time, or null if selection is incomplete
     */
    public LocalTime getSelectedStartTime() {
        String hour = startHourComboBox.getValue();
        String minute = startMinuteComboBox.getValue();
        String amPm = startAmPmComboBox.getValue();

        if (hour != null && minute != null && amPm != null) {
            int h = Integer.parseInt(hour);
            int m = Integer.parseInt(minute);

            // Convert to 24-hour format
            if (amPm.equals("PM") && h < 12) {
                h += 12;
            } else if (amPm.equals("AM") && h == 12) {
                h = 0;
            }

            return LocalTime.of(h, m);
        }

        return null;
    }

    /**
     * Gets the selected end time from the ComboBoxes.
     * 
     * @return The selected end time, or null if selection is incomplete
     */
    public LocalTime getSelectedEndTime() {
        String hour = endHourComboBox.getValue();
        String minute = endMinuteComboBox.getValue();
        String amPm = endAmPmComboBox.getValue();

        if (hour != null && minute != null && amPm != null) {
            int h = Integer.parseInt(hour);
            int m = Integer.parseInt(minute);

            // Convert to 24-hour format
            if (amPm.equals("PM") && h < 12) {
                h += 12;
            } else if (amPm.equals("AM") && h == 12) {
                h = 0;
            }

            return LocalTime.of(h, m);
        }

        return null;
    }

    /**
     * Updates the end time ComboBoxes to reflect the given time.
     * 
     * @param endTime The end time to set
     */
    private void updateEndTimeControls(LocalTime endTime) {
        int hour = endTime.getHour();
        String amPm = "AM";

        // Convert to 12-hour format
        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) {
                hour -= 12;
            }
        } else if (hour == 0) {
            hour = 12;
        }

        endHourComboBox.setValue(String.valueOf(hour));
        endMinuteComboBox.setValue(String.format("%02d", endTime.getMinute()));
        endAmPmComboBox.setValue(amPm);
    }

    /**
     * Sets the booking details to be displayed in the dialog.
     * 
     * @param hall      The hall being booked
     * @param date      The booking date
     * @param startTime The start time of the booking
     * @param endTime   The end time of the booking
     * @param cost      The total cost of the booking
     */
    public void setBookingDetails(Hall hall, LocalDate date, LocalTime startTime, LocalTime endTime, double cost) {
        if (hall == null || date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("All booking details must be provided");
        }

        // Store the hall for cost calculations
        this.currentHall = hall;

        // Set hall name
        hallNameLabel.setText(hall.getHallId());

        // Set date
        bookingDatePicker.setValue(date);

        // Set time values in ComboBoxes
        setTimeControls(startTime, endTime);

        // Calculate and set duration
        long durationMinutes = Duration.between(startTime, endTime).toMinutes();
        double durationHours = durationMinutes / 60.0;
        durationLabel.setText(String.format("%.1f hours", durationHours));

        // Set cost
        costLabel.setText(String.format("$%.2f", cost));
    }

    /**
     * Sets the time selection controls to the specified start and end times.
     * 
     * @param startTime The start time to set
     * @param endTime   The end time to set
     */
    private void setTimeControls(LocalTime startTime, LocalTime endTime) {
        // Set start time controls
        int startHour = startTime.getHour();
        String startAmPm = "AM";

        if (startHour >= 12) {
            startAmPm = "PM";
            if (startHour > 12) {
                startHour -= 12;
            }
        } else if (startHour == 0) {
            startHour = 12;
        }

        startHourComboBox.setValue(String.valueOf(startHour));
        startMinuteComboBox.setValue(String.format("%02d", startTime.getMinute()));
        startAmPmComboBox.setValue(startAmPm);

        // Set end time controls
        updateEndTimeControls(endTime);
    }

    /**
     * Gets the selected booking date.
     * 
     * @return The selected booking date
     */
    public LocalDate getSelectedDate() {
        return bookingDatePicker.getValue();
    }

    /**
     * Gets any special requests entered by the user.
     * 
     * @return The special requests text
     */
    public String getSpecialRequests() {
        return specialRequests.getText();
    }
}