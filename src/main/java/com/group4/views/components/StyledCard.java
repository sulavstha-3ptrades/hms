package com.group4.views.components;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A styled card component for displaying grouped information
 */
public class StyledCard extends VBox {

    private final Label titleLabel;
    private final VBox contentBox;
    private final VBox actionBox;

    /**
     * Creates a new card with the specified title
     * 
     * @param title the card title
     */
    public StyledCard(String title) {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Apply card styles
        getStyleClass().add("card");

        // Create title
        titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setPadding(new Insets(0, 0, 8, 0));

        // Create content container
        contentBox = new VBox(8); // 8px spacing

        // Create action container
        actionBox = new VBox(8);
        actionBox.setPadding(new Insets(16, 0, 0, 0));
        actionBox.setVisible(false);
        actionBox.setManaged(false);

        // Add components
        getChildren().addAll(titleLabel, contentBox, actionBox);
    }

    /**
     * Sets the card title
     * 
     * @param title the new title
     */
    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Adds content to the card
     * 
     * @param content the content to add
     */
    public void addContent(Node content) {
        contentBox.getChildren().add(content);
    }

    /**
     * Sets the content of the card, replacing any existing content
     * 
     * @param content the new content
     */
    public void setContent(Node content) {
        contentBox.getChildren().clear();
        contentBox.getChildren().add(content);
    }

    /**
     * Adds an action button to the card
     * 
     * @param action the action button to add
     */
    public void addAction(StyledButton action) {
        actionBox.getChildren().add(action);
        actionBox.setVisible(true);
        actionBox.setManaged(true);
    }

    /**
     * Clears all action buttons from the card
     */
    public void clearActions() {
        actionBox.getChildren().clear();
        actionBox.setVisible(false);
        actionBox.setManaged(false);
    }

    /**
     * Sets whether the card is elevated (has a shadow)
     * 
     * @param elevated whether the card should be elevated
     */
    public void setElevated(boolean elevated) {
        if (elevated) {
            setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 4);");
        } else {
            setStyle("");
        }
    }
}