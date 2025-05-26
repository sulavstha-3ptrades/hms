package com.group4.utils;

import javafx.scene.image.Image;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling image operations.
 */
public class ImageUtils {
    private static final Logger logger = Logger.getLogger(ImageUtils.class.getName());
    
    // Default avatar path (relative to resources)
    public static final String DEFAULT_AVATAR_PATH = "/images/default-avatar.jpg";
    // Directory for user-uploaded images (relative to resources)
    public static final String USER_IMAGES_DIR = "src/main/resources/com/group4/assets/images/users/";
    
    // Base path for resources (used in the application)
    private static final String RESOURCES_BASE = "/com/group4/assets/images/users/";
    // Maximum file size for profile pictures (5MB)
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    
    private ImageUtils() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Loads an image from the specified path, with fallback to default avatar.
     * @param imagePath Path to the image (can be null or empty for default avatar)
     * @return Loaded image or default avatar if loading fails
     */
    public static Image loadImage(String imagePath) {
        // If no path provided, use default avatar
        if (imagePath == null || imagePath.trim().isEmpty()) {
            logger.info("No image path provided, using default avatar");
            return loadDefaultAvatar();
        }
        
        logger.info("Attempting to load image from path: " + imagePath);
        
        try {
            // First try to load from the filesystem (for development)
            File imageFile = new File(imagePath);
            
            // If not found, try to construct path relative to resources
            if (!imageFile.exists() && imagePath.startsWith("/")) {
                // Remove leading slash for filesystem path
                String fsPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
                imageFile = new File("src/main/resources" + (fsPath.startsWith("/") ? "" : "/") + fsPath);
                logger.fine("Trying filesystem path: " + imageFile.getAbsolutePath());
            }
            
            if (imageFile.exists()) {
                logger.fine("Loading image from filesystem: " + imageFile.getAbsolutePath());
                try (FileInputStream fis = new FileInputStream(imageFile)) {
                    Image image = new Image(fis);
                    if (!image.isError()) {
                        logger.fine("Successfully loaded image from filesystem: " + imageFile.getAbsolutePath());
                        return image;
                    } else {
                        logger.warning("Error loading image (file exists but couldn't be loaded): " + imageFile.getAbsolutePath());
                    }
                }
            } else {
                logger.fine("File not found at: " + imageFile.getAbsolutePath());
            }
            
            // Try to load as resource (for packaged JAR)
            String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
            logger.fine("Trying to load as resource: " + resourcePath);
            
            try (InputStream is = ImageUtils.class.getResourceAsStream(resourcePath)) {
                if (is != null) {
                    Image image = new Image(is);
                    if (!image.isError()) {
                        logger.fine("Successfully loaded image from resource: " + resourcePath);
                        return image;
                    } else {
                        logger.warning("Error loading image from resource (image error): " + resourcePath);
                    }
                } else {
                    logger.fine("Resource not found: " + resourcePath);
                }
            }
            
            // Try to load from the user's home directory as a last resort
            String homePath = System.getProperty("user.home") + "/.hall_management" + resourcePath;
            File homeFile = new File(homePath);
            if (homeFile.exists()) {
                logger.fine("Trying to load from user's home directory: " + homePath);
                try (FileInputStream fis = new FileInputStream(homeFile)) {
                    Image image = new Image(fis);
                    if (!image.isError()) {
                        logger.fine("Successfully loaded image from home directory: " + homePath);
                        return image;
                    }
                }
            }
            
            logger.warning("Failed to load image from path: " + imagePath);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error loading image from " + imagePath, e);
        }
        
        // Fall back to default avatar if anything goes wrong
        logger.info("Falling back to default avatar");
        return loadDefaultAvatar();
    }
    
    /**
     * Loads the default avatar image.
     * @return The default avatar image
     */
    public static Image loadDefaultAvatar() {
        // First try loading from the compiled resources
        try (InputStream is = ImageUtils.class.getResourceAsStream(DEFAULT_AVATAR_PATH)) {
            if (is != null) {
                return new Image(is);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load default avatar from resources", e);
        }
        
        // Then try loading from the target directory (for development)
        try {
            File defaultAvatar = new File("target/classes/com/group4/assets/images/users/default-avatar.jpg");
            if (defaultAvatar.exists()) {
                try (FileInputStream fis = new FileInputStream(defaultAvatar)) {
                    return new Image(fis);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load default avatar from target directory", e);
        }
        
        // If all else fails, create a placeholder
        logger.warning("Falling back to placeholder image");
        return createPlaceholderImage();
    }
    
    /**
     * Creates a simple placeholder image when no other images are available.
     * @return A simple generated image
     */
    private static Image createPlaceholderImage() {
        // Create a simple colored rectangle as a last resort
        return new Image("data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200'><rect width='200' height='200' fill='%23cccccc'/><text x='50%' y='50%' font-family='Arial' font-size='14' text-anchor='middle' dominant-baseline='middle' fill='%23666666'>No Image</text></svg>");
    }
    
    /**
     * Saves an uploaded profile image and returns the relative path.
     * @param sourceFile The source image file
     * @param userId The user ID for the filename
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
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );
            
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
     * @return File object representing the directory, or null if creation failed
     */
    private static File getProfileImagesDirectory() {
        try {
            // First try the development location (src/main/resources/...)
            File devDir = new File("src/main/resources" + USER_IMAGES_DIR);
            
            // Create parent directories if they don't exist
            if (!devDir.exists()) {
                if (!devDir.mkdirs()) {
                    logger.warning("Failed to create development directory: " + devDir.getAbsolutePath());
                    // Fall back to user's home directory
                    File homeDir = new File(System.getProperty("user.home"), ".hall_management" + USER_IMAGES_DIR);
                    if (!homeDir.exists() && !homeDir.mkdirs()) {
                        logger.severe("Failed to create home directory: " + homeDir.getAbsolutePath());
                        return null;
                    }
                    return homeDir;
                }
                return devDir;
            }
            return devDir;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting profile images directory", e);
            return null;
        }
    }
}
