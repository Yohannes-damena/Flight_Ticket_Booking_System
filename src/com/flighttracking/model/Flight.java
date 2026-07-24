package com.flighttracking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a flight entity in the Flight Ticket Booking System.
 * Stores flight metadata, total seat capacity, booked seats, and pricing.
 */
public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;

    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private String departureTime;
    private int totalSeats;
    private int bookedSeats;
    private double basePrice;

    public Flight() {
    }

    public Flight(String flightNumber, String airline, String origin, String destination, 
                  String departureTime, int totalSeats, double basePrice) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
        this.basePrice = basePrice;
    }

    public Flight(String flightNumber, String airline, String origin, String destination, 
                  String departureTime, int totalSeats, int bookedSeats, double basePrice) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.basePrice = basePrice;
    }

    /**
     * Calculates available seats remaining on this flight.
     * @return Number of available seats (totalSeats - bookedSeats)
     */
    public int getAvailableSeats() {
        return Math.max(0, totalSeats - bookedSeats);
    }

    /**
     * Checks if the flight is fully booked.
     * @return true if no seats remain available
     */
    public boolean isFull() {
        return getAvailableSeats() <= 0;
    }

    /**
     * Attempts to book a single seat on this flight.
     * @return true if seat booking succeeded, false if flight is full
     */
    public boolean bookSeat() {
        if (isFull()) {
            return false;
        }
        bookedSeats++;
        return true;
    }

    /**
     * Cancels a booked seat on this flight.
     * @return true if seat cancellation succeeded, false if no seats were booked
     */
    public boolean cancelSeat() {
        if (bookedSeats <= 0) {
            return false;
        }
        bookedSeats--;
        return true;
    }

    // Getters and Setters

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(flightNumber, flight.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightNumber);
    }

    @Override
    public String toString() {
        return String.format("%s (%s -> %s) [%d/%d seats available] - $%.2f",
                flightNumber, origin, destination, getAvailableSeats(), totalSeats, basePrice);
    }
}
