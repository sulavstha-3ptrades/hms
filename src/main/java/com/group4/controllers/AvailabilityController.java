package com.group4.controllers;

import com.group4.models.Availability;
import com.group4.models.Hall;
import com.group4.services.AvailabilityService;
import com.group4.services.HallService;
import com.group4.utils.TaskUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for managing hall availability.
 */
public class AvailabilityController implements Initializable {

    @FXML
    private ComboBox<Hall> hallComboBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField startTimeField;

    @FXML
    private TextField endTimeField;

    @FXML
    private TextArea remarksArea;

    @FXML
    private CheckBox isAvailableCheckbox;

    @FXML
    private TableView<Availability> availabilityTable;

    @FXML
    private TableColumn<Availability, String> idColumn;

    @FXML
    private TableColumn<Availability, String> hallIdColumn;

    @FXML
    private TableColumn<Availability, LocalDateTime> startDateTimeColumn;

    @FXML
    private TableColumn<Availability, LocalDateTime> endDateTimeColumn;

    @FXML
    private TableColumn<Availability, String> remarksColumn;

    @FXML
    private TableColumn<Availability, Boolean> isAvailableColumn;

    private final AvailabilityService availabilityService = new AvailabilityService();
    private final HallService hallService = new HallService();
    private ObservableList<Availability> availabilityList = FXCollections.observableArrayList();
    private ObservableList<Hall> hallsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().availabilityIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        startDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().startDateTimeProperty());
        endDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endDateTimeProperty());
        remarksColumn.setCellValueFactory(cellData -> cellData.getValue().remarksProperty());
        isAvailableColumn.setCellValueFactory(cellData -> cellData.getValue().availableProperty());

        // Load halls for the combo box
        loadHalls();

        // Load availabilities
        loadAvailabilities();

        // Set up selection listener
        availabilityTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayAvailabilityDetails(newSelection);
            }
        });
    }

    /**
     * Loads all halls for the combo box.
     */
    private void loadHalls() {
        Task<List<Hall>> hallsTask = hallService.getAllHalls();

        TaskUtils.executeTaskWithProgress(hallsTask,
                halls -> {
                    hallsList.clear();
                    hallsList.addAll(halls);
                    hallComboBox.setItems(hallsList);
                },
                error -> showAlert("Error loading halls: " + error.getMessage()));
    }

    /**
     * Loads all availability entries.
     */
    private void loadAvailabilities() {
        try {
            Task<List<Availability>> task = new Task<List<Availability>>() {
                @Override
                protected List<Availability> call() throws Exception {
                    return availabilityService.getAllAvailabilities();
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    availabilities -> {
                        availabilityList.clear();
                        availabilityList.addAll(availabilities);
                        availabilityTable.setItems(availabilityList);
                    },
                    error -> showAlert("Error loading availabilities: " + error.getMessage()));
        } catch (Exception e) {
            showAlert("Error loading availabilities: " + e.getMessage());
        }
    }

    /**
     * Displays the details of an availability entry in the form.
     * 
     * @param availability the availability entry to display
     */
    private void displayAvailabilityDetails(Availability availability) {
        // Set hall
        for (Hall hall : hallsList) {
            if (hall.getHallId().equals(availability.getHallId())) {
                hallComboBox.setValue(hall);
                break;
            }
        }

        // Set dates and times
        LocalDateTime startDateTime = availability.getStartDateTime();
        LocalDateTime endDateTime = availability.getEndDateTime();

        startDatePicker.setValue(startDateTime.toLocalDate());
        endDatePicker.setValue(endDateTime.toLocalDate());

        startTimeField.setText(String.format("%02d:%02d", startDateTime.getHour(), startDateTime.getMinute()));
        endTimeField.setText(String.format("%02d:%02d", endDateTime.getHour(), endDateTime.getMinute()));

        remarksArea.setText(availability.getRemarks());
        isAvailableCheckbox.setSelected(availability.isAvailable());
    }

    /**
     * Handles the add availability button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAddAvailability(ActionEvent event) {
        try {
            validateInput();

            Hall selectedHall = hallComboBox.getValue();
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();
            String remarks = remarksArea.getText();
            boolean isAvailable = isAvailableCheckbox.isSelected();

            Task<Availability> task = new Task<Availability>() {
                @Override
                protected Availability call() throws Exception {
                    return availabilityService.addAvailability(
                            selectedHall.getHallId(), startDateTime, endDateTime, remarks, isAvailable);
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    availability -> {
                        availabilityList.add(availability);
                        clearForm();
                        showAlert("Availability added successfully!");
                    },
                    error -> showAlert("Error adding availability: " + error.getMessage()));
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    /**
     * Handles the update availability button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleUpdateAvailability(ActionEvent event) {
        Availability selectedAvailability = availabilityTable.getSelectionModel().getSelectedItem();
        if (selectedAvailability == null) {
            showAlert("Please select an availability entry to update.");
            return;
        }

        try {
            validateInput();

            Hall selectedHall = hallComboBox.getValue();
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();
            String remarks = remarksArea.getText();
            boolean isAvailable = isAvailableCheckbox.isSelected();

            selectedAvailability.setHallId(selectedHall.getHallId());
            selectedAvailability.setStartDateTime(startDateTime);
            selectedAvailability.setEndDateTime(endDateTime);
            selectedAvailability.setRemarks(remarks);
            selectedAvailability.setAvailable(isAvailable);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    availabilityService.updateAvailability(selectedAvailability);
                    return null;
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    result -> {
                        loadAvailabilities(); // Refresh the table
                        clearForm();
                        showAlert("Availability updated successfully!");
                    },
                    error -> showAlert("Error updating availability: " + error.getMessage()));
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    /**
     * Handles the delete availability button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleDeleteAvailability(ActionEvent event) {
        Availability selectedAvailability = availabilityTable.getSelectionModel().getSelectedItem();
        if (selectedAvailability == null) {
            showAlert("Please select an availability entry to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Availability");
        confirmAlert.setContentText("Are you sure you want to delete this availability entry?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    availabilityService.deleteAvailability(selectedAvailability.getAvailabilityId());
                    return null;
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    result -> {
                        availabilityList.remove(selectedAvailability);
                        clearForm();
                        showAlert("Availability deleted successfully!");
                    },
                    error -> showAlert("Error deleting availability: " + error.getMessage()));
        }
    }

    /**
     * Validates the user input.
     * 
     * @throws IllegalArgumentException if the input is invalid
     */
    private void validateInput() throws IllegalArgumentException {
        if (hallComboBox.getValue() == null) {
            throw new IllegalArgumentException("Please select a hall.");
        }

        if (startDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Please select a start date.");
        }

        if (endDatePicker.getValue() == null) {
            throw new IllegalArgumentException("Please select an end date.");
        }

        if (startTimeField.getText().isEmpty()) {
            throw new IllegalArgumentException("Please enter a start time.");
        }

        if (endTimeField.getText().isEmpty()) {
            throw new IllegalArgumentException("Please enter an end time.");
        }

        try {
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();

            if (startDateTime.isAfter(endDateTime)) {
                throw new IllegalArgumentException("Start date/time must be before end date/time.");
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            } else {
                throw new IllegalArgumentException("Invalid date/time format. Please use HH:MM format for times.");
            }
        }
    }

    /**
     * Gets the start date and time from the form.
     * 
     * @return the start date and time
     */
    private LocalDateTime getStartDateTime() {
        LocalDate startDate = startDatePicker.getValue();
        String[] timeParts = startTimeField.getText().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        return LocalDateTime.of(startDate, LocalTime.of(hour, minute));
    }

    /**
     * Gets the end date and time from the form.
     * 
     * @return the end date and time
     */
    private LocalDateTime getEndDateTime() {
        LocalDate endDate = endDatePicker.getValue();
        String[] timeParts = endTimeField.getText().split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);
        return LocalDateTime.of(endDate, LocalTime.of(hour, minute));
    }

    /**
     * Clears the form.
     */
    private void clearForm() {
        hallComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        startTimeField.clear();
        endTimeField.clear();
        remarksArea.clear();
        isAvailableCheckbox.setSelected(true);
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