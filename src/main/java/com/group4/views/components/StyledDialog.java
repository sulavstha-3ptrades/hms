package components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A styled dialog component for displaying modals
 */
public class StyledDialog extends Stage {

    private final VBox contentContainer;
    private final HBox buttonContainer;
    private final Label titleLabel;

    /**
     * Creates a new styled dialog
     * 
     * @param owner the owner window
     * @param title the dialog title
     */
    public StyledDialog(Window owner, String title) {
        // Configure stage
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);

        // Create root container
        VBox root = new VBox();
        root.getStyleClass().add("dialog");
        root.setStyle(
                "-fx-background-color: white; -fx-background-radius: 4; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 4);");

        // Create title
        titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(16));

        // Create content container
        contentContainer = new VBox(16);
        contentContainer.setPadding(new Insets(0, 16, 16, 16));
        VBox.setVgrow(contentContainer, Priority.ALWAYS);

        // Create button container
        buttonContainer = new HBox(8);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(16));
        buttonContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 0 0 4 4;");

        // Add components
        root.getChildren().addAll(titleLabel, contentContainer, buttonContainer);

        // Set scene
        Scene scene = new Scene(root);
        scene.setFill(null);
        setScene(scene);

        // Add close on click outside
        scene.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Region) {
                hide();
            }
        });
    }

    /**
     * Sets the dialog content
     * 
     * @param content the content node
     */
    public void setContent(javafx.scene.Node content) {
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(content);
    }

    /**
     * Adds an action button to the dialog
     * 
     * @param button the button to add
     */
    public void addButton(StyledButton button) {
        buttonContainer.getChildren().add(button);
    }

    /**
     * Sets the dialog title
     * 
     * @param title the new title
     */
    public void setDialogTitle(String title) {
        titleLabel.setText(title);
    }

    /**
     * Shows the dialog and waits for it to be closed
     */
    public void showAndWait() {
        // Center on owner
        Window owner = getOwner();
        setX(owner.getX() + (owner.getWidth() - getWidth()) / 2);
        setY(owner.getY() + (owner.getHeight() - getHeight()) / 2);

        super.showAndWait();
    }

    /**
     * Creates a confirmation dialog
     * 
     * @param owner     the owner window
     * @param title     the dialog title
     * @param message   the confirmation message
     * @param onConfirm the action to perform on confirmation
     * @return the created dialog
     */
    public static StyledDialog createConfirmDialog(Window owner, String title, String message, Runnable onConfirm) {
        StyledDialog dialog = new StyledDialog(owner, title);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        dialog.setContent(messageLabel);

        StyledButton confirmButton = new StyledButton("Confirm");
        confirmButton.setOnAction(e -> {
            onConfirm.run();
            dialog.hide();
        });

        StyledButton cancelButton = new StyledButton("Cancel", StyledButton.ButtonType.SECONDARY);
        cancelButton.setOnAction(e -> dialog.hide());

        dialog.addButton(cancelButton);
        dialog.addButton(confirmButton);

        return dialog;
    }
}