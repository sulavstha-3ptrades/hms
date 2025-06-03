package com.group4.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*;

/**
 * Represents a hall booking in the Hall Management System.
 */
public class Booking {
    private final StringProperty bookingId = new SimpleStringProperty(this, "bookingId", "");
    private final StringProperty customerId = new SimpleStringProperty(this, "customerId", "");
    private final StringProperty hallId = new SimpleStringProperty(this, "hallId", "");
    private final ObjectProperty<LocalDateTime> startDateTime = new SimpleObjectProperty<>(this, "startDateTime", null);
    private final ObjectProperty<LocalDateTime> endDateTime = new SimpleObjectProperty<>(this, "endDateTime", null);
    private final DoubleProperty totalCost = new SimpleDoubleProperty(this, "totalCost", 0.0);
    private final ObjectProperty<BookingStatus> bookingStatus = new SimpleObjectProperty<>(this, "bookingStatus", null);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Default constructor
     */
    public Booking() {
    }

    /**
     * Parameterized constructor
     */
    public Booking(String bookingId, String customerId, String hallId,
            LocalDateTime startDateTime, LocalDateTime endDateTime,
            double totalCost, BookingStatus bookingStatus) {
        this.bookingId.set(bookingId);
        this.customerId.set(customerId);
        this.hallId.set(hallId);
        this.startDateTime.set(startDateTime);
        this.endDateTime.set(endDateTime);
        this.totalCost.set(totalCost);
        this.bookingStatus.set(bookingStatus);
    }

    // Getters and Setters
    public String getBookingId() {
        return bookingId.get();
    }

    public void setBookingId(String bookingId) {
        this.bookingId.set(bookingId);
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

    public double getTotalCost() {
        return totalCost.get();
    }

    public void setTotalCost(double totalCost) {
        this.totalCost.set(totalCost);
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus.get();
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus.set(bookingStatus);
    }

    // JavaFX property getters
    public StringProperty bookingIdProperty() {
        return bookingId;
    }

    public StringProperty customerIdProperty() {
        return customerId;
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

    public DoubleProperty totalCostProperty() {
        return totalCost;
    }

    public ObjectProperty<BookingStatus> bookingStatusProperty() {
        return bookingStatus;
    }

    /**
     * Converts booking object to a delimited string representation
     * 
     * @return String representation of the booking
     */
    public String toDelimitedString() {
        return getBookingId() + "|" + getCustomerId() + "|" + getHallId() + "|" +
                getStartDateTime().format(DATE_TIME_FORMATTER) + "|" +
                getEndDateTime().format(DATE_TIME_FORMATTER) + "|" +
                getTotalCost() + "|" + getBookingStatus().name();
    }

    /**
     * Creates a Booking object from a delimited string
     * 
     * @param data The delimited string containing booking data
     * @return Booking object
     */
    public static Booking fromDelimitedString(String data) {
        String[] parts = data.split("\\|");
        if (parts.length == 6) {
            // Assume BOOKED status for old data without status field
            return new Booking(
                    parts[0], // bookingId
                    parts[1], // customerId
                    parts[2], // hallId
                    LocalDateTime.parse(parts[3], DATE_TIME_FORMATTER), // startDateTime
                    LocalDateTime.parse(parts[4], DATE_TIME_FORMATTER), // endDateTime
                    Double.parseDouble(parts[5]), // totalCost
                    BookingStatus.BOOKED // bookingStatus
            );
        } else if (parts.length == 7) {
            return new Booking(
                    parts[0], // bookingId
                    parts[1], // customerId
                    parts[2], // hallId
                    LocalDateTime.parse(parts[3], DATE_TIME_FORMATTER), // startDateTime
                    LocalDateTime.parse(parts[4], DATE_TIME_FORMATTER), // endDateTime
                    Double.parseDouble(parts[5]), // totalCost
                    BookingStatus.valueOf(parts[6]) // bookingStatus
            );
        }
        return null;
    }
}
