package com.group4.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Centralized file path management for the Hall Management System.
 * Provides platform-independent file paths and ensures proper directory structure.
 * All paths are relative to the application's data directory.
 * 
 * <p>Key features:
 * <ul>
 *   <li>Cross-platform path handling</li>
 *   <li>Automatic directory creation</li>
 *   <li>Consistent file naming</li>
 *   <li>Centralized path management</li>
 * </ul>
 */
public final class FileConstants {
    private static final Logger LOGGER = Logger.getLogger(FileConstants.class.getName());
    
    // Private constructor to prevent instantiation
    private FileConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    // Base directory structure
    /** Base directory for all application data */
    public static final String DATA_DIR = "data";
    
    /** Directory for all image files */
    public static final String IMAGES_DIR = "images";
    
    /** Directory for user profile images */
    public static final String USER_IMAGES_DIR = joinPaths(IMAGES_DIR, "users");
    
    /** Directory for hall images */
    public static final String HALL_IMAGES_DIR = joinPaths(IMAGES_DIR, "halls");
    
    // File extensions
    private static final String TXT_EXTENSION = ".txt";
    private static final String PNG_EXTENSION = ".png";
    
    // Default filenames
    private static final String DEFAULT_USER_IMAGE = "default-user" + PNG_EXTENSION;
    private static final String DEFAULT_HALL_IMAGE = "default-hall" + PNG_EXTENSION;

    // Data files
    /** Users data file */
    public static final String USERS_FILE = "users" + TXT_EXTENSION;
    
    /** Halls data file */
    public static final String HALLS_FILE = "halls" + TXT_EXTENSION;
    
    /** Bookings data file */
    public static final String BOOKINGS_FILE = "bookings" + TXT_EXTENSION;
    
    /** Issues data file */
    public static final String ISSUES_FILE = "issues" + TXT_EXTENSION;
    
    /** Availability schedule data file */
    public static final String AVAILABILITY_SCHEDULE_FILE = "availability_schedule" + TXT_EXTENSION;
    
    /** Maintenance schedule data file */
    public static final String MAINTENANCE_SCHEDULE_FILE = "maintenance_schedule" + TXT_EXTENSION;
    
    /**
     * Joins path components using the system's file separator.
     * 
     * @param first First path component
     * @param more Additional path components
     * @return Combined path as a string
     */
    public static String joinPaths(String first, String... more) {
        return Path.of(first, more).toString();
    }
    
    /**
     * Gets the application's base data directory.
     * 
     * @return Path to the application data directory
     */
    public static Path getAppDataDir() {
        return PlatformUtils.getAppDataDir();
    }
    
    /**
     * Gets the full path to the users data file.
     * 
     * @return Full path to users.txt
     */
    public static String getUsersFilePath() {
        return getDataFilePath(USERS_FILE);
    }
    
    /**
     * Gets the full path to the halls data file.
     * 
     * @return Full path to halls.txt
     */
    public static String getHallsFilePath() {
        return getDataFilePath(HALLS_FILE);
    }
    
    /**
     * Gets the full path to the bookings data file.
     * 
     * @return Full path to bookings.txt
     */
    public static String getBookingsFilePath() {
        return getDataFilePath(BOOKINGS_FILE);
    }
    
    /**
     * Gets the full path to the issues data file.
     * 
     * @return Full path to issues.txt
     */
    public static String getIssuesFilePath() {
        return getDataFilePath(ISSUES_FILE);
    }
    
    /**
     * Gets the full path to the availability schedule file.
     * 
     * @return Full path to availability_schedule.txt
     */
    public static String getAvailabilityScheduleFilePath() {
        return getDataFilePath(AVAILABILITY_SCHEDULE_FILE);
    }
    
    /**
     * Gets the full path to the maintenance schedule file.
     * 
     * @return Full path to maintenance_schedule.txt
     */
    public static String getMaintenanceScheduleFilePath() {
        return getDataFilePath(MAINTENANCE_SCHEDULE_FILE);
    }
    
    /**
     * Gets the full path to a user image file.
     * 
     * @param filename The name of the image file
     * @return Full path to the user image file
     */
    public static String getUserImagePath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return getDefaultUserImagePath();
        }
        return getAppDataDir().resolve(USER_IMAGES_DIR).resolve(filename).toString();
    }
    
    /**
     * Gets the full path to a hall image file.
     * 
     * @param filename The name of the image file
     * @return Full path to the hall image file
     */
    public static String getHallImagePath(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return getDefaultHallImagePath();
        }
        return getAppDataDir().resolve(HALL_IMAGES_DIR).resolve(filename).toString();
    }
    
    /**
     * Gets the path to the default user image.
     * 
     * @return Path to the default user image
     */
    public static String getDefaultUserImagePath() {
        return getAppDataDir().resolve(USER_IMAGES_DIR).resolve(DEFAULT_USER_IMAGE).toString();
    }
    
    /**
     * Gets the path to the data directory.
     * 
     * @param filename The name of the file in the data directory
     * @return Full path to the file in the data directory
     */
    private static String getDataFilePath(String filename) {
        return getAppDataDir().resolve(DATA_DIR).resolve(filename).toString();
    }
    
    /**
     * Gets the path to the default hall image.
     * 
     * @return Path to the default hall image
     */
    public static String getDefaultHallImagePath() {
        return getHallImagePath(DEFAULT_HALL_IMAGE);
    }
    
    static {
        createApplicationDirectories();
    }
    
    /**
     * Creates all required application directories if they don't exist.
     * This includes data and image directories.
     */
    private static void createApplicationDirectories() {
        try {
            // Create all required directories
            Path appDataDir = getAppDataDir();
            Files.createDirectories(appDataDir.resolve(DATA_DIR));
            Files.createDirectories(appDataDir.resolve(USER_IMAGES_DIR));
            Files.createDirectories(appDataDir.resolve(HALL_IMAGES_DIR));
            
            // Create default images if they don't exist
            createDefaultImageIfNotExists(
                appDataDir.resolve(USER_IMAGES_DIR).resolve(DEFAULT_USER_IMAGE),
                "/com/group4/images/default-user.png");
                
            createDefaultImageIfNotExists(
                appDataDir.resolve(HALL_IMAGES_DIR).resolve(DEFAULT_HALL_IMAGE),
                "/com/group4/images/default-hall.png");
                
            LOGGER.info("Application directories initialized successfully");
        } catch (Exception e) {
            String errorMsg = "Failed to initialize application directories: " + e.getMessage();
            LOGGER.severe(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    /**
     * Copies a default image from resources to the specified path if it doesn't exist.
     * 
     * @param targetPath Path where the default image should be created
     * @param resourcePath Path to the resource image
     */
    private static void createDefaultImageIfNotExists(Path targetPath, String resourcePath) {
        if (Files.exists(targetPath)) {
            return;
        }
        
        try (InputStream is = FileConstants.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOGGER.warning("Resource not found: " + resourcePath);
                return;
            }
            
            // Ensure parent directory exists
            Path parentDir = targetPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Copy the resource to the target path
            Files.copy(is, targetPath);
            LOGGER.fine("Created default image: " + targetPath);
        } catch (IOException e) {
            String errorMsg = "Failed to create default image: " + targetPath + " - " + e.getMessage();
            LOGGER.severe(errorMsg);
            // Don't throw exception - the app can run without default images
        }
    }
}