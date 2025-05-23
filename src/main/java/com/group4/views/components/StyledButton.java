package components;

import javafx.scene.control.Button;

/**
 * Custom styled button component that follows the application's design system.
 */
public class StyledButton extends Button {

    public enum ButtonType {
        PRIMARY,
        SECONDARY,
        DANGER
    }

    /**
     * Creates a new styled button with the specified text and type
     * 
     * @param text button text
     * @param type button type (PRIMARY, SECONDARY, or DANGER)
     */
    public StyledButton(String text, ButtonType type) {
        super(text);

        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Apply button-specific styles
        getStyleClass().add("button");
        switch (type) {
            case SECONDARY:
                getStyleClass().add("button-secondary");
                break;
            case DANGER:
                getStyleClass().add("button-danger");
                break;
            default:
                // Primary is the default style
                break;
        }

        // Set accessibility
        setAccessibleRole(javafx.scene.AccessibleRole.BUTTON);
        setAccessibleText(text);

        // Add hover effect
        setOnMouseEntered(e -> setOpacity(0.8));
        setOnMouseExited(e -> setOpacity(1.0));
    }

    /**
     * Creates a new primary styled button
     * 
     * @param text button text
     */
    public StyledButton(String text) {
        this(text, ButtonType.PRIMARY);
    }
}