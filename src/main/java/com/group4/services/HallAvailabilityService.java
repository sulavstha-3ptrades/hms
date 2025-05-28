package com.group4.services;

import com.group4.models.HallAvailability;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;

import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service class for managing hall availability.
 */
public class HallAvailabilityService {

    /**
     * Gets all hall availability records.
     * 
     * @return A Task that returns a list of all hall availability records
     */
    public Task<List<HallAvailability>> getAllAvailability() {
        return new Task<List<HallAvailability>>() {
            @Override
            protected List<HallAvailability> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<HallAvailability> availabilityList = new ArrayList<>();

                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null) {
                        availabilityList.add(availability);
                    }
                }

                return availabilityList;
            }
        };
    }

    /**
     * Gets availability records for a specific hall.
     * 
     * @param hallId The ID of the hall
     * @return A Task that returns a list of availability records for the specified hall
     */
    public Task<List<HallAvailability>> getAvailabilityByHallId(String hallId) {
        return new Task<List<HallAvailability>>() {
            @Override
            protected List<HallAvailability> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<HallAvailability> availabilityList = new ArrayList<>();

                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null && availability.getHallId().equals(hallId)) {
                        availabilityList.add(availability);
                    }
                }

                return availabilityList;
            }
        };
    }

    /**
     * Gets availability records for a specific time period.
     * 
     * @param startTime The start time of the period
     * @param endTime The end time of the period
     * @return A Task that returns a list of availability records within the specified period
     */
    public Task<List<HallAvailability>> getAvailabilityByTimePeriod(LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<List<HallAvailability>>() {
            @Override
            protected List<HallAvailability> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<HallAvailability> availabilityList = new ArrayList<>();

                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null) {
                        // Check if availability period overlaps with the given period
                        boolean overlaps = (availability.getStartTime().isBefore(endTime) || 
                                           availability.getStartTime().isEqual(endTime)) &&
                                          (availability.getEndTime().isAfter(startTime) || 
                                           availability.getEndTime().isEqual(startTime));
                        
                        if (overlaps) {
                            availabilityList.add(availability);
                        }
                    }
                }

                return availabilityList;
            }
        };
    }

    /**
     * Gets available halls for a specific time period.
     * 
     * @param startTime The start time of the period
     * @param endTime The end time of the period
     * @return A Task that returns a list of hall IDs that are available during the specified period
     */
    public Task<List<String>> getAvailableHallIds(LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<String> availableHallIds = new ArrayList<>();

                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null && availability.getStatus().equals("AVAILABLE")) {
                        // Check if availability period overlaps with the given period
                        boolean overlaps = (availability.getStartTime().isBefore(endTime) || 
                                           availability.getStartTime().isEqual(endTime)) &&
                                          (availability.getEndTime().isAfter(startTime) || 
                                           availability.getEndTime().isEqual(startTime));
                        
                        if (overlaps && !availableHallIds.contains(availability.getHallId())) {
                            availableHallIds.add(availability.getHallId());
                        }
                    }
                }

                return availableHallIds;
            }
        };
    }

    /**
     * Adds a new hall availability record.
     * 
     * @param hallId The ID of the hall
     * @param startTime The start time of the availability
     * @param endTime The end time of the availability
     * @param status The status of the availability (AVAILABLE or UNAVAILABLE)
     * @return A Task that returns the added availability record if successful, null otherwise
     */
    public Task<HallAvailability> addAvailability(String hallId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        return new Task<HallAvailability>() {
            @Override
            protected HallAvailability call() throws Exception {
                // Generate a unique availability ID
                String availabilityId = "AVAIL-" + UUID.randomUUID().toString().substring(0, 8);

                // Create a new availability record
                HallAvailability availability = new HallAvailability(availabilityId, hallId, startTime, endTime, status);

                // Save the availability record to the file
                FileHandler.appendLine(FileConstants.AVAILABILITY_SCHEDULE_FILE, availability.toDelimitedString());

                return availability;
            }
        };
    }

    /**
     * Updates an existing hall availability record.
     * 
     * @param availability The availability record to update
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateAvailability(HallAvailability availability) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    HallAvailability existingAvailability = HallAvailability.fromDelimitedString(line);
                    if (existingAvailability != null && 
                        existingAvailability.getAvailabilityId().equals(availability.getAvailabilityId())) {
                        updatedLines.add(availability.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.AVAILABILITY_SCHEDULE_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Deletes a hall availability record by its ID.
     * 
     * @param availabilityId The ID of the availability record to delete
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> deleteAvailability(String availabilityId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                List<String> updatedLines = lines.stream()
                        .filter(line -> {
                            HallAvailability availability = HallAvailability.fromDelimitedString(line);
                            return availability == null || !availability.getAvailabilityId().equals(availabilityId);
                        })
                        .collect(Collectors.toList());

                if (updatedLines.size() < lines.size()) {
                    FileHandler.writeLines(FileConstants.AVAILABILITY_SCHEDULE_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Checks if a hall is available for a specific time period.
     * 
     * @param hallId The ID of the hall
     * @param startTime The start time of the period
     * @param endTime The end time of the period
     * @return A Task that returns true if the hall is available, false otherwise
     */
    public Task<Boolean> isHallAvailable(String hallId, LocalDateTime startTime, LocalDateTime endTime) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.AVAILABILITY_SCHEDULE_FILE);
                
                // First, check if there's an explicit availability record for this period
                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null && availability.getHallId().equals(hallId)) {
                        // Check if availability period contains the given period
                        boolean contains = (availability.getStartTime().isBefore(startTime) || 
                                           availability.getStartTime().isEqual(startTime)) &&
                                          (availability.getEndTime().isAfter(endTime) || 
                                           availability.getEndTime().isEqual(endTime));
                        
                        if (contains) {
                            return availability.getStatus().equals("AVAILABLE");
                        }
                    }
                }
                
                // If no explicit record found, check for any overlapping unavailable periods
                for (String line : lines) {
                    HallAvailability availability = HallAvailability.fromDelimitedString(line);
                    if (availability != null && 
                        availability.getHallId().equals(hallId) && 
                        availability.getStatus().equals("UNAVAILABLE")) {
                        
                        // Check if unavailable period overlaps with the given period
                        boolean overlaps = (availability.getStartTime().isBefore(endTime) || 
                                           availability.getStartTime().isEqual(endTime)) &&
                                          (availability.getEndTime().isAfter(startTime) || 
                                           availability.getEndTime().isEqual(startTime));
                        
                        if (overlaps) {
                            return false;
                        }
                    }
                }
                
                // If no unavailable periods overlap, the hall is available
                return true;
            }
        };
    }
}
