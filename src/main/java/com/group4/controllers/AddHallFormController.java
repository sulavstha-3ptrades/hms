package com.group4.controllers;

import com.group4.models.Hall;
import com.group4.models.HallType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.Node;

public class AddHallFormController {



    @FXML
    private TextField hallIdField;
    @FXML
    private ComboBox<HallType> hallTypeComboBox;
    @FXML
    private TextField capacityField;
    @FXML
    private TextField ratePerHourField;
    @FXML
    private Label errorLabel;
    @FXML
    private DialogPane dialogPane;

    private boolean isEditMode = false;
    private Hall hallToEdit;

    // Getters for form fields to be accessed from AdminDashboardController
    public ComboBox<HallType> getHallTypeComboBox() {
        return hallTypeComboBox;
    }

    public TextField getCapacityField() {
        return capacityField;
    }

    public TextField getRatePerHourField() {
        return ratePerHourField;
    }

    public Label getErrorLabel() {
        return errorLabel;
    }

    /**
     * Initialize the form in edit mode with the given hall's data
     * 
     * @param hall The hall to edit
     */
    /**
     * Initializes the form in edit mode with the given hall's data
     * 
     * @param hall The hall to edit
     */
    public void initEditMode(Hall hall) {
        if (hall == null)
            return;

        isEditMode = true;
        this.hallToEdit = hall;

        // Populate form with hall data
        hallIdField.setText(hall.getHallId());
        hallTypeComboBox.setValue(hall.getType());
        capacityField.setText(String.valueOf(hall.getCapacity()));
        ratePerHourField.setText(String.format("%.2f", hall.getRatePerHour()));

        // Disable hall ID field in edit mode since we don't allow changing the ID
        hallIdField.setDisable(true);

        // Window title is now handled by the setDialog method
    }

    @FXML
    public void initialize() {
        try {
            // Initialize hall type combo box
            if (hallTypeComboBox != null) {
                hallTypeComboBox.setItems(FXCollections.observableArrayList(HallType.values()));
            }

            // Add text formatters for numeric fields
            if (capacityField != null) {
                capacityField.setTextFormatter(createNumberTextFormatter());
            }
            if (ratePerHourField != null) {
                ratePerHourField.setTextFormatter(createDecimalTextFormatter());
            }

            // Only generate hall ID if not in edit mode
            if (!isEditMode && hallIdField != null) {
                generateHallId();
            }

            // Set up the form validation
            setupFormValidation();

            // Schedule dialog validation to run after the UI is fully initialized
            Platform.runLater(this::setupDialogValidation);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error initializing form: " + e.getMessage());
        }
    }

    private void setupDialogValidation() {
        if (dialogPane == null) {
            // If dialogPane is still null, try again later
            Platform.runLater(this::setupDialogValidation);
            return;
        }

        try {
            // Get the dialog and its buttons
            Node saveButton = dialogPane.lookupButton(ButtonType.OK);

            if (saveButton != null) {
                // Add validation listeners
                hallIdField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState(saveButton));
                hallTypeComboBox.valueProperty()
                        .addListener((obs, oldVal, newVal) -> updateSaveButtonState(saveButton));
                capacityField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState(saveButton));
                ratePerHourField.textProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState(saveButton));

                // Initial validation
                updateSaveButtonState(saveButton);

                // Handle dialog close request
                if (dialogPane.getScene() != null && dialogPane.getScene().getWindow() != null) {
                    dialogPane.getScene().getWindow().setOnCloseRequest(event -> {
                        if (!isFormValid()) {
                            event.consume();
                        }
                    });
                }
            } else {
                // Try again if saveButton is not available yet
                Platform.runLater(this::setupDialogValidation);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Retry on any exception
            Platform.runLater(this::setupDialogValidation);
        }
    }

    private void updateSaveButtonState(Node saveButton) {
        boolean isValid = isFormValid();
        saveButton.setDisable(!isValid);
    }

    /**
     * Displays an error message in the error label
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void generateHallId() {
        // Generate a simple timestamp-based ID (can be replaced with a more
        // sophisticated ID generator)
        String timestamp = String.valueOf(System.currentTimeMillis());
        String hallId = "HALL-" + timestamp.substring(timestamp.length() - 6);
        hallIdField.setText(hallId);
    }

    private void setupFormValidation() {
        // Clear any existing error messages when fields change
        hallIdField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        hallTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearError());
        capacityField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        ratePerHourField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    private void clearError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        errorLabel.setText("");
    }

    /**
     * Gets the hall data from the form
     * 
     * @return Hall object with form data, or null if form is invalid
     */
    public Hall getHall() {
        if (!isFormValid()) {
            return null;
        }

        // If we're in edit mode, update the existing hall
        if (isEditMode && hallToEdit != null) {
            // Update the existing hall's properties
            hallToEdit.setType(hallTypeComboBox.getValue());
            hallToEdit.setCapacity(Integer.parseInt(capacityField.getText().trim()));
            hallToEdit.setRatePerHour(Double.parseDouble(ratePerHourField.getText().trim()));
            return hallToEdit;
        }

        // Otherwise, create a new hall
        Hall hall = new Hall();
        hall.setHallId(hallIdField.getText().trim());
        hall.setType(hallTypeComboBox.getValue());
        hall.setCapacity(Integer.parseInt(capacityField.getText().trim()));
        hall.setRatePerHour(Double.parseDouble(ratePerHourField.getText().trim()));

        return hall;
    }

    public boolean isFormValid() {
        errorLabel.setText("");

        // Validate hall ID (only for new halls)
        if (!isEditMode && hallIdField.getText().trim().isEmpty()) {
            errorLabel.setText("Hall ID is required.");
            return false;
        }

        // Validate hall type
        if (hallTypeComboBox.getValue() == null) {
            errorLabel.setText("Please select a hall type.");
            return false;
        }

        // Validate capacity
        if (capacityField.getText().trim().isEmpty()) {
            errorLabel.setText("Capacity is required.");
            return false;
        }

        try {
            int capacity = Integer.parseInt(capacityField.getText().trim());
            if (capacity <= 0) {
                errorLabel.setText("Capacity must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid capacity. Please enter a valid number.");
            return false;
        }

        // Validate rate per hour
        if (ratePerHourField.getText().trim().isEmpty()) {
            errorLabel.setText("Rate per hour is required.");
            return false;
        }

        try {
            double rate = Double.parseDouble(ratePerHourField.getText().trim());
            if (rate < 0) {
                errorLabel.setText("Rate must be greater than or equal to 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Invalid rate. Please enter a valid number.");
            return false;
        }

        return true;
    }

    private TextFormatter<String> createNumberTextFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        });
    }

    private TextFormatter<String> createDecimalTextFormatter() {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        });
    }

}
