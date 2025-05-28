package com.group4.utils;

import java.io.File;
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
 */
public class AppInitializer {
    private static final Logger LOGGER = Logger.getLogger(AppInitializer.class.getName());
    private static final String DATA_DIR = "data";
    private static final String[] DATA_FILES = {
        "availability_schedule.txt",
        "bookings.txt",
        "halls.txt",
        "issues.txt",
        "maintenance_schedule.txt",
        "users.txt"
    };

    /**
     * Initializes the application by setting up required directories and files.
     * This method should be called before the application starts.
     */
    public static void initialize() {
        try {
            createDataDirectory();
            createDataFiles();
            setFilePermissions();
            LOGGER.info("Application initialization completed successfully");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize application: " + e.getMessage(), e);
            throw new RuntimeException("Application initialization failed", e);
        }
    }

    private static void createDataDirectory() throws IOException {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create data directory: " + dataDir.getAbsolutePath());
            }
            LOGGER.info("Created data directory: " + dataDir.getAbsolutePath());
        }
    }

    private static void createDataFiles() throws IOException {
        for (String fileName : DATA_FILES) {
            File file = new File(DATA_DIR, fileName);
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (!created) {
                    LOGGER.warning("Failed to create data file: " + file.getAbsolutePath());
                } else {
                    LOGGER.info("Created data file: " + file.getAbsolutePath());
                }
            }
        }
    }

    private static void setFilePermissions() throws IOException {
        try {
            // Set directory permissions (rwxr-xr-x)
            Path dataPath = Paths.get(DATA_DIR);
            
            // Set directory permissions
            Set<PosixFilePermission> dirPerms = new HashSet<>();
            // User permissions
            dirPerms.add(PosixFilePermission.OWNER_READ);
            dirPerms.add(PosixFilePermission.OWNER_WRITE);
            dirPerms.add(PosixFilePermission.OWNER_EXECUTE);
            // Group permissions
            dirPerms.add(PosixFilePermission.GROUP_READ);
            dirPerms.add(PosixFilePermission.GROUP_EXECUTE);
            // Others permissions
            dirPerms.add(PosixFilePermission.OTHERS_READ);
            dirPerms.add(PosixFilePermission.OTHERS_EXECUTE);
            
            Files.setPosixFilePermissions(dataPath, dirPerms);
            
            // Set file permissions (rw-r--r--)
            Set<PosixFilePermission> filePerms = new HashSet<>();
            // User permissions
            filePerms.add(PosixFilePermission.OWNER_READ);
            filePerms.add(PosixFilePermission.OWNER_WRITE);
            // Group permissions
            filePerms.add(PosixFilePermission.GROUP_READ);
            // Others permissions
            filePerms.add(PosixFilePermission.OTHERS_READ);
            
            for (String fileName : DATA_FILES) {
                Path filePath = dataPath.resolve(fileName);
                if (Files.exists(filePath)) {
                    Files.setPosixFilePermissions(filePath, filePerms);
                }
            }
            
        } catch (UnsupportedOperationException e) {
            // This will happen on Windows when trying to set POSIX permissions
            LOGGER.info("POSIX file permissions not supported on this platform. Using default permissions.");
        }
    }
}
