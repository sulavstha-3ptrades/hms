package com.group4.services;

import com.group4.models.Maintenance;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;

import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing hall maintenance schedules.
 */
public class MaintenanceService {

    /**
     * Gets all maintenance schedules.
     * 
     * @return A Task that returns a list of all maintenance schedules
     */
    public Task<List<Maintenance>> getAllMaintenanceSchedules() {
        return new Task<List<Maintenance>>() {
            @Override
            protected List<Maintenance> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);
                List<Maintenance> maintenanceList = new ArrayList<>();

                for (String line : lines) {
                    Maintenance maintenance = Maintenance.fromDelimitedString(line);
                    if (maintenance != null) {
                        maintenanceList.add(maintenance);
                    }
                }

                return maintenanceList;
            }
        };
    }

    /**
     * Gets maintenance schedules for a specific hall.
     * 
     * @param hallId The ID of the hall
     * @return A Task that returns a list of maintenance schedules for the specified hall
     */
    public Task<List<Maintenance>> getMaintenanceByHallId(String hallId) {
        return new Task<List<Maintenance>>() {
            @Override
            protected List<Maintenance> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);
                List<Maintenance> maintenanceList = new ArrayList<>();

                for (String line : lines) {
                    Maintenance maintenance = Maintenance.fromDelimitedString(line);
                    if (maintenance != null && maintenance.getHallId().equals(hallId)) {
                        maintenanceList.add(maintenance);
                    }
                }

                return maintenanceList;
            }
        };
    }

    /**
     * Gets maintenance schedules for a specific time period.
     * 
     * @param startTime The start time of the period
     * @param endTime The end time of the period
     * @return A Task that returns a list of maintenance schedules within the specified period
     */
    public Task<List<Maintenance>> getMaintenanceByTimePeriod(LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<List<Maintenance>>() {
            @Override
            protected List<Maintenance> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);
                List<Maintenance> maintenanceList = new ArrayList<>();

                for (String line : lines) {
                    Maintenance maintenance = Maintenance.fromDelimitedString(line);
                    if (maintenance != null) {
                        // Check if maintenance period overlaps with the given period
                        boolean overlaps = (maintenance.getStartTime().isBefore(endTime) || 
                                           maintenance.getStartTime().isEqual(endTime)) &&
                                          (maintenance.getEndTime().isAfter(startTime) || 
                                           maintenance.getEndTime().isEqual(startTime));
                        
                        if (overlaps) {
                            maintenanceList.add(maintenance);
                        }
                    }
                }

                return maintenanceList;
            }
        };
    }

    /**
     * Adds a new maintenance schedule.
     * 
     * @param hallId The ID of the hall
     * @param description The description of the maintenance
     * @param startTime The start time of the maintenance
     * @param endTime The end time of the maintenance
     * @return A Task that returns the added maintenance schedule if successful, null otherwise
     */
    public Task<Maintenance> addMaintenance(String hallId, String description, LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<Maintenance>() {
            @Override
            protected Maintenance call() throws Exception {
                // Generate a unique maintenance ID
                String maintenanceId = "MAINT-" + UUID.randomUUID().toString().substring(0, 8);

                // Create a new maintenance schedule
                Maintenance maintenance = new Maintenance(maintenanceId, hallId, description, startTime, endTime);

                // Save the maintenance schedule to the file
                FileHandler.appendLine(FileConstants.MAINTENANCE_SCHEDULE_FILE, maintenance.toDelimitedString());

                return maintenance;
            }
        };
    }

    /**
     * Updates an existing maintenance schedule.
     * 
     * @param maintenance The maintenance schedule to update
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateMaintenance(Maintenance maintenance) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Maintenance existingMaintenance = Maintenance.fromDelimitedString(line);
                    if (existingMaintenance != null && 
                        existingMaintenance.getMaintenanceId().equals(maintenance.getMaintenanceId())) {
                        updatedLines.add(maintenance.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.MAINTENANCE_SCHEDULE_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Deletes a maintenance schedule by its ID.
     * 
     * @param maintenanceId The ID of the maintenance schedule to delete
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> deleteMaintenance(String maintenanceId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);
                List<String> updatedLines = lines.stream()
                        .filter(line -> {
                            Maintenance maintenance = Maintenance.fromDelimitedString(line);
                            return maintenance == null || !maintenance.getMaintenanceId().equals(maintenanceId);
                        })
                        .collect(Collectors.toList());

                if (updatedLines.size() < lines.size()) {
                    FileHandler.writeLines(FileConstants.MAINTENANCE_SCHEDULE_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Checks if a hall has any maintenance scheduled for a specific time period.
     * 
     * @param hallId The ID of the hall
     * @param startTime The start time of the period
     * @param endTime The end time of the period
     * @return A Task that returns true if maintenance is scheduled, false otherwise
     */
    public Task<Boolean> isMaintenanceScheduled(String hallId, LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.MAINTENANCE_SCHEDULE_FILE);

                for (String line : lines) {
                    Maintenance maintenance = Maintenance.fromDelimitedString(line);
                    if (maintenance != null && maintenance.getHallId().equals(hallId)) {
                        // Check if maintenance period overlaps with the given period
                        boolean overlaps = (maintenance.getStartTime().isBefore(endTime) || 
                                           maintenance.getStartTime().isEqual(endTime)) &&
                                          (maintenance.getEndTime().isAfter(startTime) || 
                                           maintenance.getEndTime().isEqual(startTime));
                        
                        if (overlaps) {
                            return true;
                        }
                    }
                }

                return false;
            }
        };
    }
}
