package com.group4.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.*;

/**
 * Represents the availability of a hall.
 */
public class Availability {
    private final StringProperty availabilityId = new SimpleStringProperty(this, "availabilityId", "");
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final ObjectProperty<LocalDateTime> startDateTime = new SimpleObjectProperty<>(this, "startDateTime", null);
    private final ObjectProperty<LocalDateTime> endDateTime = new SimpleObjectProperty<>(this, "endDateTime", null);
    private final StringProperty remarks = new SimpleStringProperty(this, "remarks", "");
    private final BooleanProperty available = new SimpleBooleanProperty(this, "available", true);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Availability(String availabilityId, String hallId, LocalDateTime startDateTime,
            LocalDateTime endDateTime, String remarks, boolean isAvailable) {
        this.availabilityId.set(availabilityId);
        this.hallId.set(hallId);
        this.startDateTime.set(startDateTime);
        this.endDateTime.set(endDateTime);
        this.remarks.set(remarks);
        this.available.set(isAvailable);
    }

    /**
     * Default constructor
     */
    public Availability() {
    }

    /**
     * Convert the availability object to a delimited string for file storage.
     */
    public String toDelimitedString() {
        return String.join("|",
                getAvailabilityId(),
                getHallId(),
                getStartDateTime().format(DATE_TIME_FORMATTER),
                getEndDateTime().format(DATE_TIME_FORMATTER),
                getRemarks(),
                String.valueOf(isAvailable()));
    }

    /**
     * Create an Availability object from a delimited string.
     */
    public static Availability fromDelimitedString(String delimitedString) {
        String[] parts = delimitedString.split("\\|");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid availability string format");
        }

        return new Availability(
                parts[0], // availabilityId
                parts[1], // hallId
                LocalDateTime.parse(parts[2], DATE_TIME_FORMATTER), // startDateTime
                LocalDateTime.parse(parts[3], DATE_TIME_FORMATTER), // endDateTime
                parts[4], // remarks
                Boolean.parseBoolean(parts[5]) // isAvailable
        );
    }

    // Getters and setters
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

    public LocalDateTime getStartDateTime() {
        return startDateTime.get();
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime.set(startDateTime);
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime.get();
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime.set(endDateTime);
    }

    public String getRemarks() {
        return remarks.get();
    }

    public void setRemarks(String remarks) {
        this.remarks.set(remarks);
    }

    public boolean isAvailable() {
        return available.get();
    }

    public void setAvailable(boolean isAvailable) {
        this.available.set(isAvailable);
    }

    // JavaFX property getters
    public StringProperty availabilityIdProperty() {
        return availabilityId;
    }

    public StringProperty hallIdProperty() {
        return hallId;
    }

    public ObjectProperty<LocalDateTime> startDateTimeProperty() {
        return startDateTime;
    }

    public ObjectProperty<LocalDateTime> endDateTimeProperty() {
        return endDateTime;
    }

    public StringProperty remarksProperty() {
        return remarks;
    }

    public BooleanProperty availableProperty() {
        return available;
    }
}