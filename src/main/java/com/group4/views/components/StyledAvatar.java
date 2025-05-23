package com.group4.views.components;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A styled avatar component that can display either an image or initials
 */
public class StyledAvatar extends StackPane {

    private static final double DEFAULT_SIZE = 40;
    private final Circle background;
    private final Label initialsLabel;
    private final ImageView imageView;

    /**
     * Creates a new avatar with the specified size
     * 
     * @param size the size of the avatar in pixels
     */
    public StyledAvatar(double size) {
        // Create circle background
        background = new Circle(size / 2);
        background.setFill(Color.valueOf("#2196f3")); // Primary color

        // Create initials label
        initialsLabel = new Label();
        initialsLabel.setTextFill(Color.WHITE);
        initialsLabel.setFont(Font.font("System", FontWeight.BOLD, size / 2.5));

        // Create image view
        imageView = new ImageView();
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);

        // Clip image to circle
        Circle clip = new Circle(size / 2);
        imageView.setClip(clip);

        // Add all components
        getChildren().addAll(background, initialsLabel, imageView);
        imageView.setVisible(false);

        // Set size
        setPrefSize(size, size);
        setMaxSize(size, size);
        setMinSize(size, size);
    }

    /**
     * Creates a new avatar with the default size
     */
    public StyledAvatar() {
        this(DEFAULT_SIZE);
    }

    /**
     * Sets the image to display
     * 
     * @param image the image to display
     */
    public void setImage(Image image) {
        if (image != null) {
            imageView.setImage(image);
            imageView.setVisible(true);
            initialsLabel.setVisible(false);
            background.setVisible(false);
        } else {
            imageView.setVisible(false);
            initialsLabel.setVisible(true);
            background.setVisible(true);
        }
    }

    /**
     * Sets the initials to display when no image is available
     * 
     * @param initials the initials to display
     */
    public void setInitials(String initials) {
        initialsLabel.setText(initials.toUpperCase());
    }

    /**
     * Sets the background color of the avatar when displaying initials
     * 
     * @param color the color to use
     */
    public void setBackgroundColor(Color color) {
        background.setFill(color);
    }
}