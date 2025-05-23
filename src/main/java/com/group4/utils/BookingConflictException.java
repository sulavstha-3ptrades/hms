package com.group4.utils;

/**
 * Exception thrown when there is a conflict with an existing booking.
 */
public class BookingConflictException extends Exception {

    /**
     * Constructs a new BookingConflictException with the specified detail message.
     * 
     * @param message the detail message
     */
    public BookingConflictException(String message) {
        super(message);
    }

    /**
     * Constructs a new BookingConflictException with the specified detail message
     * and cause.
     * 
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public BookingConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}