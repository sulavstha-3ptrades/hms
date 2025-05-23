package components;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;

/**
 * A component for handling image selection and preview
 */
public class ImageUploadField extends VBox {

    private final ImageView previewImage;
    private final Label placeholderLabel;
    private final StyledButton uploadButton;
    private File selectedFile;

    /**
     * Creates a new image upload field
     */
    public ImageUploadField() {
        // Load global styles
        String css = getClass().getResource("/styles/global.css").toExternalForm();
        getStylesheets().add(css);

        // Set spacing and alignment
        setSpacing(8);
        setAlignment(Pos.CENTER);

        // Create preview container
        VBox previewContainer = new VBox();
        previewContainer.setAlignment(Pos.CENTER);
        previewContainer.setPrefSize(200, 200);
        previewContainer.getStyleClass().add("image-upload-preview");
        previewContainer.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 4; -fx-border-style: dashed;");

        // Create preview image view
        previewImage = new ImageView();
        previewImage.setFitWidth(180);
        previewImage.setFitHeight(180);
        previewImage.setPreserveRatio(true);
        previewImage.setVisible(false);

        // Create placeholder label
        placeholderLabel = new Label("No image selected");
        placeholderLabel.getStyleClass().add("image-upload-placeholder");

        // Add preview components
        previewContainer.getChildren().addAll(previewImage, placeholderLabel);

        // Create upload button
        uploadButton = new StyledButton("Choose Image");
        uploadButton.setOnAction(e -> handleImageSelection());

        // Add all components
        getChildren().addAll(previewContainer, uploadButton);
    }

    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            selectedFile = file;
            updatePreview(file);
        }
    }

    private void updatePreview(File file) {
        try {
            Image image = new Image(file.toURI().toString());
            previewImage.setImage(image);
            previewImage.setVisible(true);
            placeholderLabel.setVisible(false);
        } catch (Exception e) {
            previewImage.setVisible(false);
            placeholderLabel.setVisible(true);
            placeholderLabel.setText("Error loading image");
        }
    }

    /**
     * Gets the selected image file
     * 
     * @return the selected file, or null if no file is selected
     */
    public File getSelectedFile() {
        return selectedFile;
    }

    /**
     * Clears the selected image
     */
    public void clear() {
        selectedFile = null;
        previewImage.setImage(null);
        previewImage.setVisible(false);
        placeholderLabel.setVisible(true);
        placeholderLabel.setText("No image selected");
    }

    /**
     * Sets whether the upload field is enabled
     * 
     * @param enabled whether the field is enabled
     */
    public void setFieldEnabled(boolean enabled) {
        setDisable(!enabled);
        uploadButton.setDisable(!enabled);
    }
}