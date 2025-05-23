package com.group4;

import com.group4.views.HallBookingForm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HallManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create the booking form
        HallBookingForm bookingForm = new HallBookingForm();

        // Create the scene
        Scene scene = new Scene(bookingForm, 600, 800);

        // Configure the stage
        primaryStage.setTitle("Hall Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}