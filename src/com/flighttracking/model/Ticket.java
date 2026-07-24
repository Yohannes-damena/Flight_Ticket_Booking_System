package com.flighttracking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Abstract base class representing a flight ticket definition.
 * Serves as the base for specific ticket subclasses (EconomyTicket, BusinessTicket).
 */
public abstract class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;

    private String ticketId;
    private Passenger passenger;
    private Flight flight;
    private String bookingDate;
    private String seatNumber;

    public Ticket() {
    }

    public Ticket(String ticketId, Passenger passenger, Flight flight, String bookingDate, String seatNumber) {
        this.ticketId = ticketId;
        this.passenger = passenger;
        this.flight = flight;
        this.bookingDate = bookingDate;
        this.seatNumber = seatNumber;
    }

    /**
     * Calculates the total price of the ticket based on tier rules, base flight price, and add-ons.
     * @return Final total ticket price
     */
    public abstract double calculateTotalPrice();

    /**
     * Returns the human-readable tier name of this ticket subclass (e.g. "Economy", "Business").
     * @return Ticket tier name string
     */
    public abstract String getTierName();

    // Getters and Setters

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(ticketId, ticket.ticketId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ticketId);
    }

    @Override
    public String toString() {
        return String.format("%s Ticket [%s] - Flight %s (%s) - Passenger: %s - Seat: %s - Total: $%.2f",
                getTierName(), ticketId, flight.getFlightNumber(), flight.getOrigin() + "->" + flight.getDestination(),
                passenger.getFullName(), seatNumber, calculateTotalPrice());
    }
}
