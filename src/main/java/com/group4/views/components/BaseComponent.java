package components;

import javafx.scene.layout.Region;

/**
 * Base component class that enforces styling standards across the application.
 * All custom components should extend this class.
 */
public abstract class BaseComponent extends Region {

    protected BaseComponent() {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Set default properties for responsive design
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
    }

    /**
     * Applies theme-specific styles to the component
     * 
     * @param isDarkMode whether dark mode is enabled
     */
    public void applyTheme(boolean isDarkMode) {
        if (isDarkMode) {
            getStyleClass().add("dark-theme");
        } else {
            getStyleClass().remove("dark-theme");
        }
    }

    /**
     * Sets accessibility properties for the component
     * 
     * @param ariaLabel accessibility label
     * @param role      ARIA role
     */
    protected void setAccessibility(String ariaLabel, String role) {
        setAccessibleText(ariaLabel);
        setAccessibleRoleDescription(role);
    }
}