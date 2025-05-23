package com.group4.models;

import javafx.beans.property.*;

/**
 * Represents a hall in the Hall Management System.
 */
public class Hall {
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final ObjectProperty<HallType> type = new SimpleObjectProperty<>(this, "type", null);
    private final IntegerProperty capacity = new SimpleIntegerProperty(this, "capacity", 0);
    private final DoubleProperty ratePerHour = new SimpleDoubleProperty(this, "ratePerHour", 0.0);

    /**
     * Default constructor
     */
    public Hall() {
    }

    /**
     * Parameterized constructor
     */
    public Hall(String hallId, HallType type, int capacity, double ratePerHour) {
        this.hallId.set(hallId);
        this.type.set(type);
        this.capacity.set(capacity);
        this.ratePerHour.set(ratePerHour);
    }

    // Getters and Setters
    public String getHallId() {
        return hallId.get();
    }

    public void setHallId(String hallId) {
        this.hallId.set(hallId);
    }

    public HallType getType() {
        return type.get();
    }

    public void setType(HallType type) {
        this.type.set(type);
    }

    public int getCapacity() {
        return capacity.get();
    }

    public void setCapacity(int capacity) {
        this.capacity.set(capacity);
    }

    public double getRatePerHour() {
        return ratePerHour.get();
    }

    public void setRatePerHour(double ratePerHour) {
        this.ratePerHour.set(ratePerHour);
    }

    // JavaFX property getters
    public StringProperty hallIdProperty() {
        return hallId;
    }

    public ObjectProperty<HallType> typeProperty() {
        return type;
    }

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public DoubleProperty ratePerHourProperty() {
        return ratePerHour;
    }

    /**
     * Converts hall object to a delimited string representation
     * 
     * @return String representation of the hall
     */
    public String toDelimitedString() {
        return getHallId() + "|" + getType().name() + "|" + getCapacity() + "|" + getRatePerHour();
    }

    /**
     * Creates a Hall object from a delimited string
     * 
     * @param data The delimited string containing hall data
     * @return Hall object
     */
    public static Hall fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length == 4) {
            return new Hall(
                    parts[0], // hallId
                    HallType.valueOf(parts[1]), // type
                    Integer.parseInt(parts[2]), // capacity
                    Double.parseDouble(parts[3]) // ratePerHour
            );
        }
        return null;
    }
}