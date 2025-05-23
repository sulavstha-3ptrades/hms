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
        this.userId.set(userId);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
        this.role.set(role);
        this.email.set(email);
        this.password.set(password);
        this.contactNumber.set(contactNumber);
        this.status.set(status);
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
        return getUserId() + "|" + getFirstName() + "|" + getLastName() + "|" + getRole() + "|" +
                getEmail() + "|" + getPassword() + "|" + getContactNumber() + "|" + getStatus();
    }

    /**
     * Creates a User object from a delimited string
     * 
     * @param data The delimited string containing user data
     * @return User object
     */
    public static User fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length == 8) {
            return new User(
                    parts[0], // userId
                    parts[1], // firstName
                    parts[2], // lastName
                    parts[3], // role
                    parts[4], // email
                    parts[5], // password
                    parts[6], // contactNumber
                    parts[7] // status
            );
        }
        return null;
    }
}