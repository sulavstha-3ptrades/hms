package com.group4.services;

import com.group4.models.Availability;
import com.group4.models.Hall;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.TaskUtils;

import javafx.concurrent.Task;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service class for managing hall availability.
 */
public class AvailabilityService {
    private final String availabilityFilePath = FileConstants.getAvailabilityScheduleFilePath();
    private final String maintenanceFilePath = FileConstants.getMaintenanceScheduleFilePath();
    private final HallService hallService = new HallService();

    /**
     * Adds a new availability entry for a hall.
     * 
     * @param hallId        the ID of the hall
     * @param startDateTime the start date and time
     * @param endDateTime   the end date and time
     * @param remarks       remarks about the availability
     * @param isAvailable   whether the hall is available during this time
     * @return the created availability entry
     * @throws IOException if an error occurs during file operations
     */
    public Availability addAvailability(String hallId, LocalDateTime startDateTime,
            LocalDateTime endDateTime, String remarks, boolean isAvailable)
            throws IOException, ExecutionException, InterruptedException {
        // Validate hall exists
        List<Hall> halls;
        try {
            Task<List<Hall>> hallsTask = hallService.getAllHalls();
            halls = TaskUtils.executeTask(hallsTask);
        } catch (Exception e) {
            throw new IOException("Error retrieving halls", e);
        }

        boolean hallExists = halls.stream().anyMatch(hall -> hall.getHallId().equals(hallId));
        if (!hallExists) {
            throw new IllegalArgumentException("Hall with ID " + hallId + " does not exist");
        }

        // Validate dates
        if (startDateTime.isAfter(endDateTime)) {
            throw new IllegalArgumentException("Start date/time must be before end date/time");
        }

        // Check for conflicts
        if (isConflictingAvailability(hallId, startDateTime, endDateTime)) {
            throw new IllegalArgumentException("Conflicting availability entry exists for this time period");
        }

        // Create new availability entry
        String availabilityId = "AVAIL-" + UUID.randomUUID().toString().substring(0, 8);
        Availability availability = new Availability(availabilityId, hallId, startDateTime, endDateTime, remarks,
                isAvailable);

        // Write to file
        List<Availability> availabilities = getAllAvailabilities();
        availabilities.add(availability);
        writeAvailabilities(availabilities);

        return availability;
    }

    /**
     * Adds a maintenance schedule entry for a hall.
     * 
     * @param hallId        the ID of the hall
     * @param startDateTime the start date and time
     * @param endDateTime   the end date and time
     * @param remarks       remarks about the maintenance
     * @return the created maintenance entry
     * @throws IOException if an error occurs during file operations
     */
    public Availability addMaintenance(String hallId, LocalDateTime startDateTime,
            LocalDateTime endDateTime, String remarks) throws IOException, ExecutionException, InterruptedException {
        // For maintenance, the hall is not available
        Availability maintenance = new Availability(
                "MAINT-" + UUID.randomUUID().toString().substring(0, 8),
                hallId, startDateTime, endDateTime, remarks, false);

        // Write to file
        List<Availability> maintenances = getAllMaintenanceSchedules();
        maintenances.add(maintenance);
        writeMaintenanceSchedules(maintenances);

        return maintenance;
    }

    /**
     * Gets all availability entries.
     * 
     * @return list of all availability entries
     * @throws IOException if an error occurs during file operations
     */
    public List<Availability> getAllAvailabilities() throws IOException, ExecutionException, InterruptedException {
        return readAvailabilityEntries(availabilityFilePath);
    }

    /**
     * Gets all maintenance schedule entries.
     * 
     * @return list of all maintenance schedule entries
     * @throws IOException if an error occurs during file operations
     */
    public List<Availability> getAllMaintenanceSchedules()
            throws IOException, ExecutionException, InterruptedException {
        return readAvailabilityEntries(maintenanceFilePath);
    }

    /**
     * Gets availability entries for a specific hall.
     * 
     * @param hallId the ID of the hall
     * @return list of availability entries for the hall
     * @throws IOException if an error occurs during file operations
     */
    public List<Availability> getAvailabilitiesByHall(String hallId)
            throws IOException, ExecutionException, InterruptedException {
        List<Availability> allAvailabilities = getAllAvailabilities();
        return allAvailabilities.stream()
                .filter(a -> a.getHallId().equals(hallId))
                .collect(Collectors.toList());
    }

