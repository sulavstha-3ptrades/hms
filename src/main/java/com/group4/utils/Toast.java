package com.group4.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Utility class for showing toast notifications
 */
public class Toast {
    private static final int DEFAULT_DURATION = 3000;

    /**
     * Shows a toast message
     * 
     * @param title    The title of the toast
     * @param message  The message to display
     * @param duration Duration in milliseconds to show the toast
     */
    public static void show(String title, String message, int duration) {
        // Create a new stage for the toast
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // Create the toast content
        Label titleLabel = new Label(title);
        titleLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle(
                "-fx-text-fill: white;");

        StackPane root = new StackPane();
        root.setStyle(
                "-fx-background-color: rgba(0, 0, 0, 0.8);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15px;");
        root.setOpacity(0);

        VBox content = new VBox(5, titleLabel, messageLabel);
        content.setStyle("-fx-alignment: center;");
        root.getChildren().add(content);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        toastStage.setScene(scene);
        toastStage.show();

        // Position the toast at the bottom center of the screen
        toastStage.setX(0);
        toastStage.setY(0);
        toastStage.setAlwaysOnTop(true);

        // Center the toast on the screen
        toastStage.centerOnScreen();

        // Animate the toast
        Timeline fadeInTimeline = new Timeline();
        KeyFrame fadeInKey = new KeyFrame(Duration.millis(300),
                new KeyValue(root.opacityProperty(), 1));
        fadeInTimeline.getKeyFrames().add(fadeInKey);
        fadeInTimeline.setOnFinished(ae -> {
            new Thread(() -> {
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Timeline fadeOutTimeline = new Timeline();
                KeyFrame fadeOutKey = new KeyFrame(Duration.millis(300),
                        new KeyValue(root.opacityProperty(), 0));
                fadeOutTimeline.getKeyFrames().add(fadeOutKey);
                fadeOutTimeline.setOnFinished(aeb -> toastStage.close());
                fadeOutTimeline.play();
            }).start();
        });
        fadeInTimeline.play();
    }

    /**
     * Shows a toast message with default duration
     * 
     * @param title   The title of the toast
     * @param message The message to display
     */
    public static void show(String title, String message) {
        show(title, message, DEFAULT_DURATION);
    }
}
