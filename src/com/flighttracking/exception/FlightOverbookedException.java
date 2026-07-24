package com.flighttracking.exception;

/**
 * Thrown when a booking is attempted on a flight that has no available seats.
 */
public class FlightOverbookedException extends Exception {
    private static final long serialVersionUID = 1L;

    private final String flightNumber;
    private final int totalSeats;

    public FlightOverbookedException(String flightNumber, int totalSeats) {
        super(String.format(
            "Flight '%s' is fully booked. All %d seats are taken. No further bookings can be accepted.",
            flightNumber, totalSeats
        ));
        this.flightNumber = flightNumber;
        this.totalSeats = totalSeats;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public int getTotalSeats() {
        return totalSeats;
    }
}
