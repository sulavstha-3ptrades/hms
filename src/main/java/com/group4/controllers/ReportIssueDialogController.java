package com.group4.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.application.Platform;
import com.group4.services.IssueService;
import com.group4.utils.TaskUtils;
import java.io.IOException;
import java.io.InputStream;

/**
 * Controller for the Report Issue dialog.
 */
public class ReportIssueDialogController {

    @FXML private Label hallIdLabel;
    @FXML private Label hallTypeLabel;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button submitButton;

    private Dialog<ButtonType> dialog;
    private boolean okClicked = false;
    private final String selectedHallId;
    private String hallType;

    /**
     * Creates a new ReportIssueDialogController with hall information.
     * @param hallId The hall ID to display
     * @param hallType The type of the hall to display
     */
    public ReportIssueDialogController(String hallId, String hallType) {
        this.selectedHallId = hallId;
        this.hallType = hallType;
        
        try {
            // Create the dialog first
            dialog = new Dialog<>();
            dialog.setTitle("Report New Issue");
            dialog.initModality(Modality.APPLICATION_MODAL);
            
            // Load the FXML file using the class loader
            FXMLLoader loader = new FXMLLoader();
            loader.setController(this);
            
            // Try to load the FXML file using the class loader
            try (InputStream fxmlStream = getClass().getResourceAsStream("/com/group4/view/ReportIssueDialog.fxml")) {
                if (fxmlStream == null) {
                    throw new IOException("Cannot find FXML file: /com/group4/view/ReportIssueDialog.fxml");
                }
                
                // Load the FXML content
                DialogPane dialogPane = loader.load(fxmlStream);
                dialog.setDialogPane(dialogPane);
                
                // Set up button actions
                dialog.setResultConverter(buttonType -> {
                    if (buttonType != null && buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                        okClicked = true;
                    }
                    return buttonType;
                });
                
                // Initialize the dialog content
                initialize();
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to load FXML content: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing ReportIssueDialog: " + e.getMessage(), e);
        }
    }
    
    /**
     * @return the hall ID displayed in the label
     */
    public String getHallId() {
        return selectedHallId;
    }
    
    /**
     * Shows an alert dialog with the specified type, title, and message.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows the dialog and handles the user response
     */
    public void showAndWait() {
        // Set up the dialog buttons
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().setAll(submitButtonType, ButtonType.CANCEL);
        
        // Get the submit button and disable it by default
        submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        
        // Handle submit button action
        submitButton.setOnAction(event -> {
            String description = getDescription();
            if (description.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Please enter a description");
                return;
            }
            
            // Create the issue
            IssueService issueService = new IssueService();
            TaskUtils.executeTaskWithProgress(
                issueService.createIssue(selectedHallId, description),
                issue -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Issue reported successfully!");
                        dialog.close();
                    });
                },
                error -> {
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to report issue: " + error.getMessage());
                    });
                }
            );
        });
        
        // Show the dialog
        dialog.showAndWait();
    }



    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
    @FXML
    public void initialize() {
        // Set the hall information in the labels
        if (selectedHallId != null && !selectedHallId.isEmpty()) {
            hallIdLabel.setText(selectedHallId);
            if (hallType != null && !hallType.isEmpty()) {
                hallTypeLabel.setText(hallType);
            } else {
                hallTypeLabel.setText("N/A");
            }
            
            // Enable/disable submit button based on description
            descriptionTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
                if (submitButton != null) {
                    submitButton.setDisable(newValue.trim().isEmpty());
                }
            });
            
            if (submitButton != null) {
                submitButton.setDisable(true);
            }
        } else {
            // If no hall ID is provided, show messages and disable submit
            hallIdLabel.setText("No hall selected");
            hallTypeLabel.setText("N/A");
            if (submitButton != null) {
                submitButton.setDisable(true);
            }
        }
    }
    
    /**
     * @return the description entered by the user
     */
    public String getDescription() {
        return descriptionTextArea != null ? descriptionTextArea.getText() : null;
    }
    
    /**
     * @return true if the user clicked OK, false otherwise
     */
    public boolean isOkClicked() {
        return okClicked;
    }
}
