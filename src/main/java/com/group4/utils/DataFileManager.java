package com.group4.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages file paths for data storage in a cross-platform way.
 * Ensures all data files are stored in the 'data' directory.
 */
public final class DataFileManager {
    private static final String DATA_DIR = "data";
    
    // Prevent instantiation
    private DataFileManager() {}
    
    /**
     * Gets the path to a data file, ensuring the data directory exists.
     * @param fileName Name of the data file
     * @return Absolute path to the data file
     */
    public static String getDataFilePath(String fileName) {
        Path dataDir = Paths.get(DATA_DIR);
        // Create data directory if it doesn't exist
        if (!dataDir.toFile().exists()) {
            dataDir.toFile().mkdirs();
        }
        return dataDir.resolve(fileName).toString();
    }
    
    // Common data file paths
    public static String getUsersFilePath() {
        return getDataFilePath("users.txt");
    }
    
    public static String getHallsFilePath() {
        return getDataFilePath("halls.txt");
    }
    
    public static String getBookingsFilePath() {
        return getDataFilePath("bookings.txt");
    }
    
    public static String getIssuesFilePath() {
        return getDataFilePath("issues.txt");
    }
    
    public static String getMaintenanceScheduleFilePath() {
        return getDataFilePath("maintenance_schedule.txt");
    }
    
    public static String getAvailabilityScheduleFilePath() {
        return getDataFilePath("availability_schedule.txt");
    }
}
