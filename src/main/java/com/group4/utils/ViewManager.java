package com.group4.utils;

import java.io.IOException;

import com.group4.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewManager {
    private static Stage primaryStage;
    private static Scene mainScene;
    private static Parent root;

    public static void initialize(Stage stage, Parent initialRoot) {
        primaryStage = stage;
        root = initialRoot;
        
        // Create scene with default dimensions if not maximized
        mainScene = new Scene(root, 800, 600);
        
        // Apply scene to stage
        primaryStage.setScene(mainScene);
        
        // Set minimum dimensions to ensure proper layout
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        
        // Center on screen by default
        primaryStage.centerOnScreen();
    }

    public static void switchView(Parent newRoot) {
        if (mainScene != null) {
            boolean wasMaximized = primaryStage.isMaximized();
            mainScene.setRoot(newRoot);
            root = newRoot;
            if (wasMaximized) {
                primaryStage.setMaximized(true);
            }
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Scene getMainScene() {
        return mainScene;
    }

    public static Parent getRoot() {
        return root;
    }
    
    /**
     * Loads and switches to a view specified by the FXML path.
     * 
     * @param fxmlPath The path to the FXML file relative to the resources folder
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void switchToView(String fxmlPath) throws IOException {
        try {
            // Remove leading slash if present to ensure consistent path handling
            String path = fxmlPath.startsWith("/") ? fxmlPath.substring(1) : fxmlPath;
            
            // Load the FXML file using the class loader
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(App.class.getClassLoader().getResource(path));
            
            if (loader.getLocation() == null) {
                throw new IOException("Cannot find resource: " + path);
            }
            
            Parent newRoot = loader.load();
            switchView(newRoot);
        } catch (IOException e) {
            System.err.println("Error loading FXML: " + fxmlPath);
            e.printStackTrace();
            throw e;
        }
    }
}
