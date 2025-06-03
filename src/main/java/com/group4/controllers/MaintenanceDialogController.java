package com.group4.controllers;

import com.group4.models.Maintenance;
import com.group4.services.MaintenanceService;
import com.group4.utils.AlertUtils;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Controller for the Maintenance Dialog.
 * Allows scheduling maintenance for a hall.
 */
public class MaintenanceDialogController {

    @FXML
    private Label hallIdLabel;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private String hallId;
    private String maintenanceId;
    private boolean isEditMode = false;
    private final MaintenanceService maintenanceService = new MaintenanceService();

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        // Set minimum date to today
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        
        // Add listeners to validate dates
        startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (endDatePicker.getValue() != null && newDate != null && newDate.isAfter(endDatePicker.getValue())) {
                endDatePicker.setValue(newDate.plusDays(1));
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (startDatePicker.getValue() != null && newDate != null && newDate.isBefore(startDatePicker.getValue())) {
                startDatePicker.setValue(newDate.minusDays(1));
            }
        });
        
        // Update UI based on edit mode
        updateUI();
    }
    
    private void updateUI() {
        if (isEditMode) {
            saveButton.setText("Update Maintenance");
        } else {
            saveButton.setText("Schedule Maintenance");
        }
    }

    /**
     * Sets up the dialog for adding a new maintenance record.
     * 
     * @param hallId The ID of the hall
     */
    public void setupForAdd(String hallId) {
        this.hallId = hallId;
        this.isEditMode = false;
        hallIdLabel.setText(hallId);
        updateUI();
    }
    
    /**
     * Sets up the dialog for editing an existing maintenance record.
     * 
     * @param maintenance The maintenance record to edit
     */
    public void setupForEdit(Maintenance maintenance) {
        this.maintenanceId = maintenance.getMaintenanceId();
        this.hallId = maintenance.getHallId();
        this.isEditMode = true;
        
        hallIdLabel.setText(hallId);
        descriptionTextArea.setText(maintenance.getDescription());
        startDatePicker.setValue(maintenance.getStartTime().toLocalDate());
        endDatePicker.setValue(maintenance.getEndTime().toLocalDate());
        
        updateUI();
    }

    /**
     * Handles the save button click.
     * Schedules maintenance for the selected hall.
     */
    @FXML
    private void handleSave() {
        // Validate inputs
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both start and end dates");
            return;
        }

        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showError("End date must be after start date");
            return;
        }

        String description = descriptionTextArea.getText().trim();
        if (description.isEmpty()) {
            showError("Please enter a description for the maintenance");
            return;
        }

        // Disable UI during save
        setFormDisabled(true);

        // Convert LocalDate to LocalDateTime
        LocalDateTime startDateTime = LocalDateTime.of(startDatePicker.getValue(), LocalTime.MIDNIGHT);
        LocalDateTime endDateTime = LocalDateTime.of(endDatePicker.getValue(), LocalTime.MAX);
        
        if (isEditMode) {
            // Update existing maintenance
            Maintenance maintenance = new Maintenance(maintenanceId, hallId, description, startDateTime, endDateTime);
            Task<Boolean> updateTask = maintenanceService.updateMaintenance(maintenance);
            
            // Set up task handlers
            updateTask.setOnSucceeded(event -> {
                boolean success = updateTask.getValue();
                if (success) {
                    Platform.runLater(() -> {
                        AlertUtils.showInfo("Success", "Maintenance schedule updated successfully.");
                        closeDialog();
                    });
                } else {
                    Platform.runLater(() -> AlertUtils.showError("Error", "Failed to update maintenance schedule."));
                    setFormDisabled(false);
                }
            });
            
            updateTask.setOnFailed(event -> {
                Platform.runLater(() -> AlertUtils.showError("Error", "Failed to update maintenance schedule: " + 
                    (updateTask.getException() != null ? updateTask.getException().getMessage() : "Unknown error")));
                setFormDisabled(false);
            });
            
            // Execute the task
            new Thread(updateTask).start();
        } else {
            // Add new maintenance
            Task<Maintenance> addTask = maintenanceService.addMaintenance(hallId, description, startDateTime, endDateTime);
            
            // Set up task handlers
            addTask.setOnSucceeded(event -> {
                Platform.runLater(() -> {
                    AlertUtils.showInfo("Success", "Maintenance scheduled successfully.");
                    closeDialog();
                });
            });
            
            addTask.setOnFailed(event -> {
                Platform.runLater(() -> AlertUtils.showError("Error", "Failed to schedule maintenance: " + 
                    (addTask.getException() != null ? addTask.getException().getMessage() : "Unknown error")));
                setFormDisabled(false);
            });
            
            // Execute the task
            new Thread(addTask).start();
        }
    }

    /**
     * Handles the cancel button click.
     * Closes the dialog without saving.
     */
    @FXML
    private void handleCancel() {
        closeDialog();
    }

    /**
     * Closes the dialog.
     */
    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an error message.
     * 
     * @param message The error message to show
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    /**
     * Sets the enabled state of the controls.
     * 
     * @param disabled True to disable controls, false to enable
     */
    private void setFormDisabled(boolean disabled) {
        startDatePicker.setDisable(disabled);
        endDatePicker.setDisable(disabled);
        descriptionTextArea.setDisable(disabled);
        saveButton.setDisable(disabled);
        cancelButton.setDisable(disabled);
    }
}
