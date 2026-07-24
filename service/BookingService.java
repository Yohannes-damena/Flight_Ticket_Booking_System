package com.flighttracking.service;

import com.flighttracking.exception.FlightOverbookedException;
import com.flighttracking.model.*;
import com.flighttracking.repository.DataContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * BookingService is the central business logic engine of the Flight Ticket Booking System.
 *
 * Responsibilities:
 * - Validates booking preconditions (seat availability, input integrity)
 * - Creates Economy or Business tickets based on the requested tier
 * - Cancels existing bookings and restores flight seat counts
 * - Provides search and query methods consumed by the UI layer
 * - Delegates all data storage to DataContext
 */
public class BookingService {

    private final DataContext dataContext;

    // Seat number generation state
    private final Map<String, Integer> seatCounterMap = new HashMap<>();

    // Tier constants
    public static final String TIER_ECONOMY  = "Economy";
    public static final String TIER_BUSINESS = "Business";

    // Ticket ID prefix
    private static final String TICKET_PREFIX = "TKT";

    // ── Constructor ────────────────────────────────────────────────────────────

    public BookingService(DataContext dataContext) {
        this.dataContext = dataContext;
    }

    // ── Core Booking Logic ─────────────────────────────────────────────────────

    /**
     * Books a flight ticket for the given passenger and flight.
     *
     * Steps:
     * 1. Validates inputs are non-null and tier is valid.
     * 2. Checks if the flight has available seats; throws FlightOverbookedException if not.
     * 3. Increments the flight's booked seat count via bookSeat().
     * 4. Generates a unique ticketId and seat number.
     * 5. Instantiates the correct Ticket subclass based on requested tier.
     * 6. Persists everything to disk via DataContext.saveToDisk().
     *
     * @param passenger         The passenger making the booking
     * @param flight            The flight to book
     * @param tier              Ticket class tier: "Economy" or "Business"
     * @param extraBaggageCount Number of extra bags (Economy only; ignored for Business)
     * @param loungeAccess      Whether lounge access is requested (Business only; ignored for Economy)
     * @return The newly created and persisted Ticket object
     * @throws FlightOverbookedException if flight.getAvailableSeats() <= 0
     * @throws IllegalArgumentException  if any required parameter is null or tier is invalid
     */
    public Ticket bookFlight(Passenger passenger, Flight flight, String tier,
                             int extraBaggageCount, boolean loungeAccess)
            throws FlightOverbookedException {

        // ── Guard: Null checks ─────────────────────────────────────────────────
        if (passenger == null) throw new IllegalArgumentException("Passenger cannot be null.");
        if (flight    == null) throw new IllegalArgumentException("Flight cannot be null.");
        if (tier      == null || tier.isBlank()) throw new IllegalArgumentException("Tier cannot be blank.");
        if (!tier.equalsIgnoreCase(TIER_ECONOMY) && !tier.equalsIgnoreCase(TIER_BUSINESS)) {
            throw new IllegalArgumentException("Invalid ticket tier: '" + tier + "'. Must be 'Economy' or 'Business'.");
        }

        // ── Guard: Availability check ──────────────────────────────────────────
        if (flight.isFull()) {
            throw new FlightOverbookedException(flight.getFlightNumber(), flight.getTotalSeats());
        }

        // ── Book the seat on the flight entity ─────────────────────────────────
        flight.bookSeat();

        // ── Generate unique IDs ────────────────────────────────────────────────
        String ticketId   = generateTicketId();
        String seatNumber = generateSeatNumber(flight.getFlightNumber(), tier);
        String bookingDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // ── Instantiate the appropriate Ticket subclass ────────────────────────
        Ticket ticket;
        if (tier.equalsIgnoreCase(TIER_BUSINESS)) {
            ticket = new BusinessTicket(ticketId, passenger, flight, bookingDate, seatNumber, loungeAccess);
        } else {
            ticket = new EconomyTicket(ticketId, passenger, flight, bookingDate, seatNumber, extraBaggageCount);
        }

        // ── Persist to DataContext & disk ──────────────────────────────────────
        dataContext.addTicket(ticket);
        dataContext.saveToDisk();

        System.out.printf("[BookingService] Booked: %s | Passenger: %s | Flight: %s | Seat: %s | Total: ETB %.2f%n",
                ticketId, passenger.getFullName(), flight.getFlightNumber(), seatNumber, ticket.calculateTotalPrice());

        return ticket;
    }

