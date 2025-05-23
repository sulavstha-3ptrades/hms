package com.group4;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.FileInputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    static {
        try {
            // Load logging configuration
            LogManager.getLogManager().readConfiguration(new FileInputStream("logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not load logging.properties file: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("Starting Hall Management System application");

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root);

        // Add custom styles if needed
        scene.getStylesheets().add(App.class.getResource("css/styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Hall Management System");
        stage.show();

        LOGGER.info("Application UI initialized successfully");
    }

    public static void main(String[] args) {
        launch();
    }
}