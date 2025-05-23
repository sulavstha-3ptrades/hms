package com.group4.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents an issue related to a hall in the Hall Management System.
 */
public class Issue {
    private final StringProperty issueId = new SimpleStringProperty(this, "issueId", "");
    private final StringProperty customerId = new SimpleStringProperty(this, "customerId", "");
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final StringProperty assignedStaffId = new SimpleStringProperty(this, "assignedStaffId", "");
    private final ObjectProperty<IssueStatus> status = new SimpleObjectProperty<>(this, "status", IssueStatus.OPEN);

    /**
     * Default constructor
     */
    public Issue() {
    }

    /**
     * Parameterized constructor
     */
    public Issue(String issueId, String customerId, String hallId,
            String description, String assignedStaffId, IssueStatus status) {
        this.issueId.set(issueId);
        this.customerId.set(customerId);
        this.hallId.set(hallId);
        this.description.set(description);
        this.assignedStaffId.set(assignedStaffId);
        this.status.set(status);
    }

    // Getters and Setters
    public String getIssueId() {
        return issueId.get();
    }

    public void setIssueId(String issueId) {
        this.issueId.set(issueId);
    }

    public String getCustomerId() {
        return customerId.get();
    }

    public void setCustomerId(String customerId) {
        this.customerId.set(customerId);
    }

    public String getHallId() {
        return hallId.get();
    }

    public void setHallId(String hallId) {
        this.hallId.set(hallId);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public String getAssignedStaffId() {
        return assignedStaffId.get();
    }

    public void setAssignedStaffId(String assignedStaffId) {
        this.assignedStaffId.set(assignedStaffId);
    }

    public IssueStatus getStatus() {
        return status.get();
    }

    public void setStatus(IssueStatus status) {
        this.status.set(status);
    }

    // JavaFX property getters
    public StringProperty issueIdProperty() {
        return issueId;
    }

    public StringProperty customerIdProperty() {
        return customerId;
    }

    public StringProperty hallIdProperty() {
        return hallId;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty assignedStaffIdProperty() {
        return assignedStaffId;
    }

    public ObjectProperty<IssueStatus> statusProperty() {
        return status;
    }

    /**
     * Converts issue object to a delimited string representation
     * 
     * @return String representation of the issue
     */
    public String toDelimitedString() {
        return getIssueId() + "|" + getCustomerId() + "|" + getHallId() + "|" +
                getDescription().replace("\n", "\\n") + "|" +
                getAssignedStaffId() + "|" + getStatus().name();
    }

    /**
     * Creates an Issue object from a delimited string
     * 
     * @param data The delimited string containing issue data
     * @return Issue object
     */
    public static Issue fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length == 6) {
            return new Issue(
                    parts[0], // issueId
                    parts[1], // customerId
                    parts[2], // hallId
                    parts[3].replace("\\n", "\n"), // description
                    parts[4], // assignedStaffId
                    IssueStatus.valueOf(parts[5]) // status
            );
        }
        return null;
    }
}