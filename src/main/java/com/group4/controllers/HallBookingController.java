package com.group4.controllers;

import com.group4.models.Availability;
import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.services.AvailabilityService;
import com.group4.services.BookingService;
import com.group4.services.HallService;
import com.group4.utils.TaskUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;

/**
 * Controller for hall search and booking.
 */
public class HallBookingController implements Initializable {

    @FXML
    private DatePicker searchDatePicker;

    @FXML
    private ComboBox<HallType> hallTypeComboBox;

    // Status-related fields removed

    @FXML
    private TextField capacityField;

    @FXML
    private Button searchButton;

    @FXML
    private TableView<Hall> hallTable;

    @FXML
    private TableColumn<Hall, String> hallIdColumn;

    @FXML
    private TableColumn<Hall, HallType> typeColumn;

    @FXML
    private TableColumn<Hall, Integer> capacityColumn;

    @FXML
    private TableColumn<Hall, Double> rateColumn;

    private final HallService hallService = new HallService();
    private final AvailabilityService availabilityService = new AvailabilityService();
    private final BookingService bookingService = new BookingService();
    private ObservableList<Hall> allHalls = FXCollections.observableArrayList();
    private FilteredList<Hall> filteredHalls;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize hall type combo box
        hallTypeComboBox.setItems(FXCollections.observableArrayList(HallType.values()));
        hallTypeComboBox.getItems().add(0, null); // Add null option for "Any type"

        // Status combo box removed

        // Initialize table columns
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        rateColumn.setCellValueFactory(cellData -> cellData.getValue().ratePerHourProperty().asObject());

