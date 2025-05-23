package com.group4;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.group4.models.Hall;
import com.group4.models.HallType;
import com.group4.services.BookingService;

/**
 * Tests for the booking cost calculation logic.
 */
public class BookingCalculatorTest {

    private BookingService bookingService;
    private Hall testHall;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @BeforeEach
    public void setup() {
        bookingService = new BookingService();

        // Create a test hall
        testHall = new Hall("H001", HallType.AUDITORIUM, 200, 100.0);
    }

    @Test
    public void testCalculateCostForOneHour() {
        // Set up a one-hour booking
        LocalDateTime startDateTime = LocalDateTime.parse("2023-05-25T10:00:00", DATE_TIME_FORMATTER);
        LocalDateTime endDateTime = LocalDateTime.parse("2023-05-25T11:00:00", DATE_TIME_FORMATTER);

        // Calculate cost
        double cost = bookingService.calculateTotalCost(testHall, startDateTime, endDateTime);

        // Assert the cost is correct for one hour at rate 100.0
        assertEquals(100.0, cost, "Cost should be equal to the rate for a one hour booking");
    }

    @Test
    public void testCalculateCostForMultipleHours() {
        // Set up a three-hour booking
        LocalDateTime startDateTime = LocalDateTime.parse("2023-05-25T10:00:00", DATE_TIME_FORMATTER);
        LocalDateTime endDateTime = LocalDateTime.parse("2023-05-25T13:00:00", DATE_TIME_FORMATTER);

        // Calculate cost
        double cost = bookingService.calculateTotalCost(testHall, startDateTime, endDateTime);

        // Assert the cost is correct for three hours at rate 100.0
        assertEquals(300.0, cost, "Cost should be 3 times the rate for a three hour booking");
    }

    @Test
    public void testCalculateCostForZeroHours() {
        // Set up a zero-hour booking (same start and end time)
        LocalDateTime sameDateTime = LocalDateTime.parse("2023-05-25T10:00:00", DATE_TIME_FORMATTER);

        // Calculate cost
        double cost = bookingService.calculateTotalCost(testHall, sameDateTime, sameDateTime);

        // Assert the cost is zero
        assertEquals(0.0, cost, "Cost should be zero for a zero hour booking");
    }

    @Test
    public void testCalculateCostForInvalidTimeRange() {
        // Set up a booking with end time before start time
        LocalDateTime startDateTime = LocalDateTime.parse("2023-05-25T11:00:00", DATE_TIME_FORMATTER);
        LocalDateTime endDateTime = LocalDateTime.parse("2023-05-25T10:00:00", DATE_TIME_FORMATTER);

        // Calculate cost
        double cost = bookingService.calculateTotalCost(testHall, startDateTime, endDateTime);

        // Assert the cost is zero for invalid time range
        assertEquals(0.0, cost, "Cost should be zero for an invalid time range");
    }
}