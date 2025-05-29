package com.group4.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles application initialization tasks such as directory and file setup.
 * Ensures all required directories and files exist with proper permissions.
 */
public final class AppInitializer {
    private static final Logger LOGGER = Logger.getLogger(AppInitializer.class.getName());

    // List of data files to be created
    private static final String[] DATA_FILES = {
            FileConstants.getUsersFilePath(),
            FileConstants.getHallsFilePath(),
            FileConstants.getBookingsFilePath(),
            FileConstants.getIssuesFilePath(),
            FileConstants.getMaintenanceScheduleFilePath(),
            FileConstants.getAvailabilityScheduleFilePath()
    };

    // Prevent instantiation
    private AppInitializer() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Initializes the application by setting up required directories and files.
     * This method should be called before the application starts.
     * 
     * @throws RuntimeException if initialization fails
     */
    public static void initialize() {
        try {
            createDataFiles();
            initializeDefaultAdmin();
            LOGGER.info("Application initialization completed successfully");
        } catch (IOException e) {
            String errorMsg = "Failed to initialize application: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Creates all required data files with proper permissions.
     * 
     * @throws IOException if file creation fails
     */
    private static void createDataFiles() throws IOException {
        for (String filePath : DATA_FILES) {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                // Ensure parent directories exist
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
                LOGGER.info("Created data file: " + path.toAbsolutePath());

                // Set appropriate permissions for the file
                setPosixPermissions(path, false);
            }
        }
    }

    /**
     * Sets POSIX file permissions on non-Windows systems.
     * 
     * @param path        Path to the file/directory
     * @param isDirectory Whether the path is a directory
     */
    private static void setPosixPermissions(Path path, boolean isDirectory) {
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            try {
                Set<PosixFilePermission> perms = new HashSet<>();

                // Owner always has read/write/execute (for directories) or read/write (for
                // files)
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                if (isDirectory) {
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                }

                // Group and others have read/execute (for directories) or read (for files)
                perms.add(PosixFilePermission.GROUP_READ);
                perms.add(PosixFilePermission.OTHERS_READ);
                if (isDirectory) {
                    perms.add(PosixFilePermission.GROUP_EXECUTE);
                    perms.add(PosixFilePermission.OTHERS_EXECUTE);
                }

                Files.setPosixFilePermissions(path, perms);
            } catch (UnsupportedOperationException e) {
                LOGGER.warning("Could not set POSIX file permissions for " + path + ": " + e.getMessage());
            } catch (IOException e) {
                LOGGER.warning("Failed to set permissions for " + path + ": " + e.getMessage());
            }
        }
    }

    /**
     * Initializes the default admin user if it doesn't exist.
     */
    private static void initializeDefaultAdmin() {
        try {
            // Use SystemInitializer to ensure default admin user exists
            boolean initialized = SystemInitializer.initializeSystem();
            if (initialized) {
                LOGGER.info("Default admin user initialization completed successfully");
            } else {
                LOGGER.warning("Default admin user initialization may have failed");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize default admin user: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize default admin user", e);
        }
    }
}