        // Set up date picker constraints
        LocalDate today = LocalDate.now();
        searchDatePicker.setValue(today);
        searchDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(today));
            }
        });

        // Load all halls without filtering
        loadHalls();
    }

    /**
     * Loads all halls.
     */
    private void loadHalls() {
        Task<List<Hall>> hallsTask = hallService.getAllHalls();

        // Show loading indicator
        hallTable.setPlaceholder(new Label("Loading halls..."));

        TaskUtils.executeTaskWithProgress(hallsTask,
                halls -> {
                    allHalls.clear();
                    allHalls.addAll(halls);
                    filteredHalls = new FilteredList<>(allHalls);
                    hallTable.setItems(filteredHalls);

                    if (halls.isEmpty()) {
                        hallTable.setPlaceholder(new Label("No halls available in the system."));
                    }
                },
                error -> {
                    showAlert("Error loading halls: " + error.getMessage());
                    hallTable.setPlaceholder(new Label("Error loading halls. Please try again."));
                });
    }

    /**
     * Handles the search button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        // Ensure halls are loaded
        if (filteredHalls == null) {
            showAlert("Please wait while halls are being loaded...");
            return;
        }

        // Get filter values
        LocalDate selectedDate = searchDatePicker.getValue();
        HallType selectedType = hallTypeComboBox.getValue();
        int minCapacity = 0;

        try {
            if (!capacityField.getText().isEmpty()) {
                minCapacity = Integer.parseInt(capacityField.getText());
            }

            // Validate capacity against hall type if selected
            if (selectedType != null && minCapacity > selectedType.getMaxCapacity()) {
                showAlert("Maximum capacity for " + selectedType.name() + " is " + selectedType.getMaxCapacity() + ".");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for capacity.");
            return;
        }

        // Validate search date
        LocalDate today = LocalDate.now();
        if (selectedDate == null || selectedDate.isBefore(today)) {
            showAlert("Please select a valid date from today onwards.");
            searchDatePicker.setValue(today);
            return;
        }

        // Build predicate for hall filtering
        final Predicate<Hall> initialPredicate = hall -> true; // Start with accepting all halls

        // Filter by hall type if selected
        final HallType selectedTypeFinal = selectedType;

        // Apply hall type filter
        final Predicate<Hall> typeFilteredPredicate;
        if (selectedTypeFinal != null) {
            typeFilteredPredicate = initialPredicate.and(hall -> hall.getType() == selectedTypeFinal);
        } else {
            typeFilteredPredicate = initialPredicate;
        }

        // Filter by minimum capacity
        final int selectedCapacity = minCapacity;
        final Predicate<Hall> capacityFilteredPredicate;
        if (selectedCapacity > 0) {
            capacityFilteredPredicate = typeFilteredPredicate.and(hall -> hall.getCapacity() >= selectedCapacity);
        } else {
            capacityFilteredPredicate = typeFilteredPredicate;
        }

        // Apply hall type and capacity filters
        filteredHalls.setPredicate(capacityFilteredPredicate);

        // If date is selected, check availability
        if (selectedDate != null) {
            final Predicate<Hall> finalPredicate = capacityFilteredPredicate;

            // Create task to check availability for filtered halls
            Task<List<String>> availableHallsTask = new Task<List<String>>() {
                @Override
                protected List<String> call() throws Exception {
                    // Create start and end time for the whole day
                    LocalDateTime startDateTime = LocalDateTime.of(selectedDate, LocalTime.of(8, 0)); // Business hours
                                                                                                      // start 8 AM
                    LocalDateTime endDateTime = LocalDateTime.of(selectedDate, LocalTime.of(18, 0)); // Business hours
                                                                                                     // end 6 PM

                    // Get all availability entries for this period
                    List<Availability> availabilities = availabilityService.getAvailabilitiesByPeriod(startDateTime,
                            endDateTime);

                    // Filter to only available halls
                    return availabilities.stream()
                            .filter(Availability::isAvailable)
                            .map(Availability::getHallId)
                            .collect(Collectors.toList());
                }
            };

            TaskUtils.executeTaskWithProgress(availableHallsTask,
                    availableHallIds -> {
                        // Add availability filter
                        Predicate<Hall> availabilityPredicate = finalPredicate
                                .and(hall -> availableHallIds.contains(hall.getHallId()));
                        filteredHalls.setPredicate(availabilityPredicate);

                        // Show message if no halls are available
                        if (hallTable.getItems().isEmpty()) {
                            hallTable.setPlaceholder(new Label("No halls available for the selected criteria."));
                        }
                    },
                    error -> showAlert("Error checking hall availability: " + error.getMessage()));
        }
    }

    /**
     * Handles the select hall button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSelectHall(ActionEvent event) {
        try {
            Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
            if (selectedHall == null) {
                showAlert("Please select a hall from the table.");
                return;
            }

            // Validate booking date
            LocalDate bookingDate = searchDatePicker.getValue();
            LocalDate today = LocalDate.now();

            if (bookingDate == null) {
                showAlert("Please select a booking date.");
                return;
            }

            if (bookingDate.isBefore(today)) {
                showAlert("Cannot book a hall for past dates. Please select today or a future date.");
                searchDatePicker.setValue(today);
                return;
            }

            // Load the booking confirmation dialog using the new dialog-specific FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/group4/view/BookingConfirmationDialog.fxml"));
            Parent dialogRoot = loader.load();

            // Get the controller and set booking details
            BookingConfirmationController dialogController = loader.getController();
            if (dialogController == null) {
                showAlert("Error: Could not initialize the booking confirmation dialog controller.");
                return;
            }

            // Set booking details with start time slightly in the future to avoid
            // validation errors
            // Add 1 minute to current time and reset seconds/nanos to avoid "Start time
            // cannot be in the past" error
            LocalTime startTime = LocalTime.now().plusMinutes(1).withSecond(0).withNano(0);
            LocalTime endTime = startTime.plusHours(1);
            double estimatedCost = selectedHall.getRatePerHour();

            // Set all booking details at once using the new method
            dialogController.setBookingDetails(
                    selectedHall,
                    bookingDate,
                    startTime,
                    endTime,
                    estimatedCost);

            // Create and configure the dialog
            Dialog<ButtonType> bookingDialog = new Dialog<>();
            bookingDialog.setDialogPane((DialogPane) dialogRoot);
            bookingDialog.setTitle("Book Hall - " + selectedHall.getHallId());

            // Customize button text
            Button okButton = (Button) bookingDialog.getDialogPane().lookupButton(ButtonType.OK);
            if (okButton != null) {
                okButton.setText("Confirm Booking");
                okButton.getStyleClass().add("confirm-button");
            }

            Button cancelButton = (Button) bookingDialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (cancelButton != null) {
                cancelButton.setText("Cancel");
            }

            // Show the dialog and wait for user response
            Optional<ButtonType> result = bookingDialog.showAndWait();

            // Process the result
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                // Get special requests if any
                String specialRequests = dialogController.getSpecialRequests();

                // Get user-selected date and times
                LocalDate selectedDate = dialogController.getSelectedDate();
                LocalTime selectedStartTime = dialogController.getSelectedStartTime();
                LocalTime selectedEndTime = dialogController.getSelectedEndTime();

                // Validate selections
                if (selectedDate == null || selectedStartTime == null || selectedEndTime == null) {
                    showAlert("Please select a valid date and time for your booking.");
                    return;
                }

                // Create booking in the database with user-selected values
                LocalDateTime startDateTime = LocalDateTime.of(selectedDate, selectedStartTime);
                LocalDateTime endDateTime = LocalDateTime.of(selectedDate, selectedEndTime);

                // Get current user ID from session and hall ID
                String hallId = selectedHall.getHallId();

                // Check if the hall is available for the selected time slot
                boolean isAvailable = TaskUtils
                        .executeTask(bookingService.isHallAvailable(hallId, startDateTime, endDateTime));
                if (!isAvailable) {
                    showAlert("This time slot is already booked. Please select a different time.");
                    return;
                }

                // Execute the booking creation task
                // Note: BookingService.createBooking automatically gets the current user ID
                // from SessionManager
                TaskUtils.executeTaskWithProgress(
                        bookingService.createBooking(hallId, startDateTime, endDateTime),
                        booking -> {
                            Platform.runLater(() -> {
                                // Process booking with specialRequests and other details
                                showAlert("Booking confirmed for Hall " + selectedHall.getHallId() +
                                        "\nBooking ID: " + booking.getBookingId() +
                                        "\nDate: "
                                        + selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")) +
                                        "\nTime: " + selectedStartTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                                        + " - " +
                                        selectedEndTime.format(DateTimeFormatter.ofPattern("h:mm a")) +
                                        "\nTotal Cost: $" + String.format("%.2f", booking.getTotalCost()) +
                                        "\nSpecial Requests: " +
                                        (specialRequests == null || specialRequests.trim().isEmpty() ? "None"
                                                : specialRequests));

                                // Refresh the hall list to show updated availability
                                loadHalls();
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                showAlert("Error creating booking: " + error.getMessage());
                            });
                        });
            } else {
                System.out.println("Booking Cancelled.");
            }
        } catch (IOException e) {
            String errorMsg = "Error processing your booking: " + e.getMessage();
            showAlert(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
        } catch (Exception e) {
            String errorMsg = "An unexpected error occurred: " + e.getMessage();
            showAlert(errorMsg);
            System.err.println(errorMsg);
            e.printStackTrace();
        }
    }

    /**
     * Handles the reset button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleReset(ActionEvent event) {
        searchDatePicker.setValue(LocalDate.now());
        hallTypeComboBox.setValue(null);
        capacityField.clear();
        filteredHalls.setPredicate(hall -> true);
    }

    /**
     * Shows an alert dialog with the specified message.
     * 
     * @param message the message to display
     */
    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Hall Booking System");
            alert.setHeaderText(null);
            alert.setContentText(message);

            // Set the alert to be always on top
            alert.initModality(Modality.APPLICATION_MODAL);

            // Add custom styles if available
            try {
                DialogPane dialogPane = alert.getDialogPane();
                URL stylesheet = getClass().getResource("/com/group4/styles/dialogs.css");
                if (stylesheet != null) {
                    dialogPane.getStylesheets().add(stylesheet.toExternalForm());
                    dialogPane.getStyleClass().add("dialog-pane");
                }
            } catch (Exception e) {
                // Ignore style loading errors
                System.err.println("Warning: Could not load dialog styles: " + e.getMessage());
            }

            alert.showAndWait();
        });
    }
}