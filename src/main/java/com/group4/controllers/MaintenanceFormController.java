package com.group4.controllers;

import com.group4.models.Hall;
import com.group4.models.Maintenance;
import com.group4.services.MaintenanceService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MaintenanceFormController {
    @FXML private Label hallLabel;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextArea descriptionField;
    @FXML private Label errorLabel;
    @FXML private Label startDateError;
    @FXML private Label startTimeError;
    @FXML private Label endDateError;
    @FXML private Label endTimeError;
    @FXML private Label descriptionError;
    
    // Time selection components
    @FXML private Spinner<Integer> startHourSpinner;
    @FXML private Spinner<Integer> startMinuteSpinner;
    @FXML private Spinner<Integer> endHourSpinner;
    @FXML private Spinner<Integer> endMinuteSpinner;
    @FXML private ToggleButton startAmButton;
    @FXML private ToggleButton startPmButton;
    @FXML private ToggleButton endAmButton;
    @FXML private ToggleButton endPmButton;
    
    private final Map<String, Control> errorFields = new HashMap<>();

    private Hall selectedHall;
    private boolean saveClicked = false;
    @FXML
    private DialogPane dialogPane;
    
    @SuppressWarnings("unused") // Will be used for dialog interaction
    private Dialog<ButtonType> parentDialog;
    
    @SuppressWarnings("unused") // Will be used when implementing save functionality
    private final MaintenanceService maintenanceService = new MaintenanceService();
    private ToggleGroup startAmPmGroup;
    private ToggleGroup endAmPmGroup;
    
    /**
     * Sets the parent dialog for this controller.
     * @param dialog The parent dialog
     */
    public void setParentDialog(Dialog<ButtonType> dialog) {
        this.parentDialog = dialog;
        if (dialog != null) {
            // Set up the dialog's result converter
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    // Force validation on all fields
                    boolean isValid = validateForm();
                    if (isValid) {
                        saveClicked = true;
                        return buttonType;
                    }
                    // Don't return null here as it prevents the dialog from closing
                    // Instead, we'll handle the dialog closing in the handleSave method
                    return null;
                }
                return buttonType;
            });
            
            // Handle dialog close request
            dialog.setOnCloseRequest(event -> {
                if (saveClicked) {
                    // Allow close if save was successful
                    return;
                }
                // Prevent close if there are validation errors
                if (!validateForm()) {
                    event.consume();
                }
            });
        }
    }
    
    /**
     * Returns true if the save button was clicked.
     * @return true if save was clicked, false otherwise
     */
    public boolean isSaveClicked() {
        return saveClicked;
    }
    
    @FXML
    private void initialize() {
        try {
            // Initialize toggle groups
            startAmPmGroup = new ToggleGroup();
            endAmPmGroup = new ToggleGroup();
            
            // Set up time spinners and validation
            initializeTimeSpinners();
            setupTimeValidation();
            
            // Set default values
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            
            // Set initial values with 1 hour buffer from current time
            startDatePicker.setValue(today);
            endDatePicker.setValue(today.plusDays(1));
            
            // Set up AM/PM toggle groups
            startAmButton.setToggleGroup(startAmPmGroup);
            startPmButton.setToggleGroup(startAmPmGroup);
            startAmButton.setSelected(now.getHour() < 12);
            startPmButton.setSelected(now.getHour() >= 12);
            
            endAmButton.setToggleGroup(endAmPmGroup);
            endPmButton.setToggleGroup(endAmPmGroup);
            endAmButton.setSelected(now.getHour() < 12);
            endPmButton.setSelected(now.getHour() >= 12);
            
            // Set default description to empty string to avoid null pointer
            descriptionField.setText("");
            
            // Add change listeners for real-time validation
            setupChangeListeners();
            
            // Mark required fields
            markRequiredFields();
            
            // Don't validate on init to prevent showing errors when form first opens
            endPmButton.setToggleGroup(endAmPmGroup);
            endAmButton.setSelected(true);
            
            // Set up validation on focus lost
            setupFocusLostValidation();
            
            // Prevent selecting past dates
            startDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(today));
                    if (date.isBefore(today)) {
                        setTooltip(new Tooltip("Cannot select past dates"));
                    }
                }
            });
            
            endDatePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    setDisable(empty || date.isBefore(today));
                    if (date.isBefore(today)) {
                        setTooltip(new Tooltip("Cannot select past dates"));
                    }
                }
            });
            
            // Set default times (9:00 AM - 5:00 PM)
            startHourSpinner.getValueFactory().setValue(9);
            startMinuteSpinner.getValueFactory().setValue(0);
            endHourSpinner.getValueFactory().setValue(5);
            endMinuteSpinner.getValueFactory().setValue(0);
            
            // Set up date change listeners
            startDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null) {
                    if (endDatePicker.getValue() != null && endDatePicker.getValue().isBefore(newDate)) {
                        endDatePicker.setValue(newDate);
                    }
                    startDatePicker.setValue(newDate);
                }
            });
            
            endDatePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
                if (newDate != null && startDatePicker.getValue() != null && newDate.isBefore(startDatePicker.getValue())) {
                    endDatePicker.setValue(startDatePicker.getValue());
                }
            });
            
            // Set description default and validation
            if (selectedHall != null) {
                descriptionField.setText("Scheduled maintenance - " + selectedHall.getType() + " Hall");
            }
            
            // Add description length limit
            TextFormatter<String> textFormatter = new TextFormatter<>(change -> 
                change.getControlNewText().length() <= 500 ? change : null);
            descriptionField.setTextFormatter(textFormatter);
            
            // Add validation on focus lost
            setupFocusLostValidation();
            
            // Add listeners for time changes
            setupTimeValidation();
        } catch (Exception e) {
            showError("Error initializing form: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private void initializeTimeSpinners() {
        // Initialize toggle groups
        startAmPmGroup = new ToggleGroup();
        endAmPmGroup = new ToggleGroup();
        
        // Set up start time toggle group
        startAmButton.setToggleGroup(startAmPmGroup);
        startPmButton.setToggleGroup(startAmPmGroup);
        startAmButton.setUserData("AM");
        startPmButton.setUserData("PM");
        
        // Set up end time toggle group
        endAmButton.setToggleGroup(endAmPmGroup);
        endPmButton.setToggleGroup(endAmPmGroup);
        endAmButton.setUserData("AM");
        endPmButton.setUserData("PM");
        
        // Set default selections
        startAmButton.setSelected(true);
        endPmButton.setSelected(true);
        
        // Hour spinners (1-12)
        SpinnerValueFactory<Integer> hourFactory = new IntegerSpinnerValueFactory(1, 12, 12) {
            @Override
            public void decrement(int steps) {
                int value = getValue();
                if (value - steps >= 1) {
                    setValue(value - steps);
                } else {
                    setValue(12);
                }
            }
            
            @Override
            public void increment(int steps) {
                int value = getValue();
                if (value + steps <= 12) {
                    setValue(value + steps);
                } else {
                    setValue(1);
                }
            }
        };
        
        startHourSpinner.setValueFactory(hourFactory);
        endHourSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 12, 12));
        
        // Initialize minute spinners
        startMinuteSpinner.setValueFactory(createMinuteFactory());
        endMinuteSpinner.setValueFactory(createMinuteFactory());
        
        // Add styling to spinners
        styleSpinner(startHourSpinner);
        styleSpinner(startMinuteSpinner);
        styleSpinner(endHourSpinner);
        styleSpinner(endMinuteSpinner);
        
        // Add validation for hour spinners
        startHourSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                int value = startHourSpinner.getValue();
                if (value < 1) startHourSpinner.getValueFactory().setValue(1);
                if (value > 12) startHourSpinner.getValueFactory().setValue(12);
                validateDateTimeRange();
            }
        });
        
        endHourSpinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                int value = endHourSpinner.getValue();
                if (value < 1) endHourSpinner.getValueFactory().setValue(1);
                if (value > 12) endHourSpinner.getValueFactory().setValue(12);
                validateDateTimeRange();
            }
        });
    }
    
    private void styleSpinner(Spinner<Integer> spinner) {
        spinner.getEditor().setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-alignment: center;");
        spinner.setEditable(true);
    }
    
    private void setupTimeValidation() {
        // Add listeners to all time-related controls
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        
        startHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        startMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        startAmPmGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        
        endHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endAmPmGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
    }
    
    private SpinnerValueFactory<Integer> createMinuteFactory() {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5);
        
        factory.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer value) {
                if (value < 0) return "00";
                if (value > 59) return "59";
                return String.format("%02d", value);
            }
            
            @Override
            public Integer fromString(String string) {
                try {
                    int value = Integer.parseInt(string);
                    if (value < 0) return 0;
                    if (value > 59) return 59;
                    return (value / 5) * 5; // Round to nearest 5 minutes
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        });
        
        // Add value change listener to ensure values stay within bounds
        factory.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue < 0) {
                factory.setValue(0);
            } else if (newValue > 59) {
                factory.setValue(59);
            } else {
                // Round to nearest 5 minutes
                int rounded = (newValue / 5) * 5;
                if (rounded != newValue) {
                    factory.setValue(rounded);
                }
            }
        });
        
        return factory;
    }
    
    private void setupFocusLostValidation() {
        Consumer<Control> addFocusListener = control -> {
            control.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) { // Focus lost
                    validateField(control);
                }
            });
        };
        
        addFocusListener.accept(startDatePicker);
        addFocusListener.accept(startHourSpinner);
        addFocusListener.accept(startMinuteSpinner);
        addFocusListener.accept(endDatePicker);
        addFocusListener.accept(endHourSpinner);
        addFocusListener.accept(endMinuteSpinner);
        addFocusListener.accept(descriptionField);
    }
    
    private void validateField(Control control) {
        if (control == startDatePicker || control == startHourSpinner || 
            control == startMinuteSpinner || control == endDatePicker || 
            control == endHourSpinner || control == endMinuteSpinner) {
            validateDateTimeRange();
        } else if (control == descriptionField) {
            validateDescription();
        }
    }
    
    private void setupChangeListeners() {
        // Add listeners to all form fields for real-time validation
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        startHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        startMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endHourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endMinuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        startAmPmGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        endAmPmGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> validateDateTimeRange());
        
        // Validate description on key release
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateDescription();
            updateCharacterCount();
        });
    }
    
    private void markRequiredFields() {
        // Add asterisk to required field labels
        hallLabel.setText(hallLabel.getText() + " *");
        
        // Style required fields
        String requiredStyle = "-fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0; -fx-padding: 0 0 2 0;";
        startDatePicker.setStyle(requiredStyle);
        endDatePicker.setStyle(requiredStyle);
        descriptionField.setStyle(requiredStyle);
    }
    
    private void updateCharacterCount() {
        int length = descriptionField.getText() != null ? descriptionField.getText().length() : 0;
        // This assumes you have a label for character count in your FXML
        // If not, you can add it or remove this part
        if (descriptionField.lookup(".character-count") != null) {
            ((Label) descriptionField.lookup(".character-count")).setText(length + "/500");
        }
    }
    
    private void validateDateTimeRange() {
        LocalDateTime start = getStartDateTime();
        LocalDateTime end = getEndDateTime();
        LocalDateTime now = LocalDateTime.now();
        
        boolean hasError = false;
        
        // Clear previous errors
        markFieldValid(startDatePicker);
        markFieldValid(endDatePicker);
        
        // Validate start date/time
        if (start == null) {
            markFieldInvalid(startDatePicker, "Please select a valid start time");
            hasError = true;
        } else if (start.isBefore(now)) {
            markFieldInvalid(startDatePicker, "Start date/time cannot be in the past");
            showError("Start date/time cannot be in the past");
            hasError = true;
        }
        
        // Validate end date/time
        if (end == null) {
            markFieldInvalid(endDatePicker, "Please select a valid end time");
            hasError = true;
        } else if (start != null) {
            if (end.isBefore(start)) {
                markFieldInvalid(endDatePicker, "End time must be after start time");
                showError("End time must be after start time");
                hasError = true;
            } else if (end.isEqual(start)) {
                markFieldInvalid(endDatePicker, "End time must be after start time");
                showError("End time must be after start time");
                hasError = true;
            }
        }
        
        if (!hasError) {
            clearError();
        }
    }
    
    private void validateDescription() {
        String text = descriptionField.getText();
        if (text == null || text.trim().isEmpty()) {
            markFieldInvalid(descriptionField, "Description is required");
        } else if (text.length() > 500) {
            markFieldInvalid(descriptionField, "Description must be 500 characters or less (currently " + text.length() + ")");
        } else {
            markFieldValid(descriptionField);
        }
    }
    
    private void markFieldInvalid(Control field, String errorMessage) {
        // Apply error styling class
        if (field instanceof Spinner) {
            Spinner<?> spinner = (Spinner<?>) field;
            if (spinner.getEditor() != null) {
                spinner.getEditor().getStyleClass().remove("error-field");
                spinner.getEditor().getStyleClass().add("error-field");
            }
        } else {
            field.getStyleClass().remove("error-field");
            field.getStyleClass().add("error-field");
        }
        
        // Update the appropriate error label with the specific error message
        if (field == startDatePicker) {
            setErrorLabel(startDateError, errorMessage);
        } else if (field == startHourSpinner || field == startMinuteSpinner) {
            setErrorLabel(startTimeError, errorMessage);
        } else if (field == endDatePicker) {
            setErrorLabel(endDateError, errorMessage);
        } else if (field == endHourSpinner || field == endMinuteSpinner) {
            setErrorLabel(endTimeError, errorMessage);
        } else if (field == descriptionField) {
            setErrorLabel(descriptionError, errorMessage);
        }
        
        // Add to error fields map if not already present
        errorFields.put(field.getId() != null ? field.getId() : field.toString(), field);
        updateErrorLabel();
    }
    
    private void setErrorLabel(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            errorLabel.getStyleClass().remove("error-message");
            errorLabel.getStyleClass().add("error-message");
        }
    }
    
    private void markFieldValid(Control field) {
        // Remove error styling class
        if (field instanceof Spinner) {
            Spinner<?> spinner = (Spinner<?>) field;
            if (spinner.getEditor() != null) {
                spinner.getEditor().getStyleClass().remove("error-field");
            }
        } else {
            field.getStyleClass().remove("error-field");
        }
        
        // Clear the appropriate error label
        if (field == startDatePicker) {
            clearErrorLabel(startDateError);
        } else if (field == startHourSpinner || field == startMinuteSpinner) {
            clearErrorLabel(startTimeError);
        } else if (field == endDatePicker) {
            clearErrorLabel(endDateError);
        } else if (field == endHourSpinner || field == endMinuteSpinner) {
            clearErrorLabel(endTimeError);
        } else if (field == descriptionField) {
            clearErrorLabel(descriptionError);
        }
        
        // Remove from error fields
        errorFields.remove(field.getId() != null ? field.getId() : field.toString());
        updateErrorLabel();
    }
    
    private void clearErrorLabel(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
    
    private void updateErrorLabel() {
        if (errorFields.isEmpty()) {
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        } else if (errorLabel != null) {
            errorLabel.setText("Please fix the highlighted fields");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            errorLabel.getStyleClass().remove("error-message");
            errorLabel.getStyleClass().add("error-message");
        }
    }
    
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            errorLabel.getStyleClass().remove("error-message");
            errorLabel.getStyleClass().add("error-message");
        }
    }
    
    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    public void setHall(Hall hall) {
        this.selectedHall = hall;
        if (hall != null) {
            hallLabel.setText(hall.getType() + " Hall");
        }
    }
    
    /**
     * Initializes the form with an existing maintenance record for editing
     * @param maintenance The maintenance record to edit
     */
    public void initializeWithMaintenance(Maintenance maintenance) {
        if (maintenance == null) return;
        
        // Set the hall ID (we'll need to load the hall details separately)
        String hallId = maintenance.getHallId();
        if (hallId != null && !hallId.isEmpty()) {
            // TODO: Load hall details from service if needed
            hallLabel.setText("Hall ID: " + hallId);
        }
        
        // Set the dates
        startDatePicker.setValue(maintenance.getStartTime().toLocalDate());
        endDatePicker.setValue(maintenance.getEndTime().toLocalDate());
        
        // Set the description
        descriptionField.setText(maintenance.getDescription());
        
        // Set the times
        LocalTime startTime = maintenance.getStartTime().toLocalTime();
        LocalTime endTime = maintenance.getEndTime().toLocalTime();
        
        // Set start time
        int hour = startTime.getHour();
        boolean isPm = hour >= 12;
        if (hour > 12) hour -= 12;
        if (hour == 0) hour = 12;
        
        if (startHourSpinner != null) {
            startHourSpinner.getValueFactory().setValue(hour);
        }
        if (startMinuteSpinner != null) {
            startMinuteSpinner.getValueFactory().setValue(startTime.getMinute());
        }
        if (startAmPmGroup != null) {
            startAmPmGroup.selectToggle(isPm ? startPmButton : startAmButton);
        }
        
        // Set end time
        hour = endTime.getHour();
        isPm = hour >= 12;
        if (hour > 12) hour -= 12;
        if (hour == 0) hour = 12;
        
        if (endHourSpinner != null) {
            endHourSpinner.getValueFactory().setValue(hour);
        }
        if (endMinuteSpinner != null) {
            endMinuteSpinner.getValueFactory().setValue(endTime.getMinute());
        }
        if (endAmPmGroup != null) {
            endAmPmGroup.selectToggle(isPm ? endPmButton : endAmButton);
        }
    }

    public LocalDateTime getStartDateTime() {
        try {
            LocalDate date = startDatePicker.getValue();
            LocalTime time = getTimeFromSpinners(startHourSpinner, startMinuteSpinner, startAmPmGroup);
            return date != null ? LocalDateTime.of(date, time) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public LocalDateTime getEndDateTime() {
        try {
            LocalDate date = endDatePicker.getValue();
            LocalTime time = getTimeFromSpinners(endHourSpinner, endMinuteSpinner, endAmPmGroup);
            return date != null ? LocalDateTime.of(date, time) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private LocalTime getTimeFromSpinners(Spinner<Integer> hourSpinner, Spinner<Integer> minuteSpinner, ToggleGroup amPmGroup) {
        int hour = hourSpinner.getValue();
        int minute = minuteSpinner.getValue();
        boolean isPm = amPmGroup.getSelectedToggle() != null && 
                      "PM".equals(amPmGroup.getSelectedToggle().getUserData());
        
        // Convert 12h to 24h format
        if (isPm && hour < 12) {
            hour += 12;
        } else if (!isPm && hour == 12) {
            hour = 0;
        }
        
        return LocalTime.of(hour, minute);
    }

    public String getDescription() {
        return descriptionField.getText().trim();
    }


    
    /**
     * Handles the save action when the save button is clicked.
     * This method is called from the dialog's result converter.
     * @return true if the save was successful, false otherwise
     */
    
    public boolean handleSave() {
        try {
            // Force validation of all fields
            boolean isValid = validateForm();
            
            if (selectedHall == null) {
                showError("No hall selected for maintenance scheduling");
                return false;
            }
            
            if (!isValid) {
                // Show error message and keep dialog open
                showError("Please fix the validation errors before saving");
                return false;
            }
            
            // Log the maintenance scheduling attempt with hall details
            System.out.println("=== Maintenance Scheduling Details ===");
            System.out.println("Hall ID: " + selectedHall.getHallId());
            System.out.println("Hall Type: " + selectedHall.getType());
            System.out.println("Hall Capacity: " + selectedHall.getCapacity());
            System.out.println("Hall Rate Per Hour: " + selectedHall.getRatePerHour());
            System.out.println("Maintenance Period: " + getStartDateTime() + " to " + getEndDateTime());
            System.out.println("====================================");
            
            saveClicked = true;
            return true;
            
        } catch (Exception e) {
            showError("Error saving maintenance: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates the form and shows error messages if validation fails.
     * @return true if the form is valid, false otherwise
     */
    private boolean validateForm() {
        // Clear previous errors
        clearError();
        
        // Validate all fields
        validateDescription();
        validateDateTimeRange();
        
        // If there are errors, focus the first invalid field
        if (!errorFields.isEmpty()) {
            errorFields.keySet().stream().findFirst().ifPresent(field -> {
                Control control = errorFields.get(field);
                if (control != null) {
                    Platform.runLater(control::requestFocus);
                }
            });
            return false;
        }
        
        return true;
    }

}
