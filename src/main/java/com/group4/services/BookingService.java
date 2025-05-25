package com.group4.services;

import com.group4.models.Booking;
import com.group4.models.Hall;
import com.group4.models.User;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the booking service interface.
 * Handles all booking-related operations including creation, cancellation, and querying.
 */
public class BookingService implements IBookingService {
    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());
    private static final int CANCELLATION_DEADLINE_DAYS = 3;

    private final HallService hallService = new HallService();

    /**
     * Gets all bookings in the system.
     * 
     * @return A Task that returns a list of all bookings
     */
    public Task<List<Booking>> getAllBookings() {
        return new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                List<Booking> bookings = new ArrayList<>();

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null) {
                        bookings.add(booking);
                    }
                }

                return bookings;
            }
        };
    }


    /**
     * Gets a booking by its ID.
     * 
     * @param bookingId The ID of the booking to get
     * @return A Task that returns the booking if found, null otherwise
     * @throws IllegalArgumentException if bookingId is null or empty
     */
    @Override
    public Task<Booking> getBookingById(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        
        final String finalBookingId = bookingId.trim();
        
        return new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                try {
                    LOGGER.info("Retrieving booking with ID: " + finalBookingId);
                    
                    List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                    
                    for (String line : lines) {
                        if (isCancelled()) {
                            LOGGER.info("Booking retrieval was cancelled");
                            return null;
                        }
                        
                        try {
                            Booking booking = Booking.fromDelimitedString(line);
                            if (booking != null && finalBookingId.equals(booking.getBookingId())) {
                                LOGGER.info("Successfully retrieved booking: " + finalBookingId);
                                return booking;
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error parsing booking: " + line, e);
                            // Continue with next line if one booking fails to parse
                        }
                    }
                    
                    LOGGER.warning("Booking not found: " + finalBookingId);
                    return null;
                    
                } catch (Exception e) {
                    String errorMsg = "Failed to retrieve booking: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, errorMsg, e);
                    throw new RuntimeException(errorMsg, e);
                }
            }
            
            @Override
            protected void failed() {
                super.failed();
                LOGGER.log(Level.SEVERE, "Failed to retrieve booking: " + finalBookingId, getException());
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue() != null) {
                    LOGGER.info("Successfully retrieved booking: " + finalBookingId);
                } else {
                    LOGGER.warning("Booking not found: " + finalBookingId);
                }
            }
        };
    }

    /**
     * Cancels a booking with the specified booking ID.
     *
     * @param bookingId The ID of the booking to cancel
     * @return A Task that returns true if cancellation was successful, false otherwise
     * @throws IllegalArgumentException if bookingId is null or empty
     */
    @Override
    public Task<Boolean> cancelBooking(String bookingId) {
        if (bookingId == null || bookingId.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID cannot be null or empty");
        }
        
        // Make effectively final for use in inner class
        final String finalBookingId = bookingId.trim();

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    LOGGER.info("Attempting to cancel booking: " + finalBookingId);
                    
                    // Read all bookings
                    List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                    List<String> updatedLines = new ArrayList<>();
                    boolean found = false;
                    
                    // Find and update the booking
                    for (String line : lines) {
                        if (isCancelled()) {
                            LOGGER.info("Booking cancellation was cancelled");
                            return false;
                        }
                        
                        Booking booking = Booking.fromDelimitedString(line);
                        if (booking != null && bookingId.equals(booking.getBookingId())) {
                            // Check if booking can be cancelled (at least 3 days before start)
                            LocalDateTime now = LocalDateTime.now();
                            LocalDateTime cancellationDeadline = booking.getStartDateTime().minusDays(CANCELLATION_DEADLINE_DAYS);
                            
                            if (now.isAfter(cancellationDeadline)) {
                                LOGGER.warning("Cannot cancel booking " + finalBookingId + ": Cancellation deadline has passed");
                                return false;
                            }
                            
                            // Remove the booking
                            found = true;
                            LOGGER.info("Booking " + finalBookingId + " removed");
                        } else {
                            updatedLines.add(line);
                        }
                    }
                    
                    if (!found) {
                        LOGGER.warning("Booking not found: " + finalBookingId);
                        return false;
                    }
                    
                    // Write updated bookings back to file
                    FileHandler.writeLines(FileConstants.BOOKINGS_FILE, updatedLines);
                    LOGGER.info("Successfully cancelled booking: " + finalBookingId);
                    return true;
                    
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error cancelling booking: " + finalBookingId, e);
                    throw new RuntimeException("Failed to cancel booking: " + e.getMessage(), e);
                }
            }
            
            @Override
            protected void failed() {
                super.failed();
                LOGGER.log(Level.SEVERE, "Failed to cancel booking: " + finalBookingId, getException());
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue()) {
                    LOGGER.info("Booking cancellation successful: " + finalBookingId);
                } else {
                    LOGGER.warning("Booking cancellation failed or not allowed: " + finalBookingId);
                }
            }
        };
    }

    /**
     * Checks if a hall is available for booking during the specified time period.
     * 
     * @param hallId        The ID of the hall to check
     * @param startDateTime The start date and time
     * @param endDateTime   The end date and time
     * @return A Task that returns true if the hall is available, false otherwise
     */
    public Task<Boolean> isHallAvailable(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Check if the requested time is within business hours (8 AM - 6 PM)
                LocalTime startTime = startDateTime.toLocalTime();
                LocalTime endTime = endDateTime.toLocalTime();
                LocalTime businessStart = LocalTime.of(8, 0);
                LocalTime businessEnd = LocalTime.of(18, 0);

                if (startTime.isBefore(businessStart) || endTime.isAfter(businessEnd)) {
                    return false;
                }

                // Check for overlapping bookings
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getHallId().equals(hallId)) {

                        // Check for overlap
                        if (!(endDateTime.isBefore(booking.getStartDateTime()) ||
                                startDateTime.isAfter(booking.getEndDateTime()))) {
                            return false;
                        }
                    }
                }

                return true;
            }
        };
    }

    /**
     * Calculates the cost of a booking.
     * 
     * @param hallId        The ID of the hall
     * @param startDateTime The start date and time
     * @param endDateTime   The end date and time
     * @return A Task that returns the calculated cost
     */
    public Task<Double> calculateBookingCost(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new Task<Double>() {
            @Override
            protected Double call() throws Exception {
                // Get the hall
                Hall hall = TaskUtils.executeTask(hallService.getHallById(hallId));
                if (hall == null) {
                    return 0.0;
                }

                // Calculate the duration in hours
                double hours = Duration.between(startDateTime, endDateTime).toMinutes() / 60.0;

                // Calculate the cost
                return hall.getRatePerHour() * hours;
            }
        };
    }

    /**
     * Creates a new booking.
     * 
     * @param hallId        The ID of the hall to book (must not be null or empty)
     * @param startDateTime The start date and time (must not be null and must be in the future)
     * @param endDateTime   The end date and time (must not be null and must be after startDateTime)
     * @return A Task that returns the created booking if successful
     * @throws IllegalArgumentException if any parameter is invalid
     * @throws IllegalStateException if the hall is not available or user is not logged in
     */
    @Override
    public Task<Booking> createBooking(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Input validation
        if (hallId == null || hallId.trim().isEmpty()) {
            throw new IllegalArgumentException("Hall ID cannot be null or empty");
        }
        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start time cannot be in the past");
        }
        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        final String finalHallId = hallId.trim();
        
        return new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                try {
                    LOGGER.info(String.format("Creating new booking for hall %s from %s to %s", 
                            finalHallId, startDateTime, endDateTime));
                    
                    // Check if the hall is available
                    if (!TaskUtils.executeTask(isHallAvailable(finalHallId, startDateTime, endDateTime))) {
                        String errorMsg = String.format("Hall %s is not available for the selected time", finalHallId);
                        LOGGER.warning(errorMsg);
                        throw new IllegalStateException(errorMsg);
                    }

                    // Get current user
                    User currentUser = SessionManager.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        String errorMsg = "User must be logged in to create a booking";
                        LOGGER.warning(errorMsg);
                        throw new IllegalStateException(errorMsg);
                    }

                    // Calculate the cost
                    double totalCost = TaskUtils.executeTask(calculateBookingCost(finalHallId, startDateTime, endDateTime));
                    LOGGER.info(String.format("Calculated total cost: $%.2f", totalCost));

                    // Generate a unique booking ID
                    String bookingId = UUID.randomUUID().toString();
                    LOGGER.info("Generated booking ID: " + bookingId);

                    // Create a new booking
                    Booking newBooking = new Booking(
                        bookingId,
                        currentUser.getUserId(),
                        finalHallId,
                        startDateTime,
                        endDateTime,
                        totalCost
                    );

                    // Save the booking to the file
                    String bookingLine = newBooking.toDelimitedString();
                    FileHandler.appendLine(FileConstants.BOOKINGS_FILE, bookingLine);
                    LOGGER.info("Successfully created booking: " + bookingId);

                    return newBooking;
                    
                } catch (Exception e) {
                    String errorMsg = "Failed to create booking: " + e.getMessage();
                    LOGGER.log(Level.SEVERE, errorMsg, e);
                    throw new RuntimeException(errorMsg, e);
                }
            }
            
            @Override
            protected void failed() {
                super.failed();
                LOGGER.log(Level.SEVERE, "Booking creation failed", getException());
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                LOGGER.info("Booking created successfully: " + getValue().getBookingId());
            }
        };
    }

    /**
     * Gets bookings for a specific hall.
     * 
     * @param hallId The ID of the hall
     * @return A Task that returns a list of bookings for the specified hall
     */
    public Task<List<Booking>> getBookingsByHallId(String hallId) {
        return new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                List<Booking> bookings = new ArrayList<>();

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getHallId().equals(hallId)) {
                        bookings.add(booking);
                    }
                }

                return bookings;
            }
        };
    }

    /**
     * Gets bookings for a specific customer.
     *
     * @param customerId The ID of the customer
     * @return A Task that returns an ObservableList of bookings for the specified customer
     * @throws IllegalArgumentException if customerId is null or empty
     */
    @Override
    public Task<ObservableList<Booking>> getBookingsByCustomerId(String customerId) {
        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID cannot be null or empty");
        }

        return new Task<ObservableList<Booking>>() {
            @Override
            protected ObservableList<Booking> call() throws Exception {
                ObservableList<Booking> bookings = FXCollections.observableArrayList();
                
                try {
                    List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                    
                    for (String line : lines) {
                        if (isCancelled()) {
                            break;
                        }
                        
                        try {
                            Booking booking = Booking.fromDelimitedString(line);
                            if (booking != null && customerId.equals(booking.getCustomerId())) {
                                bookings.add(booking);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.WARNING, "Error parsing booking: " + line, e);
                            // Continue with next line if one booking fails to parse
                        }
                        
                        updateProgress(bookings.size(), lines.size());
                    }
                    
                    return bookings;
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Error reading bookings file", e);
                    throw new RuntimeException("Failed to load bookings: " + e.getMessage(), e);
                }
            }
            
            @Override
            protected void failed() {
                super.failed();
                LOGGER.log(Level.SEVERE, "Failed to load bookings", getException());
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                LOGGER.info("Successfully loaded " + getValue().size() + " bookings for customer " + customerId);
            }
        };
    }

    /**
     * Calculates the total cost for a booking based on hall rate and duration.
     * 
     * @param hall          The hall being booked
     * @param startDateTime The start date and time of the booking
     * @param endDateTime   The end date and time of the booking
     * @return The total cost, or 0 if the time range is invalid
     */
    public double calculateTotalCost(Hall hall, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Check for invalid time range
        if (startDateTime == null || endDateTime == null || !startDateTime.isBefore(endDateTime)) {
            return 0.0;
        }

        // Calculate duration in hours
        Duration duration = Duration.between(startDateTime, endDateTime);
        long hours = duration.toHours();

        // Calculate total cost
        return hall.getRatePerHour() * hours;
    }
}