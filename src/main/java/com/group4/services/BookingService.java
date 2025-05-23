package com.group4.services;

import com.group4.models.Booking;
import com.group4.models.BookingStatus;
import com.group4.models.Hall;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;
import com.group4.utils.SessionManager;
import com.group4.utils.TaskUtils;
import javafx.concurrent.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for booking management operations.
 */
public class BookingService {

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
     * Gets bookings for the current user.
     * 
     * @return A Task that returns a list of bookings for the current user
     */
    public Task<List<Booking>> getMyBookings() {
        return new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                String userId = SessionManager.getInstance().getCurrentUser().getUserId();
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                List<Booking> bookings = new ArrayList<>();

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getCustomerId().equals(userId)) {
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
     */
    public Task<Booking> getBookingById(String bookingId) {
        return new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getBookingId().equals(bookingId)) {
                        return booking;
                    }
                }

                return null;
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
                    if (booking != null && booking.getHallId().equals(hallId) &&
                            booking.getStatus() != BookingStatus.CANCELED) {

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
     * @param hallId        The ID of the hall to book
     * @param startDateTime The start date and time
     * @param endDateTime   The end date and time
     * @return A Task that returns the created booking if successful, null otherwise
     */
    public Task<Booking> createBooking(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return new Task<Booking>() {
            @Override
            protected Booking call() throws Exception {
                // Check if the hall is available
                if (!TaskUtils.executeTask(isHallAvailable(hallId, startDateTime, endDateTime))) {
                    return null;
                }

                // Calculate the cost
                double totalCost = TaskUtils.executeTask(calculateBookingCost(hallId, startDateTime, endDateTime));

                // Generate a unique booking ID
                String bookingId = "BOOK-" + UUID.randomUUID().toString().substring(0, 8);

                // Get the current user ID
                String customerId = SessionManager.getInstance().getCurrentUser().getUserId();

                // Create a new booking
                Booking booking = new Booking(bookingId, customerId, hallId, startDateTime,
                        endDateTime, totalCost, BookingStatus.PENDING);

                // Save the booking to the file
                FileHandler.appendLine(FileConstants.BOOKINGS_FILE, booking.toDelimitedString());

                return booking;
            }
        };
    }

    /**
     * Updates the status of a booking.
     * 
     * @param bookingId The ID of the booking to update
     * @param status    The new status
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> updateBookingStatus(String bookingId, BookingStatus status) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                List<String> updatedLines = new ArrayList<>();
                boolean found = false;

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getBookingId().equals(bookingId)) {
                        booking.setStatus(status);
                        updatedLines.add(booking.toDelimitedString());
                        found = true;
                    } else {
                        updatedLines.add(line);
                    }
                }

                if (found) {
                    FileHandler.writeLines(FileConstants.BOOKINGS_FILE, updatedLines);
                    return true;
                }

                return false;
            }
        };
    }

    /**
     * Cancels a booking if it's more than 3 days before the start date.
     * 
     * @param bookingId The ID of the booking to cancel
     * @return A Task that returns true if successful, false otherwise
     */
    public Task<Boolean> cancelBooking(String bookingId) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Get the booking
                Booking booking = TaskUtils.executeTask(getBookingById(bookingId));
                if (booking == null) {
                    return false;
                }

                // Check if it's more than 3 days before the start date
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cancellationDeadline = booking.getStartDateTime().minusDays(3);

                if (now.isAfter(cancellationDeadline)) {
                    return false;
                }

                // Update the booking status
                return TaskUtils.executeTask(updateBookingStatus(bookingId, BookingStatus.CANCELED));
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
     * @return A Task that returns a list of bookings for the specified customer
     */
    public Task<List<Booking>> getBookingsByCustomerId(String customerId) {
        return new Task<List<Booking>>() {
            @Override
            protected List<Booking> call() throws Exception {
                List<String> lines = FileHandler.readLines(FileConstants.BOOKINGS_FILE);
                List<Booking> bookings = new ArrayList<>();

                for (String line : lines) {
                    Booking booking = Booking.fromDelimitedString(line);
                    if (booking != null && booking.getCustomerId().equals(customerId)) {
                        bookings.add(booking);
                    }
                }

                return bookings;
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