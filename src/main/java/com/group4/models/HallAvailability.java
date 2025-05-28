package com.group4.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing hall availability schedule.
 */
public class HallAvailability {
    private final StringProperty availabilityId = new SimpleStringProperty(this, "availabilityId", "");
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final ObjectProperty<LocalDateTime> startTime = new SimpleObjectProperty<>(this, "startTime", null);
    private final ObjectProperty<LocalDateTime> endTime = new SimpleObjectProperty<>(this, "endTime", null);
    private final StringProperty status = new SimpleStringProperty(this, "status", "AVAILABLE");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Default constructor
     */
    public HallAvailability() {
    }

    /**
     * Parameterized constructor
     */
    public HallAvailability(String availabilityId, String hallId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.availabilityId.set(availabilityId);
        this.hallId.set(hallId);
        this.startTime.set(startTime);
        this.endTime.set(endTime);
        this.status.set(status);
    }

    // Getters and Setters
    public String getAvailabilityId() {
        return availabilityId.get();
    }

    public void setAvailabilityId(String availabilityId) {
        this.availabilityId.set(availabilityId);
    }

    public String getHallId() {
        return hallId.get();
    }

    public void setHallId(String hallId) {
        this.hallId.set(hallId);
    }

    public LocalDateTime getStartTime() {
        return startTime.get();
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime.set(startTime);
    }

    public LocalDateTime getEndTime() {
        return endTime.get();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime.set(endTime);
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    // JavaFX property getters
    public StringProperty availabilityIdProperty() {
        return availabilityId;
    }

    public StringProperty hallIdProperty() {
        return hallId;
    }

    public ObjectProperty<LocalDateTime> startTimeProperty() {
        return startTime;
    }

    public ObjectProperty<LocalDateTime> endTimeProperty() {
        return endTime;
    }

    public StringProperty statusProperty() {
        return status;
    }

    /**
     * Converts hall availability object to a delimited string representation
     * 
     * @return String representation of the hall availability
     */
    public String toDelimitedString() {
        return getAvailabilityId() + "|" + 
               getHallId() + "|" + 
               getStartTime().format(formatter) + "|" + 
               getEndTime().format(formatter) + "|" + 
               getStatus();
    }

    /**
     * Creates a HallAvailability object from a delimited string
     * 
     * @param data The delimited string containing hall availability data
     * @return HallAvailability object
     */
    public static HallAvailability fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 5) {
            return new HallAvailability(
                    parts[0], // availabilityId
                    parts[1], // hallId
                    LocalDateTime.parse(parts[2], formatter), // startTime
                    LocalDateTime.parse(parts[3], formatter), // endTime
                    parts[4]  // status
            );
        }
        return null;
    }
}
