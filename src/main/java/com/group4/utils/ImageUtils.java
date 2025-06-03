package com.group4.utils;

import javafx.scene.image.Image;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;

/**
 * Utility class for handling image operations.
 */
public class ImageUtils {
    private static final Logger logger = Logger.getLogger(ImageUtils.class.getName());

    // Default avatar path (relative to resources)
    private static final String DEFAULT_AVATAR_PATH = "@default-avatar.jpg";

    // Directory for user-uploaded images (absolute path)
    public static final String USER_IMAGES_DIR = "src/main/resources/com/group4/assets/images/users";

    // Base path for resources (used in the application)
    private static final String RESOURCES_BASE = "/com/group4/assets/images/users/";

    // Relative path from resources root
    private static final String RELATIVE_IMAGES_DIR = "com/group4/assets/images/users";
    // Maximum file size for profile pictures (5MB)
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

    private ImageUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Loads an image from the given path.
     * If the path is null or invalid, returns the default avatar.
     * 
     * @param imagePath The path to the image
     * @return The loaded image, or default avatar if loading fails
     */
    public static Image loadImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.trim().isEmpty() || imagePath.equals(DEFAULT_AVATAR_PATH)) {
                return loadDefaultAvatar();
            }

            // Try loading from resources first
            InputStream imageStream = ImageUtils.class.getResourceAsStream(imagePath);
            if (imageStream != null) {
                return new Image(imageStream);
            }

            // If resource stream is null, try loading from file system
            File imageFile = new File(USER_IMAGES_DIR + File.separator + imagePath);
            if (imageFile.exists()) {
                return new Image(imageFile.toURI().toString());
            }

            logger.warning("Could not find image at path: " + imagePath);
            return loadDefaultAvatar();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading image from path " + imagePath, e);
            return loadDefaultAvatar();
        }
    }

    /**
     * Loads the default avatar image.
     * 
     * @return The default avatar image
     */
    public static Image loadDefaultAvatar() {
        try {
            // Try loading from resources first
            String resourcePath = RESOURCES_BASE + DEFAULT_AVATAR_PATH;
            InputStream imageStream = ImageUtils.class.getResourceAsStream(resourcePath);

            if (imageStream != null) {
                return new Image(imageStream);
            }

            // If resource stream is null, try loading from file system
            File defaultFile = new File(USER_IMAGES_DIR, DEFAULT_AVATAR_PATH);
            if (defaultFile.exists()) {
                return new Image(defaultFile.toURI().toString());
            }

            logger.warning("Could not find default avatar at path: " + resourcePath);
            return createPlaceholderImage();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading default avatar", e);
            return createPlaceholderImage();
        }
    }

    /**
     * Creates a simple placeholder image when no other images are available.
     * 
     * @return A simple generated image
     */
    private static Image createPlaceholderImage() {
        return new Image(
                "data:image/svg+xml;base64," +
                        "PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIj4" +
                        "KICAgIDxjaXJjbGUgY3g9IjEwMCIgY3k9IjEwMCIgcj0iODAiIGZpbGw9IiNlMGUwZTAiLz4KICAgIDxjaXJjbG" +
                        "UgY3g9IjEwMCIgY3k9IjgwIiByPSI0MCIgZmlsbD0iI2JkYmRiZCIvPgogICAgPHBhdGggZD0iTTQwLDE4MGMwL" +
                        "TQwLDEyMC00MCwxMjAtNDAiIGZpbGw9IiNiZGJkYmQiLz4KPC9zdmc+");
    }

    /**
     * Saves an uploaded profile image and returns the relative path.
     * 
     * @param sourceFile The source image file
     * @param userId     The user ID for the filename
     * @return Relative path to the saved image, or null if saving failed
     */
    public static String saveProfileImage(File sourceFile, String userId) {
        if (sourceFile == null || userId == null || userId.trim().isEmpty()) {
            logger.warning("Invalid parameters for saveProfileImage");
            return null;
        }

        logger.info("Saving profile image for user: " + userId);
        logger.info("Source file: " + sourceFile.getAbsolutePath());

        try {
            // Validate file size
            if (sourceFile.length() > MAX_IMAGE_SIZE) {
                String msg = String.format("Image file too large: %d bytes (max: %d)",
                        sourceFile.length(), MAX_IMAGE_SIZE);
                logger.warning(msg);
                return null;
            }

            // Get the target directory for profile images
            File targetDir = getProfileImagesDirectory();
            if (targetDir == null) {
                logger.severe("Could not access or create profile images directory");
                return null;
            }

            logger.info("Target directory: " + targetDir.getAbsolutePath());

            // Generate unique filename
            String fileExt = getFileExtension(sourceFile.getName());
            String fileName = String.format("user_%s_%d.%s", userId, System.currentTimeMillis(), fileExt);
            File targetFile = new File(targetDir, fileName);

            logger.info("Target file: " + targetFile.getAbsolutePath());

            // Ensure parent directories exist
            if (!targetFile.getParentFile().exists()) {
                logger.info("Creating parent directories");
                if (!targetFile.getParentFile().mkdirs()) {
                    logger.severe("Failed to create parent directories");
                    return null;
                }
            }

            // Copy the file
            logger.info("Copying file...");
            java.nio.file.Files.copy(
                    sourceFile.toPath(),
                    targetFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Verify the file was created
            if (!targetFile.exists()) {
                logger.severe("Failed to save image: Target file not found after copy");
                return null;
            }

            // Return the resource path that will be used to load the image
            String resourcePath = RESOURCES_BASE + fileName;
            logger.info("Image saved successfully. Resource path: " + resourcePath);
            return resourcePath;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error saving profile image", e);
            return null;
        }
    }

    /**
     * Gets the file extension from a filename.
     */
    private static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1).toLowerCase();
    }

    /**
     * Gets the target directory for saving user profile images.
     * Creates the directory if it doesn't exist.
     * 
     * @return File object representing the directory, or null if creation failed
     */
    public static File getProfileImagesDirectory() {
        try {
            File imagesDir = new File(USER_IMAGES_DIR);
            if (!imagesDir.exists()) {
                boolean created = imagesDir.mkdirs();
                if (!created) {
                    logger.severe("Failed to create directory: " + imagesDir.getAbsolutePath());
                    return null;
                }
            }
            logger.info("Using profile images directory: " + imagesDir.getAbsolutePath());
            return imagesDir;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting profile images directory", e);
            return null;
        }
    }
}
