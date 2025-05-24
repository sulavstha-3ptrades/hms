package com.group4.utils;

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
}
