package com.group4.utils;

import com.group4.models.User;
import java.io.File;
import java.io.IOException;

/**
 * Utility class to manage file permissions based on user roles.
 */
public class FilePermissionManager {

    /**
     * Sets appropriate file permissions based on the user's role.
     * 
     * @param filePath the path to the file
     * @param user     the current user
     * @throws IOException if an I/O error occurs
     */
    public static void setFilePermissions(String filePath, User user) throws IOException {
        File file = new File(filePath);

        // Ensure the file exists
        if (!file.exists()) {
            return;
        }

        // Set permissions based on user role
        switch (user.getRole().toUpperCase()) {
            case "ADMIN":
                // Admin has full access
                file.setReadable(true);
                file.setWritable(true);
                break;
            case "MANAGER":
                // Manager has read/write access
                file.setReadable(true);
                file.setWritable(true);
                break;
            case "SCHEDULER":
                // Scheduler has conditional access
                if (filePath.contains("halls.txt") || filePath.contains("availability_schedule.txt")
                        || filePath.contains("maintenance_schedule.txt")) {
                    file.setReadable(true);
                    file.setWritable(true);
                } else {
                    file.setReadable(true);
                    file.setWritable(false);
                }
                break;
            case "CUSTOMER":
            default:
                // Customers only have read access
                file.setReadable(true);
                file.setWritable(false);
                break;
        }
    }

    /**
     * Checks if a user has write permission for a given file.
     * 
     * @param filePath the path to the file
     * @param user     the current user
     * @return true if the user has write permission, false otherwise
     */
    public static boolean hasWritePermission(String filePath, User user) {
        if (user == null) {
            return false;
        }

        switch (user.getRole().toUpperCase()) {
            case "ADMIN":
            case "MANAGER":
                return true;
            case "SCHEDULER":
                return filePath.contains("halls.txt") || filePath.contains("availability_schedule.txt")
                        || filePath.contains("maintenance_schedule.txt");
            case "CUSTOMER":
                // Allow customers to write to the bookings file
                return filePath.contains("bookings.txt");
            default:
                return false;
        }
    }

    /**
     * Checks if a user has read permission for a given file.
     * 
     * @param filePath the path to the file
     * @param user     the current user
     * @return true if the user has read permission, false otherwise
     */
    public static boolean hasReadPermission(String filePath, User user) {
        return user != null; // All authenticated users have read permission
    }
}