package com.group4.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.group4.models.User;
import javafx.concurrent.Task;

/**
 * Utility class for file operations with proper locking mechanisms.
 */
public class FileHandler {

    /**
     * Creates a Task that reads all lines from a file.
     * 
     * @param filePath Path to the file
     * @return Task that returns a list of lines
     */
    public static Task<List<String>> createReadLinesTask(String filePath) {
        return new Task<List<String>>() {
            @Override
            protected List<String> call() throws Exception {
                return readLines(filePath);
            }
        };
    }

    /**
     * Creates a Task that writes lines to a file.
     * 
     * @param filePath Path to the file
     * @param lines    Lines to write
     * @return Task that returns true if successful
     */
    public static Task<Boolean> createWriteLinesTask(String filePath, List<String> lines) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                writeLines(filePath, lines);
                return true;
            }
        };
    }

    /**
     * Reads all lines from a file with proper locking.
     * 
     * @param filePath Path to the file
     * @return List of lines read from the file
     * @throws IOException If an I/O error occurs
     */
    public static List<String> readLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // Create directory if it doesn't exist
        Files.createDirectories(path.getParent());

        // Create file if it doesn't exist
        if (!Files.exists(path)) {
            Files.createFile(path);
            return new ArrayList<>();
        }

        // Check permission if there's a current user
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && !FilePermissionManager.hasReadPermission(filePath, currentUser)) {
            throw new IOException("Access denied: No read permission for " + filePath);
        }

        List<String> lines = new ArrayList<>();

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
                FileChannel channel = file.getChannel()) {

            // Acquire a shared lock for reading
            try (FileLock lock = channel.lock(0L, Long.MAX_VALUE, true)) {
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }
            }
        }

        return lines;
    }

    /**
     * Writes lines to a file with proper locking.
     * 
     * @param filePath Path to the file
     * @param lines    Lines to write
     * @throws IOException If an I/O error occurs
     */
    public static void writeLines(String filePath, List<String> lines) throws IOException {
        Path path = Paths.get(filePath);

        // Create directory if it doesn't exist
        Files.createDirectories(path.getParent());

        // Check permission if there's a current user
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && !FilePermissionManager.hasWritePermission(filePath, currentUser)) {
            throw new IOException("Access denied: No write permission for " + filePath);
        }

        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw");
                FileChannel channel = file.getChannel()) {

            // Acquire an exclusive lock for writing
            try (FileLock lock = channel.lock()) {
                // Truncate the file
                channel.truncate(0);

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }
                    writer.flush();
                }
            }
        }
    }

    /**
     * Appends a line to a file with proper locking.
     * 
     * @param filePath Path to the file
     * @param line     Line to append
     * @throws IOException If an I/O error occurs
     */
    public static void appendLine(String filePath, String line) throws IOException {
        Path path = Paths.get(filePath);

        // Create directory if it doesn't exist
        Files.createDirectories(path.getParent());

        // Check permission if there's a current user
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && !FilePermissionManager.hasWritePermission(filePath, currentUser)) {
            throw new IOException("Access denied: No write permission for " + filePath);
        }

        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw");
                FileChannel channel = file.getChannel()) {

            // Acquire an exclusive lock for writing
            try (FileLock lock = channel.lock()) {
                // Move to the end of the file
                file.seek(file.length());

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                }
            }
        }
    }

    /**
     * Creates a Task that appends a line to a file.
     * 
     * @param filePath Path to the file
     * @param line     Line to append
     * @return Task that returns true if successful
     */
    public static Task<Boolean> createAppendLineTask(String filePath, String line) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                appendLine(filePath, line);
                return true;
            }
        };
    }
}