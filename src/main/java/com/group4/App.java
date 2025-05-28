package com.group4;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import com.group4.utils.ViewManager;
import com.group4.utils.AppInitializer;

/**
 * JavaFX App
 */
public class App extends Application {
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    static {
        try {
            // Load logging configuration from classpath
            LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not load logging.properties file: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        Platform.setImplicitExit(true);
        LOGGER.info("Starting Hall Management System application");
        
        // Initialize application directories and files with proper permissions
        LOGGER.info("Initializing application resources...");
        AppInitializer.initialize();

        // Load the login screen
        FXMLLoader loader = new FXMLLoader(App.class.getResource("view/Login.fxml"));
        Parent root = loader.load();

        // Add custom styles
        String css = App.class.getResource("css/styles.css").toExternalForm();

        // Initialize ViewManager with the first view
        ViewManager.initialize(stage, root);
        ViewManager.getMainScene().getStylesheets().add(css);

        stage.setTitle("Hall Management System");
        stage.setMaximized(true);
        stage.show();

        LOGGER.info("Application UI initialized successfully");
    }

    public static void main(String[] args) {
        launch();
    }
}