package com.group4.controllers;

import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.services.HallService;
import com.group4.utils.TaskUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.beans.property.SimpleStringProperty;

/**
 * Controller for the Hall Management screen.
 */
public class HallManagementController {

    @FXML
    private TextField hallIdField;

    @FXML
    private ComboBox<String> hallTypeComboBox;

    @FXML
    private TextField capacityField;

    @FXML
    private TextField ratePerHourField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button backButton;

    @FXML
    private TableView<Hall> hallsTable;

    @FXML
    private TableColumn<Hall, String> hallIdColumn;

    @FXML
    private TableColumn<Hall, String> hallTypeColumn;

    @FXML
    private TableColumn<Hall, Integer> capacityColumn;

    @FXML
    private TableColumn<Hall, Double> rateColumn;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button maintenanceButton;

    private HallService hallService;
    private ObservableList<Hall> hallList = FXCollections.observableArrayList();
    private boolean isEditMode = false;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        hallService = new HallService();

        // Populate hall type dropdown
        hallTypeComboBox.setItems(FXCollections.observableArrayList(
                "AUDITORIUM", "BANQUET", "MEETING_ROOM"));

        // Configure table columns
        hallIdColumn.setCellValueFactory(cellData -> cellData.getValue().hallIdProperty());
        hallTypeColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().toString()));
        capacityColumn.setCellValueFactory(cellData -> cellData.getValue().capacityProperty().asObject());
        rateColumn.setCellValueFactory(cellData -> cellData.getValue().ratePerHourProperty().asObject());

        // Set table data source
        hallsTable.setItems(hallList);

        // Add table selection listener
        hallsTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        editButton.setDisable(false);
                        deleteButton.setDisable(false);
                        maintenanceButton.setDisable(false);
                    } else {
                        editButton.setDisable(true);
                        deleteButton.setDisable(true);
                        maintenanceButton.setDisable(true);
                    }
                });

        // Disable action buttons initially
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        maintenanceButton.setDisable(true);

        // Add text change listeners to clear error
        capacityField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        ratePerHourField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setVisible(false);
        });

        // Load halls from the file
        loadHalls();
    }

    /**
     * Loads all halls from the file.
     */
    private void loadHalls() {
        TaskUtils.executeTaskWithProgress(
                hallService.getAllHalls(),
                halls -> {
                    hallList.clear();
                    hallList.addAll(halls);
                },
                error -> {
                    Platform.runLater(() -> {
                        showError("Failed to load halls: " + error.getMessage());
                    });
                });
    }

    /**
     * Handles the save button click.
     */
    @FXML
    private void handleSave() {
        // Get form values
        String hallTypeStr = hallTypeComboBox.getValue();
        String capacityStr = capacityField.getText().trim();
        String rateStr = ratePerHourField.getText().trim();

        // Validate inputs
        if (hallTypeStr == null || capacityStr.isEmpty() || rateStr.isEmpty()) {
            showError("All fields are required");
            return;
        }

        try {
            HallType hallType = HallType.valueOf(hallTypeStr);
            int capacity = Integer.parseInt(capacityStr);
            double rate = Double.parseDouble(rateStr);

            // Validate capacity and rate
            if (capacity <= 0) {
                showError("Capacity must be greater than zero");
                return;
            }

            if (rate <= 0) {
                showError("Rate per hour must be greater than zero");
                return;
            }

            // Disable buttons during operation
            setControlsDisabled(true);
            Hall selectedHall;
            if (isEditMode) {
                // Update existing hall
                selectedHall = hallsTable.getSelectionModel().getSelectedItem();
                selectedHall.setType(hallType);
                selectedHall.setCapacity(capacity);
                selectedHall.setRatePerHour(rate);

                TaskUtils.executeTaskWithProgress(
                        hallService.updateHall(selectedHall),
                        success -> {
                            Platform.runLater(() -> {
                                loadHalls();
                                clearForm();
                                setControlsDisabled(false);
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                showError("Error updating hall: " + error.getMessage());
                                setControlsDisabled(false);
                            });
                        });
            } else {
                // Add new hall
                TaskUtils.executeTaskWithProgress(
                        hallService.addHall(hallType, capacity, rate),
                        hall -> {
                            Platform.runLater(() -> {
                                // Reload halls from file to get the new hall
                                loadHalls();
                                clearForm();
                                setControlsDisabled(false);
                            });
                        },
                        error -> {
                            Platform.runLater(() -> {
                                showError("Error adding hall: " + error.getMessage());
                                setControlsDisabled(false);
                            });
                        });
            }
        } catch (NumberFormatException e) {
            showError("Capacity and rate must be valid numbers");
        } catch (IllegalArgumentException e) {
            showError("Invalid hall type");
        }
    }

    /**
     * Handles the clear button click.
     */
    @FXML
    private void handleClear() {
        clearForm();
    }

    /**
     * Handles the edit button click.
     */
    @FXML
    private void handleEdit() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall != null) {
            // Populate form with selected hall data
            hallIdField.setText(selectedHall.getHallId());
            hallTypeComboBox.setValue(selectedHall.getType().name());
            capacityField.setText(String.valueOf(selectedHall.getCapacity()));
            ratePerHourField.setText(String.valueOf(selectedHall.getRatePerHour()));

            // Enable edit mode
            isEditMode = true;
            saveButton.setText("Update");
        }
    }

    /**
     * Handles the delete button click.
     */
    @FXML
    private void handleDelete() {
        Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
        if (selectedHall != null) {
            TaskUtils.executeTaskWithProgress(
                    hallService.deleteHall(selectedHall.getHallId()),
                    success -> {
                        if (success) {
                            loadHalls();
                            clearForm();
                        } else {
                            showError("Failed to delete hall. Please try again.");
                        }
                    },
                    error -> showError("Error deleting hall: " + error.getMessage())
            );
        }
    }
    
    /**
     * Handles the maintenance button click event.
     * Opens the maintenance dialog for the selected hall.
     */
@FXML
private void handleMaintenance() {
    Hall selectedHall = hallsTable.getSelectionModel().getSelectedItem();
    if (selectedHall != null) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/group4/view/MaintenanceDialog.fxml"));
            Parent root = loader.load();
            
            MaintenanceDialogController controller = loader.getController();
            controller.setupForAdd(selectedHall.getHallId());
            
            Stage dialog = new Stage();
            dialog.setTitle("Schedule Maintenance - " + selectedHall.getHallId());
            dialog.setScene(new Scene(root, 500, 400));
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.showAndWait();
            
            // Refresh the halls table after maintenance is scheduled
            loadHalls();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening maintenance dialog: " + e.getMessage());
        }
    } else {
        showError("Please select a hall to schedule maintenance");
    }
}

    /**
     * Clears the form.
     */
    private void clearForm() {
        hallIdField.clear();
        hallTypeComboBox.setValue(null);
        capacityField.clear();
        ratePerHourField.clear();
        errorLabel.setVisible(false);

        // Reset edit mode
        isEditMode = false;
        saveButton.setText("Save");
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
    private void setControlsDisabled(boolean disabled) {
        hallTypeComboBox.setDisable(disabled);
        capacityField.setDisable(disabled);
        ratePerHourField.setDisable(disabled);
        saveButton.setDisable(disabled);
        clearButton.setDisable(disabled);
        editButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
    }
}