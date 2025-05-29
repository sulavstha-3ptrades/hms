package com.group4.services;

import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for hall management operations.
 */
public class HallService {

    /**
     * Gets all halls.
     * 
     * @return A Task that returns a list of all halls
     */
    public Task<List<Hall>> getAllHalls() {
        return new Task<List<Hall>>() {
            @Override
            protected List<Hall> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                List<Hall> halls = new ArrayList<>();

                // If file is empty, initialize with sample halls
                if (lines.isEmpty()) {
                    initializeSampleHalls();
                    lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                }


                for (String line : lines) {
                    Hall hall = Hall.fromDelimitedString(line);
                    if (hall != null) {
                        halls.add(hall);
                    }
                }


                return halls;
            }
        };
    }
    
    /**
     * Initializes the halls file with sample data if it's empty.
     * 
     * @throws IOException If an I/O error occurs
     */
    private void initializeSampleHalls() throws IOException {
        List<String> lines = new ArrayList<>();
        
        // Add sample halls
        lines.add("HALL-12345678|AUDITORIUM|500|1000.0");
        lines.add("HALL-23456789|BANQUET|200|500.0");
        lines.add("HALL-34567890|MEETING_ROOM|50|200.0");
        
        // Write sample data to file
        FileHandler.writeLines(FileConstants.getHallsFilePath(), lines);
    }

    /**
     * Gets a hall by its ID.
     * 
     * @param hallId The ID of the hall to get
     * @return A Task that returns the hall if found, null otherwise
     */
    public Task<Hall> getHallById(String hallId) {
        return new Task<Hall>() {
            @Override
            protected Hall call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());

                for (String line : lines) {
                    Hall hall = Hall.fromDelimitedString(line);
                    if (hall != null && hall.getHallId().equals(hallId)) {
                        return hall;
                    }
                }

                return null;
            }
        };
    }

    /**
     * Adds a new hall.
     * 
     * @param type        The type of the hall
     * @param capacity    The capacity of the hall
     * @param ratePerHour The rate per hour of the hall
     * @return A Task that returns the added hall if successful, null otherwise
     */
    public Task<Hall> addHall(HallType type, int capacity, double ratePerHour) {
        return new Task<Hall>() {
            @Override
            protected Hall call() throws Exception {
                // Generate a unique hall ID
                String hallId = "HALL-" + UUID.randomUUID().toString().substring(0, 8);

                // Create a new hall
                Hall hall = new Hall(hallId, type, capacity, ratePerHour);

                // Save the hall to the file
                FileHandler.appendLine(FileConstants.getHallsFilePath(), hall.toDelimitedString());

                return hall;
            }
        };
    }

    /**
     * Updates an existing hall.
     * 
     * @param hall The hall to update
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateHall(Hall hall) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Hall existingHall = Hall.fromDelimitedString(line);
                    if (existingHall != null && existingHall.getHallId().equals(hall.getHallId())) {
                        updatedLines.add(hall.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.getHallsFilePath(), updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Deletes a hall by its ID.
     * 
     * @param hallId The ID of the hall to delete
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> deleteHall(String hallId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                List<String> updatedLines = lines.stream()
                        .filter(line -> {
                            Hall hall = Hall.fromDelimitedString(line);
                            return hall == null || !hall.getHallId().equals(hallId);
                        })
                        .collect(Collectors.toList());

                if (updatedLines.size() < lines.size()) {
                    FileHandler.writeLines(FileConstants.getHallsFilePath(), updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Gets halls by type.
     * 
     * @param type The type of halls to get
     * @return A Task that returns a list of halls of the specified type
     */
    public Task<List<Hall>> getHallsByType(HallType type) {
        return new Task<List<Hall>>() {
            @Override
            protected List<Hall> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                List<Hall> halls = new ArrayList<>();

                for (String line : lines) {
                    Hall hall = Hall.fromDelimitedString(line);
                    if (hall != null && hall.getType() == type) {
                        halls.add(hall);
                    }
                }

                return halls;
            }
        };
    }

    /**
     * Gets halls by minimum capacity.
     * 
     * @param minCapacity The minimum capacity
     * @return A Task that returns a list of halls with at least the specified
     *         capacity
     */
    public Task<List<Hall>> getHallsByMinCapacity(int minCapacity) {
        return new Task<List<Hall>>() {
            @Override
            protected List<Hall> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.getHallsFilePath());
                List<Hall> halls = new ArrayList<>();

                for (String line : lines) {
                    Hall hall = Hall.fromDelimitedString(line);
                    if (hall != null && hall.getCapacity() >= minCapacity) {
                        halls.add(hall);
                    }
                }

                return halls;
            }
        };
    }
}