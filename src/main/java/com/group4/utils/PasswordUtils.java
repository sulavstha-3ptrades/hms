package com.group4.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtils {
    
    // Define the log-round factor (work factor) for BCrypt
    private static final int BCRYPT_WORKLOAD = 12;
    
    /**
     * Hashes a password using BCrypt.
     *
     * @param password The plain text password to hash
     * @return The hashed password
     */
    public static String hashPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_WORKLOAD));
    }
    
    /**
     * Verifies a password against a hash.
     *
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to verify against
     * @return true if the password matches the hash, false otherwise
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null || 
            !hashedPassword.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(password, hashedPassword);
    }
}
