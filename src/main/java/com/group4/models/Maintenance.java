package com.group4.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a maintenance schedule for a hall.
 */
public class Maintenance {
    private final StringProperty maintenanceId = new SimpleStringProperty(this, "maintenanceId", "");
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final ObjectProperty<LocalDateTime> startTime = new SimpleObjectProperty<>(this, "startTime", null);
    private final ObjectProperty<LocalDateTime> endTime = new SimpleObjectProperty<>(this, "endTime", null);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Default constructor
     */
    public Maintenance() {
    }

    /**
     * Parameterized constructor
     */
    public Maintenance(String maintenanceId, String hallId, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.maintenanceId.set(maintenanceId);
        this.hallId.set(hallId);
        this.description.set(description);
        this.startTime.set(startTime);
        this.endTime.set(endTime);
    }

    // Getters and Setters
    public String getMaintenanceId() {
        return maintenanceId.get();
    }

    public void setMaintenanceId(String maintenanceId) {
        this.maintenanceId.set(maintenanceId);
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

    // JavaFX property getters
    public StringProperty maintenanceIdProperty() {
        return maintenanceId;
    }

    public StringProperty hallIdProperty() {
        return hallId;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<LocalDateTime> startTimeProperty() {
        return startTime;
    }

    public ObjectProperty<LocalDateTime> endTimeProperty() {
        return endTime;
    }

    /**
     * Converts maintenance object to a delimited string representation
     * 
     * @return String representation of the maintenance
     */
    public String toDelimitedString() {
        return getMaintenanceId() + "|" + 
               getHallId() + "|" + 
               getDescription() + "|" + 
               getStartTime().format(formatter) + "|" + 
               getEndTime().format(formatter);
    }

    /**
     * Creates a Maintenance object from a delimited string
     * 
     * @param data The delimited string containing maintenance data
     * @return Maintenance object
     */
    public static Maintenance fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 5) {
            return new Maintenance(
                    parts[0], // maintenanceId
                    parts[1], // hallId
                    parts[2], // description
                    LocalDateTime.parse(parts[3], formatter), // startTime
                    LocalDateTime.parse(parts[4], formatter)  // endTime
            );
        }
        return null;
    }
}