    // ── Cancellation Logic ─────────────────────────────────────────────────────

    /**
     * Cancels an existing booking by ticket ID.
     *
     * Steps:
     * 1. Looks up the ticket in DataContext.
     * 2. Retrieves the associated flight and calls cancelSeat() to free the seat.
     * 3. Removes the ticket from storage.
     * 4. Persists the updated state to disk.
     *
     * @param ticketId The unique ID of the ticket to cancel
     * @return The cancelled Ticket object
     * @throws IllegalArgumentException if no ticket with the given ID exists
     */
    public Ticket cancelBooking(String ticketId) {
        if (ticketId == null || ticketId.isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be blank.");
        }

        Ticket ticket = dataContext.getTicketById(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("No ticket found with ID: " + ticketId);
        }

        Flight flight = ticket.getFlight();
        if (flight != null) {
            flight.cancelSeat();
        }

        dataContext.removeTicket(ticketId);
        dataContext.saveToDisk();

        System.out.printf("[BookingService] Cancelled ticket: %s | Passenger: %s | Flight: %s%n",
                ticketId, ticket.getPassenger().getFullName(), flight != null ? flight.getFlightNumber() : "N/A");

        return ticket;
    }

    // ── Query & Search Methods (used by UI layer) ──────────────────────────────

    public List<Flight> getAllFlights() {
        return dataContext.getAllFlights();
    }

    public List<Ticket> getAllTickets() {
        return dataContext.getAllTicketsList();
    }

    public Ticket findTicketById(String ticketId) {
        return dataContext.getTicketById(ticketId);
    }

    public List<Flight> searchFlights(String keyword) {
        if (keyword == null || keyword.isBlank()) return dataContext.getAllFlights();
        String lower = keyword.toLowerCase().trim();
        return dataContext.getAllFlights().stream()
                .filter(f ->
                    f.getFlightNumber().toLowerCase().contains(lower) ||
                    f.getOrigin().toLowerCase().contains(lower)       ||
                    f.getDestination().toLowerCase().contains(lower)  ||
                    f.getAirline().toLowerCase().contains(lower)
                )
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByPassengerName(String passengerName) {
        if (passengerName == null || passengerName.isBlank()) return Collections.emptyList();
        String lower = passengerName.toLowerCase().trim();
        return dataContext.getAllTicketsList().stream()
                .filter(t -> t.getPassenger().getFullName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsByFlight(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) return Collections.emptyList();
        return dataContext.getAllTicketsList().stream()
                .filter(t -> t.getFlight().getFlightNumber().equalsIgnoreCase(flightNumber))
                .collect(Collectors.toList());
    }

    // ── Statistics ─────────────────────────────────────────────────────────────

    public double getTotalRevenue() {
        return dataContext.getAllTicketsList().stream()
                .mapToDouble(Ticket::calculateTotalPrice)
                .sum();
    }

    public int getTotalBookedSeats() {
        return dataContext.getAllFlights().stream()
                .mapToInt(Flight::getBookedSeats)
                .sum();
    }

    public int getTotalAvailableSeats() {
        return dataContext.getAllFlights().stream()
                .mapToInt(Flight::getAvailableSeats)
                .sum();
    }

    // ── Helper: ID & Seat Generation ──────────────────────────────────────────

    private String generateTicketId() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String random    = String.format("%04d", new Random().nextInt(10000));
        return TICKET_PREFIX + "-" + timestamp + "-" + random;
    }

    private String generateSeatNumber(String flightNumber, String tier) {
        String key = flightNumber + "-" + tier;
        int index = seatCounterMap.getOrDefault(key, 0) + 1;
        seatCounterMap.put(key, index);
        int startRow = tier.equalsIgnoreCase(TIER_BUSINESS) ? 1 : 21;
        int row      = startRow + (index - 1) / 6;
        char col     = (char) ('A' + (index - 1) % 6);
        return row + "" + col;
    }

    public DataContext getDataContext() {
        return dataContext;
    }
}
