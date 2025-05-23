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
 * Controller for managing hall maintenance schedules.
 */
public class MaintenanceController implements Initializable {

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
    private TableView<Availability> maintenanceTable;

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

    private final AvailabilityService availabilityService = new AvailabilityService();
    private final HallService hallService = new HallService();
    private ObservableList<Availability> maintenanceList = FXCollections.observableArrayList();
    private ObservableList<Hall> hallsList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize table columns
        idColumn.setCellValueFactory(cellData -> cellData.getValue().availabilityIdProperty());
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        startDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().startDateTimeProperty());
        endDateTimeColumn.setCellValueFactory(cellData -> cellData.getValue().endDateTimeProperty());
        remarksColumn.setCellValueFactory(cellData -> cellData.getValue().remarksProperty());

        // Load halls for the combo box
        loadHalls();

        // Load maintenance schedules
        loadMaintenanceSchedules();

        // Set up selection listener
        maintenanceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayMaintenanceDetails(newSelection);
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
     * Loads all maintenance schedule entries.
     */
    private void loadMaintenanceSchedules() {
        try {
            Task<List<Availability>> task = new Task<List<Availability>>() {
                @Override
                protected List<Availability> call() throws Exception {
                    return availabilityService.getAllMaintenanceSchedules();
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    maintenances -> {
                        maintenanceList.clear();
                        maintenanceList.addAll(maintenances);
                        maintenanceTable.setItems(maintenanceList);
                    },
                    error -> showAlert("Error loading maintenance schedules: " + error.getMessage()));
        } catch (Exception e) {
            showAlert("Error loading maintenance schedules: " + e.getMessage());
        }
    }

    /**
     * Displays the details of a maintenance schedule entry in the form.
     * 
     * @param maintenance the maintenance schedule entry to display
     */
    private void displayMaintenanceDetails(Availability maintenance) {
        // Set hall
        for (Hall hall : hallsList) {
            if (hall.getHallId().equals(maintenance.getHallId())) {
                hallComboBox.setValue(hall);
                break;
            }
        }

        // Set dates and times
        LocalDateTime startDateTime = maintenance.getStartDateTime();
        LocalDateTime endDateTime = maintenance.getEndDateTime();

        startDatePicker.setValue(startDateTime.toLocalDate());
        endDatePicker.setValue(endDateTime.toLocalDate());

        startTimeField.setText(String.format("%02d:%02d", startDateTime.getHour(), startDateTime.getMinute()));
        endTimeField.setText(String.format("%02d:%02d", endDateTime.getHour(), endDateTime.getMinute()));

        remarksArea.setText(maintenance.getRemarks());
    }

    /**
     * Handles the add maintenance button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleAddMaintenance(ActionEvent event) {
        try {
            validateInput();

            Hall selectedHall = hallComboBox.getValue();
            LocalDateTime startDateTime = getStartDateTime();
            LocalDateTime endDateTime = getEndDateTime();
            String remarks = remarksArea.getText();

            Task<Availability> task = new Task<Availability>() {
                @Override
                protected Availability call() throws Exception {
                    return availabilityService.addMaintenance(
                            selectedHall.getHallId(), startDateTime, endDateTime, remarks);
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    maintenance -> {
                        maintenanceList.add(maintenance);
                        clearForm();
                        showAlert("Maintenance schedule added successfully!");
                    },
                    error -> showAlert("Error adding maintenance schedule: " + error.getMessage()));
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    /**
     * Handles the delete maintenance button click.
     * 
     * @param event the action event
     */
    @FXML
    private void handleDeleteMaintenance(ActionEvent event) {
        Availability selectedMaintenance = maintenanceTable.getSelectionModel().getSelectedItem();
        if (selectedMaintenance == null) {
            showAlert("Please select a maintenance schedule to delete.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Maintenance Schedule");
        confirmAlert.setContentText("Are you sure you want to delete this maintenance schedule?");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    availabilityService.deleteAvailability(selectedMaintenance.getAvailabilityId());
                    return null;
                }
            };

            TaskUtils.executeTaskWithProgress(task,
                    result -> {
                        maintenanceList.remove(selectedMaintenance);
                        clearForm();
                        showAlert("Maintenance schedule deleted successfully!");
                    },
                    error -> showAlert("Error deleting maintenance schedule: " + error.getMessage()));
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