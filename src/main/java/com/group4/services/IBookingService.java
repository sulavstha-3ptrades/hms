package com.group4.services;

import com.group4.models.Booking;
import java.time.LocalDateTime;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * Service interface for booking-related operations.
 */
public interface IBookingService {
    /**
     * Retrieves all bookings for a specific customer.
     *
     * @param customerId the ID of the customer
     * @return Task containing the list of bookings
     */
    Task<ObservableList<Booking>> getBookingsByCustomerId(String customerId);

    /**
     * Cancels a booking.
     *
     * @param bookingId the ID of the booking to cancel
     * @return Task containing true if cancellation was successful
     */
    Task<Boolean> cancelBooking(String bookingId);

    /**
     * Gets booking details by ID.
     *
     * @param bookingId the ID of the booking
     * @return Task containing the booking details
     */
    Task<Booking> getBookingById(String bookingId);

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
    Task<Booking> createBooking(String hallId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
