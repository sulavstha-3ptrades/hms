package com.group4.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for handling platform-specific operations.
 */
public class PlatformUtils {
    private static final String APP_NAME = "HallManagement";
    private static final String OS = System.getProperty("os.name").toLowerCase();
    
    /**
     * Gets the appropriate application data directory based on the operating system.
     * 
     * @return Path to the application data directory
     */
    public static Path getAppDataDir() {
        Path appDataDir;
        
        if (isWindows()) {
            appDataDir = Paths.get(System.getenv("APPDATA"), APP_NAME);
        } else if (isMac()) {
            appDataDir = Paths.get(
                System.getProperty("user.home"), 
                "Library", 
                "Application Support", 
                APP_NAME
            );
        } else {
            // Linux/Unix
            appDataDir = Paths.get(
                System.getProperty("user.home"), 
                ".config", 
                APP_NAME.toLowerCase()
            );
        }
        
        return appDataDir.toAbsolutePath();
    }
    
    /**
     * Gets the path to a resource file in a platform-independent way.
     * 
     * @param resourcePath The resource path (e.g., "/com/group4/styles.css")
     * @return The absolute path to the resource
     */
    public static String getResourcePath(String resourcePath) {
        // Remove leading slash if present
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        
        // Convert to platform-specific path
        return resourcePath.replace("/", System.getProperty("file.separator"));
    }
    
    public static boolean isWindows() {
        return OS.contains("win");
    }
    
    public static boolean isMac() {
        return OS.contains("mac") || OS.contains("darwin");
    }
    
    public static boolean isUnix() {
        return !isWindows() && !isMac();
    }
    
    /**
     * Normalizes a file path for the current platform.
     * 
     * @param path The path to normalize
     * @return The normalized path
     */
    public static String normalizePath(String path) {
        return path.replace("/", System.getProperty("file.separator"))
                  .replace("\\", System.getProperty("file.separator"));
    }
}
