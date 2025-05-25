package com.group4.controllers;

import com.group4.models.Availability;
import com.group4.models.BookingStatus;
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
    private ComboBox<BookingStatus> statusComboBox;

    @FXML
    private TableColumn<Hall, BookingStatus> statusColumn;

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

        // Initialize status combo box
        statusComboBox.setItems(FXCollections.observableArrayList(BookingStatus.values()));
        statusComboBox.getItems().add(0, null); // Add null option for "Any status"

        // Initialize table columns
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        rateColumn.setCellValueFactory(cellData -> cellData.getValue().ratePerHourProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

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

        // Get search criteria
        LocalDate searchDate = searchDatePicker.getValue();
        LocalDate today = LocalDate.now();

        // Validate search date
        if (searchDate == null || searchDate.isBefore(today)) {
            showAlert("Please select a valid date from today onwards.");
            searchDatePicker.setValue(today);
            return;
        }

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
        BookingStatus selectedStatus = statusComboBox.getValue();
        final HallType selectedType = hallType;
        
        // Apply hall type filter
        final Predicate<Hall> typeFilteredPredicate;
        if (selectedType != null) {
            typeFilteredPredicate = initialPredicate.and(hall -> hall.getType() == selectedType);
        } else {
            typeFilteredPredicate = initialPredicate;
        }

        // Apply status filter
        final Predicate<Hall> statusFilteredPredicate;
        if (selectedStatus != null) {
            statusFilteredPredicate = typeFilteredPredicate.and(hall -> hall.getStatus() == selectedStatus);
        } else {
            statusFilteredPredicate = typeFilteredPredicate;
        }

        // Filter by minimum capacity
        final int selectedCapacity = minCapacity;
        final Predicate<Hall> capacityFilteredPredicate;
        if (selectedCapacity > 0) {
            capacityFilteredPredicate = statusFilteredPredicate.and(hall -> hall.getCapacity() >= selectedCapacity);
        } else {
            capacityFilteredPredicate = statusFilteredPredicate;
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
        Hall selectedHall = hallTable.getSelectionModel().getSelectedItem();
        if (selectedHall == null) {
            showAlert("Please select a hall.");
            return;
        }

        // Validate booking date
        LocalDate bookingDate = searchDatePicker.getValue();
        LocalDate today = LocalDate.now();
        if (bookingDate == null || bookingDate.isBefore(today)) {
            showAlert("Cannot book a hall for past dates. Please select a valid date.");
            searchDatePicker.setValue(today);
            return;
        }

        try {
            // Load the booking confirmation dialog from resources
            URL fxmlUrl = getClass().getResource("/com/group4/view/BookingConfirmation.fxml");
            if (fxmlUrl == null) {
                showAlert("Error: Could not find the booking form template. Please contact system administrator.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root;
            try {
                root = loader.load();
            } catch (IOException e) {
                showAlert("Error loading booking form: " + e.getMessage() + 
                         "\n\nPlease ensure the application is properly installed and try again.");
                return;
            }

            // Get the controller and set the hall
            BookingConfirmationController controller = loader.getController();
            if (controller == null) {
                showAlert("Error: Could not initialize the booking form. Please contact system administrator.");
                return;
            }
            controller.setHall(selectedHall);

            // Show the dialog
            Stage bookingStage = new Stage();
            bookingStage.initModality(Modality.APPLICATION_MODAL);
            bookingStage.setTitle("Book Hall - " + selectedHall.getHallId());
            bookingStage.setScene(new Scene(root));
            bookingStage.showAndWait();

            // Refresh the hall list after booking
            loadHalls();
        } catch (Exception e) {
            showAlert("An unexpected error occurred while opening the booking form: " + 
                     e.getMessage() + "\n\nPlease try again or contact system administrator if the problem persists.");
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
        statusComboBox.setValue(null);
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