package com.group4.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Modality;

import java.util.Optional;

/**
 * Utility class for managing dialogs and alerts.
 */
public class DialogManager {
    
    private DialogManager() {
        // Private constructor to prevent instantiation
    }

    /**
     * Shows an information dialog.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }

    /**
     * Shows a warning dialog.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }

    /**
     * Shows an error dialog.
     *
     * @param title   the dialog title
     * @param message the message to display
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @return true if user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void showAlert(AlertType type, String title, String message) {
        Alert alert = createAlert(type, title, message);
        alert.showAndWait();
    }

    private static Alert createAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initModality(Modality.APPLICATION_MODAL);
        return alert;
    }
}
