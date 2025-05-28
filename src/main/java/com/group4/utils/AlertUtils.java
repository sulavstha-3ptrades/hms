package com.group4.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for showing alerts and dialogs.
 */
public class AlertUtils {
    
    /**
     * Shows an information alert.
     *
     * @param title   the title of the alert
     * @param content the content text
     */
    public static void showInfo(String title, String content) {
        showAlert(AlertType.INFORMATION, title, null, content);
    }

    /**
     * Shows an error alert.
     *
     * @param title   the title of the alert
     * @param header  the header text (can be null)
     * @param content the content text
     */
    public static void showError(String title, String header, String content) {
        showAlert(AlertType.ERROR, title, header, content);
    }
    
    /**
     * Shows an error alert with just title and content.
     *
     * @param title   the title of the alert
     * @param content the content text
     */
    public static void showError(String title, String content) {
        showAlert(AlertType.ERROR, title, null, content);
    }

    /**
     * Shows a warning alert.
     *
     * @param title   the title of the alert
     * @param content the content text
     */
    public static void showWarning(String title, String content) {
        showAlert(AlertType.WARNING, title, null, content);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param title   the title of the dialog
     * @param header  the header text (can be null)
     * @param content the content text
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Make the dialog modal
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows a generic alert with the specified type.
     *
     * @param type    the type of alert
     * @param title   the title of the alert
     * @param header  the header text (can be null)
     * @param content the content text
     */
    private static void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Make the dialog modal
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        
        alert.showAndWait();
    }
}
