package com.group4.controllers;

import com.group4.models.Availability;
import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.services.AvailabilityService;
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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Controller for hall search and booking.
 */
public class HallBookingController implements Initializable {

    @FXML
    private DatePicker searchDatePicker;

    @FXML
    private ComboBox<HallType> hallTypeComboBox;

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
    private ObservableList<Hall> allHalls = FXCollections.observableArrayList();
    private FilteredList<Hall> filteredHalls;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize hall type combo box
        hallTypeComboBox.setItems(FXCollections.observableArrayList(HallType.values()));
        hallTypeComboBox.getItems().add(0, null); // Add null option for "Any type"

        // Initialize table columns
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        rateColumn.setCellValueFactory(cellData -> cellData.getValue().ratePerHourProperty().asObject());

        // Load all halls
        loadHalls();

        // Set default date to today
        searchDatePicker.setValue(LocalDate.now());
    }

    /**
     * Loads all halls.
     */
    private void loadHalls() {
        Task<List<Hall>> hallsTask = hallService.getAllHalls();

        TaskUtils.executeTaskWithProgress(hallsTask,
                halls -> {
                    allHalls.clear();
                    allHalls.addAll(halls);
                    filteredHalls = new FilteredList<>(allHalls);
                    hallTable.setItems(filteredHalls);
                },
                error -> showAlert("Error loading halls: " + error.getMessage()));
    }

    /**
     * Handles the search button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        // Get search criteria
        LocalDate searchDate = searchDatePicker.getValue();
        HallType hallType = hallTypeComboBox.getValue();
        int minCapacity = 0;

        try {
            if (!capacityField.getText().isEmpty()) {
                minCapacity = Integer.parseInt(capacityField.getText());
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid number for capacity.");
            return;
        }

        // Build predicate for hall filtering
        final Predicate<Hall> initialPredicate = hall -> true; // Start with accepting all halls

        // Filter by hall type if selected
        final HallType selectedType = hallType;
        final Predicate<Hall> typeFilteredPredicate;
        if (selectedType != null) {
            typeFilteredPredicate = initialPredicate.and(hall -> hall.getType() == selectedType);
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
        if (searchDate != null) {
            final LocalDate selectedDate = searchDate;
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
        Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert("Please select a hall.");
            return;
        }

        try {
            // Load the booking confirmation FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/group4/view/BookingConfirmation.fxml"));
            Parent root = loader.load();

            // Get the controller and set the hall
            BookingConfirmationController controller = loader.getController();
            controller.setHall(selectedHall);

            // Create a new stage for the booking confirmation
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL);
            bookingStage.setTitle("Book Hall - " + selectedHall.getHallId());
            bookingStage.setScene(new Scene(root));
            bookingStage.showAndWait();

            // Refresh the hall list after booking
            loadHalls();
        } catch (IOException e) {
            showAlert("Error opening booking confirmation: " + e.getMessage());
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
     * Shows an alert dialog.
     * 
     * @param message the message to show
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}