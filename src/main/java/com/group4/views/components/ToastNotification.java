package components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A toast notification component for displaying temporary messages
 */
public class ToastNotification extends StackPane {

    public enum ToastType {
        SUCCESS, ERROR, INFO, WARNING
    }

    private final HBox container;
    private final Label messageLabel;
    private SequentialTransition animation;

    /**
     * Creates a new toast notification
     */
    public ToastNotification() {
        // Set position
        setAlignment(Pos.TOP_CENTER);
        setTranslateY(20);
        setVisible(false);
        setMouseTransparent(true);

        // Create container
        container = new HBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(10);
        container.setPadding(new javafx.geometry.Insets(12, 20, 12, 20));
        container.setStyle("-fx-background-radius: 4; -fx-background-color: #323232;");

        // Create message label
        messageLabel = new Label();
        messageLabel.setTextFill(Color.WHITE);

        // Add components
        container.getChildren().add(messageLabel);
        getChildren().add(container);
    }

    /**
     * Shows a toast notification
     * 
     * @param message         the message to display
     * @param type            the type of notification
     * @param durationSeconds how long to show the notification
     */
    public void show(String message, ToastType type, double durationSeconds) {
        // Update message
        messageLabel.setText(message);

        // Set type-specific styles
        switch (type) {
            case SUCCESS:
                container.setStyle("-fx-background-radius: 4; -fx-background-color: #43a047;");
                break;
            case ERROR:
                container.setStyle("-fx-background-radius: 4; -fx-background-color: #d32f2f;");
                break;
            case WARNING:
                container.setStyle("-fx-background-radius: 4; -fx-background-color: #ffa000;");
                break;
            default: // INFO
                container.setStyle("-fx-background-radius: 4; -fx-background-color: #323232;");
                break;
        }

        // Cancel existing animation if running
        if (animation != null) {
            animation.stop();
        }

        // Create fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.2), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Create pause animation
        PauseTransition pause = new PauseTransition(Duration.seconds(durationSeconds));

        // Create fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.2), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        // Combine animations
        animation = new SequentialTransition(fadeIn, pause, fadeOut);
        animation.setOnFinished(e -> setVisible(false));

        // Show and start animation
        setVisible(true);
        animation.play();
    }

    /**
     * Shows a success toast notification
     * 
     * @param message the message to display
     */
    public void showSuccess(String message) {
        show(message, ToastType.SUCCESS, 3);
    }

    /**
     * Shows an error toast notification
     * 
     * @param message the message to display
     */
    public void showError(String message) {
        show(message, ToastType.ERROR, 3);
    }

    /**
     * Shows an info toast notification
     * 
     * @param message the message to display
     */
    public void showInfo(String message) {
        show(message, ToastType.INFO, 3);
    }

    /**
     * Shows a warning toast notification
     * 
     * @param message the message to display
     */
    public void showWarning(String message) {
        show(message, ToastType.WARNING, 3);
    }
}