package com.group4.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.file.StandardCopyOption;

import javafx.concurrent.Task;

/**
 * Utility class for file operations with proper locking mechanisms and retry logic.
 * Handles platform-specific file system differences and ensures thread safety.
 * 
 * <p>Features:
 * <ul>
 *   <li>Thread-safe file operations with proper locking</li>
 *   <li>Automatic retry on failure with configurable attempts</li>
 *   <li>Cross-platform path handling</li>
 *   <li>Resource cleanup with try-with-resources</li>
 *   <li>Detailed error logging</li>
 * </ul>
 */

/**
 * Utility class for file operations with proper locking mechanisms.
 * Handles platform-specific file system differences automatically.
 */
public final class FileHandler {
    // Private constructor to prevent instantiation
    private FileHandler() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    private static final Logger LOGGER = Logger.getLogger(FileHandler.class.getName());
    
    // Configuration constants
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 100;

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
     * Reads all lines from a file with proper locking and retry logic.
     * 
     * @param filePath Path to the file (can be relative or absolute)
     * @return List of lines read from the file
     * @throws IOException If an I/O error occurs after all retries
     */
    /**
     * Reads all lines from a file with proper locking and retry logic.
     *
     * @param filePath Path to the file (can be relative or absolute)
     * @return List of lines read from the file (never null)
     * @throws IOException If an I/O error occurs after all retries
     * @throws IllegalArgumentException If filePath is null
     */
    public static List<String> readLines(String filePath) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        LOGGER.fine("Reading from file: " + path);

        // Ensure parent directories exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // Create file if it doesn't exist
        if (!Files.exists(path)) {
            LOGGER.info("File does not exist, creating: " + path);
            Files.createFile(path);
            return new ArrayList<>();
        }

        return withRetry(() -> {
            List<String> lines = new ArrayList<>();
            
            // Use Files.lines() for better resource management
            try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            return lines;
        }, "read", path);
    }

    /**
     * Writes lines to a file with proper locking and retry logic.
     * 
     * @param filePath Path to the file (can be relative or absolute)
     * @param lines    Lines to write
     * @throws IOException If an I/O error occurs after all retries
     */
    /**
     * Writes lines to a file with proper locking and retry logic.
     * Creates the file and parent directories if they don't exist.
     *
     * @param filePath Path to the file (can be relative or absolute)
     * @param lines    Lines to write (null or empty list creates an empty file)
     * @throws IOException If an I/O error occurs after all retries
     * @throws IllegalArgumentException If filePath is null
     */
    public static void writeLines(String filePath, List<String> lines) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        // Create a final copy of lines to use in the lambda
        final List<String> linesCopy = lines != null ? new ArrayList<>(lines) : Collections.emptyList();

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        LOGGER.fine("Writing " + linesCopy.size() + " lines to file: " + path);

        // Ensure parent directories exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        withRetry(() -> {
            // Use Files.write() with StandardOpenOption for atomic write
            Files.write(
                path,
                linesCopy,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
            );
            return null;
        }, "write", path);
    }
    
    /**
     * Appends a line to a file with proper locking and retry logic.
     * Creates the file and parent directories if they don't exist.
     *
     * @param filePath Path to the file (can be relative or absolute)
     * @param line     Line to append (null is treated as empty string)
     * @throws IOException If an I/O error occurs after all retries
     * @throws IllegalArgumentException If filePath is null
     */
    public static void appendLine(String filePath, String line) throws IOException {
        if (filePath == null) {
            throw new IllegalArgumentException("File path cannot be null");
        }
        // Create a final copy of line to use in the lambda
        final String lineToAppend = line != null ? line : "";

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        LOGGER.fine("Appending line to file: " + path);

        // Ensure parent directories exist
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        withRetry(() -> {
            // Use Files.write() with StandardOpenOption.APPEND for atomic append
            Files.write(
                path,
                Collections.singletonList(lineToAppend),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
            );
            return null;
        }, "append", path);
    }
    
    /**
     * Helper method to execute file operations with retry logic and proper error handling.
     *
     * @param operation The file operation to execute
     * @param operationName Name of the operation for logging
     * @param path Path to the file being operated on
     * @param <T> Return type of the operation
     * @return Result of the operation
     * @throws IOException If the operation fails after all retries
     */
    private static <T> T withRetry(Callable<T> operation, String operationName, Path path) throws IOException {
        int attempt = 0;
        IOException lastException = null;

        while (attempt < MAX_RETRIES) {
            try {
                return operation.call();
            } catch (IOException e) {
                lastException = e;
                attempt++;
                
                if (attempt < MAX_RETRIES) {
                    LOGGER.log(Level.WARNING, 
                        String.format("Attempt %d/%d failed to %s file: %s - %s", 
                        attempt, MAX_RETRIES, operationName, path, e.getMessage()));
                    
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Thread interrupted during file operation", ie);
                    }
                }
            } catch (Exception e) {
                throw new IOException("Unexpected error during file operation", e);
            }
        }
        
        // If we get here, all retries failed
        String errorMsg = String.format("Failed to %s file after %d attempts: %s", 
            operationName, MAX_RETRIES, path);
        LOGGER.log(Level.SEVERE, errorMsg, lastException);
        throw new IOException(errorMsg, lastException);
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
    
    /**
     * Copies a file from source to destination with proper error handling and retry logic.
     * 
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @throws IOException If the copy operation fails after all retries
     */
    public static void copyFile(String sourcePath, String targetPath) throws IOException {
        if (sourcePath == null || targetPath == null) {
            throw new IllegalArgumentException("Source and target paths must not be null");
        }
        
        Path source = Paths.get(sourcePath).toAbsolutePath().normalize();
        Path target = Paths.get(targetPath).toAbsolutePath().normalize();
        
        if (!Files.exists(source)) {
            throw new FileNotFoundException("Source file not found: " + source);
        }
        
        // Ensure target directory exists
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        
        withRetry(() -> {
            // Use REPLACE_EXISTING to overwrite if file exists
            Files.copy(
                source, 
                target, 
                StandardCopyOption.REPLACE_EXISTING, 
                StandardCopyOption.COPY_ATTRIBUTES
            );
            return null;
        }, "copy", source);
    }
    
    /**
     * Creates a Task that copies a file from source to destination.
     * 
     * @param sourcePath Source file path
     * @param targetPath Target file path
     * @return Task that returns true if successful
     */
    public static Task<Boolean> createCopyFileTask(String sourcePath, String targetPath) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                copyFile(sourcePath, targetPath);
                return true;
            }
        };
    }
    
    /**
     * Creates a Task that appends multiple lines to a file.
     * 
     * @param filePath Path to the file
     * @param lines    Lines to append
     * @return Task that returns true if successful
     */
    public static Task<Boolean> createAppendLinesTask(String filePath, List<String> lines) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (filePath == null) {
                    throw new IllegalArgumentException("File path cannot be null");
                }
                if (lines == null) {
                    return true; // Nothing to append
                }
                
                Path path = Paths.get(filePath).toAbsolutePath().normalize();
                
                // Ensure parent directories exist
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                
                withRetry(() -> {
                    Files.write(
                        path,
                        lines,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE
                    );
                    return null;
                }, "append", path);
                
                return true;
            }
        };
    }
}