    /**
     * Gets availability entries for a specific time period.
     * 
     * @param startDateTime the start of the period
     * @param endDateTime   the end of the period
     * @return list of availability entries that overlap with the period
     * @throws IOException if an error occurs during file operations
     */
    public List<Availability> getAvailabilitiesByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws IOException, ExecutionException, InterruptedException {
        List<Availability> allAvailabilities = getAllAvailabilities();
        return allAvailabilities.stream()
                .filter(a -> {
                    // Check if the periods overlap
                    return !(a.getEndDateTime().isBefore(startDateTime) || a.getStartDateTime().isAfter(endDateTime));
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing availability entry.
     * 
     * @param availability the updated availability entry
     * @throws IOException if an error occurs during file operations
     */
    public void updateAvailability(Availability availability)
            throws IOException, ExecutionException, InterruptedException {
        List<Availability> availabilities = getAllAvailabilities();

        for (int i = 0; i < availabilities.size(); i++) {
            if (availabilities.get(i).getAvailabilityId().equals(availability.getAvailabilityId())) {
                availabilities.set(i, availability);
                writeAvailabilities(availabilities);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Availability entry with ID " + availability.getAvailabilityId() + " not found");
    }

    /**
     * Deletes an availability entry.
     * 
     * @param availabilityId the ID of the availability entry to delete
     * @throws IOException if an error occurs during file operations
     */
    public void deleteAvailability(String availabilityId) throws IOException, ExecutionException, InterruptedException {
        List<Availability> availabilities = getAllAvailabilities();
        List<Availability> updatedAvailabilities = availabilities.stream()
                .filter(a -> !a.getAvailabilityId().equals(availabilityId))
                .collect(Collectors.toList());

        if (availabilities.size() == updatedAvailabilities.size()) {
            throw new IllegalArgumentException("Availability entry with ID " + availabilityId + " not found");
        }

        writeAvailabilities(updatedAvailabilities);
    }

    /**
     * Checks if there is a conflicting availability entry for a hall.
     * 
     * @param hallId        the ID of the hall
     * @param startDateTime the start date and time
     * @param endDateTime   the end date and time
     * @return true if there is a conflict, false otherwise
     * @throws IOException if an error occurs during file operations
     */
    private boolean isConflictingAvailability(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws IOException, ExecutionException, InterruptedException {
        List<Availability> hallAvailabilities = getAvailabilitiesByHall(hallId);

        for (Availability availability : hallAvailabilities) {
            // Check if the periods overlap
            if (!(availability.getEndDateTime().isBefore(startDateTime)
                    || availability.getStartDateTime().isAfter(endDateTime))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reads availability entries from a file.
     * 
     * @param filePath the path to the file
     * @return list of availability entries
     * @throws IOException if an error occurs during file operations
     */
    private List<Availability> readAvailabilityEntries(String filePath)
            throws IOException, ExecutionException, InterruptedException {
        Task<List<String>> readTask = FileHandler.createReadLinesTask(filePath);
        try {
            List<String> lines = TaskUtils.executeTask(readTask);
            List<Availability> availabilities = new ArrayList<>();

            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    availabilities.add(Availability.fromDelimitedString(line));
                }
            }

            return availabilities;
        } catch (Exception e) {
            throw new IOException("Error reading availability entries", e);
        }
    }

    /**
     * Writes availability entries to the file.
     * 
     * @param availabilities the list of availability entries
     * @throws IOException if an error occurs during file operations
     */
    private void writeAvailabilities(List<Availability> availabilities) throws IOException {
        List<String> lines = availabilities.stream()
                .map(Availability::toDelimitedString)
                .collect(Collectors.toList());

        try {
            Task<Boolean> writeTask = FileHandler.createWriteLinesTask(availabilityFilePath, lines);
            TaskUtils.executeTask(writeTask);
        } catch (Exception e) {
            throw new IOException("Error writing availability entries", e);
        }
    }

    /**
     * Writes maintenance schedule entries to the file.
     * 
     * @param maintenances the list of maintenance schedule entries
     * @throws IOException if an error occurs during file operations
     */
    private void writeMaintenanceSchedules(List<Availability> maintenances) throws IOException {
        List<String> lines = maintenances.stream()
                .map(Availability::toDelimitedString)
                .collect(Collectors.toList());

        try {
            Task<Boolean> writeTask = FileHandler.createWriteLinesTask(maintenanceFilePath, lines);
            TaskUtils.executeTask(writeTask);
        } catch (Exception e) {
            throw new IOException("Error writing maintenance entries", e);
        }
    }
}