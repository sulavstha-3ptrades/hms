package com.group4.utils;

import com.group4.models.User;

/**
 * Singleton class for managing user sessions.
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    /**
     * Private constructor to prevent instantiation.
     */
    private SessionManager() {
    }

    /**
     * Get the singleton instance.
     * 
     * @return The SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Set the current user for the session.
     * 
     * @param user The user to set as the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Get the current user.
     * 
     * @return The current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if a user is logged in.
     * 
     * @return True if a user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if the current user has the specified role.
     * 
     * @param role The role to check
     * @return True if the current user has the specified role, false otherwise
     */
    public boolean hasRole(String role) {
        return isLoggedIn() && currentUser.getRole().equals(role);
    }

    /**
     * Clear the current user session.
     */
    public void logout() {
        currentUser = null;
    }
}