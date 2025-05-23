package components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * Form group component that provides consistent layout and spacing for form
 * elements
 */
public class FormGroup extends VBox {

    private final Label label;

    /**
     * Creates a new form group with the specified label and input
     * 
     * @param labelText label text
     * @param input     input component
     */
    public FormGroup(String labelText, Node input) {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Apply form group styles
        getStyleClass().add("form-group");

        // Create and style label
        label = new Label(labelText);
        label.getStyleClass().add("label");

        // Set layout properties
        setAlignment(Pos.TOP_LEFT);

        // Add components
        getChildren().addAll(label, input);

        // Set accessibility
        setAccessibleRoleDescription("form group");
        setAccessibleText(labelText + " form group");

        // Bind input width to form group width if it's a Region
        if (input instanceof Region) {
            ((Region) input).prefWidthProperty().bind(widthProperty());
        }
    }

    /**
     * Updates the label text
     * 
     * @param text new label text
     */
    public void setLabelText(String text) {
        label.setText(text);
        setAccessibleText(text + " form group");
    }

    /**
     * Sets whether the input is required
     * 
     * @param required whether the input is required
     */
    public void setRequired(boolean required) {
        if (required) {
            label.setText(label.getText() + " *");
        } else {
            label.setText(label.getText().replace(" *", ""));
        }
    }
}