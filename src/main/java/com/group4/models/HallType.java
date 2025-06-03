package com.group4.models;

/**
 * Enum representing different types of halls available in the system.
 */
public enum HallType {
    AUDITORIUM(1000),
    BANQUET(300),
    MEETING_ROOM(30);

    private final int maxCapacity;

    HallType(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }
}