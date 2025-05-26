package com.group4.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a user in the Hall Management System.
 */
public class User {
    private final StringProperty userId = new SimpleStringProperty(this, "userId", "");
    private final StringProperty firstName = new SimpleStringProperty(this, "firstName", "");
    private final StringProperty lastName = new SimpleStringProperty(this, "lastName", "");
    private final StringProperty role = new SimpleStringProperty(this, "role", "");
    private final StringProperty email = new SimpleStringProperty(this, "email", "");
    private final StringProperty password = new SimpleStringProperty(this, "password", "");
    private final StringProperty contactNumber = new SimpleStringProperty(this, "contactNumber", "");
    private final StringProperty status = new SimpleStringProperty(this, "status", "");
    private final StringProperty profilePicture = new SimpleStringProperty(this, "profilePicture", "");

    /**
     * Default constructor
     */
    public User() {
    }

    /**
     * Parameterized constructor
     */
    public User(String userId, String firstName, String lastName, String role,
            String email, String password, String contactNumber, String status) {
        this(userId, firstName, lastName, role, email, password, contactNumber, status, "");
    }
    
    public User(String userId, String firstName, String lastName, String role,
            String email, String password, String contactNumber, String status, String profilePicture) {
        this.userId.set(userId);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.role.set(role);
        this.email.set(email);
        this.password.set(password);
        this.contactNumber.set(contactNumber);
        this.status.set(status);
        this.profilePicture.set(profilePicture);
    }

    // Standard getters and setters (needed for PropertyValueFactory)
    public String getUserId() {
        return userId.get();
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getRole() {
        return role.get();
    }

    public void setRole(String role) {
        this.role.set(role);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getPassword() {
        return password.get();
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getContactNumber() {
        return contactNumber.get();
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber.set(contactNumber);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
    
    /**
     * Gets the path to the user's profile picture.
     * If no picture is set, returns the path to the default avatar.
     * The path is relative to the resources folder.
     */
    public String getProfilePicture() {
        String picture = profilePicture.get();
        if (picture == null || picture.trim().isEmpty()) {
            return "/com/group4/assets/images/users/default-avatar.jpg";
        }
        // If it's already a full path, return as is
        if (picture.startsWith("/")) {
            return picture;
        }
        // Otherwise, assume it's just a filename and return the full path
        return "/com/group4/assets/images/users/" + picture;
    }
    
    public void setProfilePicture(String profilePicture) {
        this.profilePicture.set(profilePicture);
    }
    
    public StringProperty profilePictureProperty() {
        return profilePicture;
    }

    // JavaFX property getters
    public StringProperty userIdProperty() {
        return userId;
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public StringProperty roleProperty() {
        return role;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public StringProperty contactNumberProperty() {
        return contactNumber;
    }

    public StringProperty statusProperty() {
        return status;
    }

    /**
     * Converts user object to a delimited string representation
     * 
     * @return String representation of the user
     */
    public String toDelimitedString() {
        return String.join("|",
            getUserId(),
            getFirstName(),
            getLastName(),
            getRole(),
            getEmail(),
            getPassword(),
            getContactNumber(),
            getStatus(),
            getProfilePicture()
        );
    }

    /**
     * Creates a User object from a delimited string
     * 
     * @param data The delimited string containing user data
     * @return User object
     */
    public static User fromDelimitedString(String data) {
        String[] parts = data.split("\\|", -1); // -1 to keep trailing empty strings
        if (parts.length >= 8) {
            String profilePicture = parts.length > 8 ? parts[8] : "";
            return new User(
                    parts[0], // userId
                    parts[1], // firstName
                    parts[2], // lastName
                    parts[3], // role
                    parts[4], // email
                    parts[5], // password
                    parts[6], // contactNumber
                    parts[7], // status
                    profilePicture // profilePicture (optional)
            );
        }
        return null;
    }
}