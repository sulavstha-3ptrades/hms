package com.group4.utils;

/**
 * Exception thrown when user input is invalid.
 */
public class InvalidInputException extends Exception {

    /**
     * Constructs a new InvalidInputException with the specified detail message.
     * 
     * @param message the detail message
     */
    public InvalidInputException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidInputException with the specified detail message and
     * cause.
     * 
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}