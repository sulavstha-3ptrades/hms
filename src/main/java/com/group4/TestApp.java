package com.group4;

import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.services.HallService;
import com.group4.utils.TaskUtils;

import java.util.List;

/**
 * Simple console application to test our implementation.
 */
public class TestApp {
    public static void main(String[] args) {
        try {
            // Create a hall service
            HallService hallService = new HallService();

            // Add a hall
            Hall hall = TaskUtils.executeTask(hallService.addHall(HallType.AUDITORIUM, 100, 50.0));
            System.out.println("Added hall: " + hall.toDelimitedString());

            // Get all halls
            List<Hall> halls = TaskUtils.executeTask(hallService.getAllHalls());
            System.out.println("All halls:");
            for (Hall h : halls) {
                System.out.println(h.toDelimitedString());
            }

            System.out.println("Test completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}