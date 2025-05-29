package com.group4;

import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;

import java.io.IOException;
import java.util.List;

/**
 * Simple console application to test our implementation without JavaFX
 * dependencies.
 */
public class ConsoleApp {
    public static void main(String[] args) {
        try {
            // Add a hall
            Hall hall = new Hall("HALL-001", HallType.AUDITORIUM, 100, 50.0);
            System.out.println("Created hall: " + hall.toDelimitedString());

            // Save the hall to the file
            FileHandler.appendLine(FileConstants.getHallsFilePath(), hall.toDelimitedString());
            System.out.println("Hall saved to file.");

            // Read all halls from the file
            List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
            System.out.println("All halls:");
            for (String line : lines) {
                Hall h = Hall.fromDelimitedString(line);
                if (h != null) {
                    System.out.println(h.toDelimitedString());
                }
            }

            System.out.println("Test completed successfully!");
        } catch (IOException e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}