package views;

import components.FormGroup;
import components.StyledButton;
import components.StyledButton.ButtonType;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;

/**
 * Sample form view for booking a hall using our styled components
 */
public class HallBookingForm extends VBox {

    private final TextField nameField;
    private final TextField emailField;
    private final ComboBox<String> hallSelector;
    private final DatePicker datePicker;
    private final TextArea notesArea;

    public HallBookingForm() {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Apply container styles
        getStyleClass().add("container");
        getStyleClass().add("card");

        // Initialize form fields
        nameField = new TextField();
        emailField = new TextField();
        hallSelector = new ComboBox<>();
        hallSelector.getItems().addAll("Main Hall", "Conference Room", "Auditorium");
        hallSelector.setPromptText("Select a hall");
        datePicker = new DatePicker();
        notesArea = new TextArea();
        notesArea.setPromptText("Additional notes or requirements");

        // Create form groups
        FormGroup nameGroup = new FormGroup("Full Name", nameField);
        nameGroup.setRequired(true);

        FormGroup emailGroup = new FormGroup("Email", emailField);
        emailGroup.setRequired(true);

        FormGroup hallGroup = new FormGroup("Hall Type", hallSelector);
        hallGroup.setRequired(true);

        FormGroup dateGroup = new FormGroup("Booking Date", datePicker);
        dateGroup.setRequired(true);

        FormGroup notesGroup = new FormGroup("Notes", notesArea);

        // Create buttons
        HBox buttonContainer = new HBox(10); // 10px spacing
        buttonContainer.setPadding(new Insets(16, 0, 0, 0));

        StyledButton submitButton = new StyledButton("Book Hall");
        StyledButton clearButton = new StyledButton("Clear Form", ButtonType.SECONDARY);

        submitButton.setOnAction(e -> handleSubmit());
        clearButton.setOnAction(e -> handleClear());

        buttonContainer.getChildren().addAll(submitButton, clearButton);

        // Add all components to the form
        getChildren().addAll(
                nameGroup,
                emailGroup,
                hallGroup,
                dateGroup,
                notesGroup,
                buttonContainer);
    }

    private void handleSubmit() {
        // Validate required fields
        if (nameField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                hallSelector.getValue() == null ||
                datePicker.getValue() == null) {
            // Show error message
            System.out.println("Please fill in all required fields");
            return;
        }

        // Process the form submission
        System.out.println("Form submitted successfully!");
        System.out.println("Name: " + nameField.getText());
        System.out.println("Email: " + emailField.getText());
        System.out.println("Hall: " + hallSelector.getValue());
        System.out.println("Date: " + datePicker.getValue());
        System.out.println("Notes: " + notesArea.getText());
    }

    private void handleClear() {
        nameField.clear();
        emailField.clear();
        hallSelector.setValue(null);
        datePicker.setValue(null);
        notesArea.clear();
    }
}