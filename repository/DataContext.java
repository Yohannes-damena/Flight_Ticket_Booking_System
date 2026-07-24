package com.flighttracking.repository;

import com.flighttracking.model.Flight;
import com.flighttracking.model.Ticket;

import java.io.*;
import java.util.*;

/**
 * DataContext serves as the in-memory "database" for the Flight Ticket Booking System.
 *
 * Responsibilities:
 * - Stores all Flight records in an ArrayList<Flight>
 * - Stores all issued Tickets in a HashMap<String, Ticket> keyed by ticketId
 * - Provides saveToDisk() and loadFromDisk() for flat-file binary persistence (.dat files)
 * - Seeds default sample flight data when no persisted data is found on startup
 */
public class DataContext {

    // ── In-Memory Storage ──────────────────────────────────────────────────────

    /** Live list of all flights registered in the system */
    private List<Flight> flights;

    /** Live map of all issued tickets, keyed by ticketId for O(1) lookup */
    private Map<String, Ticket> ticketsMap;

    // ── File Persistence Paths ─────────────────────────────────────────────────

    /** Binary file storing serialized flight records */
    public static final String FLIGHTS_FILE = "data/flights.dat";

    /** Binary file storing serialized ticket records */
    public static final String TICKETS_FILE = "data/tickets.dat";

    // ── Constructor ────────────────────────────────────────────────────────────

    /**
     * Initializes DataContext by attempting to load persisted data from disk.
     * If no data files are found, seeds the system with sample flight data.
     */
    public DataContext() {
        this.flights = new ArrayList<>();
        this.ticketsMap = new HashMap<>();

        // Ensure the data directory exists
        new File("data").mkdirs();

        // Attempt to restore persisted data; fall back to seed data if none found
        boolean flightsLoaded = loadFlightsFromDisk();
        boolean ticketsLoaded = loadTicketsFromDisk();

        if (!flightsLoaded) {
            System.out.println("[DataContext] No persisted flight data found. Seeding sample data...");
            seedSampleData();
            saveFlightsToDisk();
        }

        if (!ticketsLoaded) {
            System.out.println("[DataContext] No persisted ticket data found. Starting with empty ticket store.");
            saveTicketsToDisk();
        }
    }

    // ── Persistence: Save to Disk ──────────────────────────────────────────────

    /**
     * Serializes the flights list to a binary .dat file using ObjectOutputStream.
     * @return true if save succeeded, false on I/O failure
     */
    public boolean saveFlightsToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(FLIGHTS_FILE)))) {
            oos.writeObject(flights);
            System.out.println("[DataContext] Flights saved to disk. (" + flights.size() + " records)");
            return true;
        } catch (IOException e) {
            System.err.println("[DataContext] ERROR saving flights: " + e.getMessage());
            return false;
        }
    }

    /**
     * Serializes the tickets map to a binary .dat file using ObjectOutputStream.
     * @return true if save succeeded, false on I/O failure
     */
    public boolean saveTicketsToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(TICKETS_FILE)))) {
            oos.writeObject(ticketsMap);
            System.out.println("[DataContext] Tickets saved to disk. (" + ticketsMap.size() + " records)");
            return true;
        } catch (IOException e) {
            System.err.println("[DataContext] ERROR saving tickets: " + e.getMessage());
            return false;
        }
    }

    /**
     * Convenience method that saves both flights and tickets to disk in one call.
     * @return true if both saves succeeded
     */
    public boolean saveToDisk() {
        boolean flightsSaved = saveFlightsToDisk();
        boolean ticketsSaved = saveTicketsToDisk();
        return flightsSaved && ticketsSaved;
    }

    // ── Persistence: Load from Disk ────────────────────────────────────────────

    /**
     * Deserializes the flights list from its binary .dat file using ObjectInputStream.
     * @return true if data was successfully loaded, false if file not found or on error
     */
    @SuppressWarnings("unchecked")
    public boolean loadFlightsFromDisk() {
        File file = new File(FLIGHTS_FILE);
        if (!file.exists()) {
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof List) {
                flights = (List<Flight>) obj;
                System.out.println("[DataContext] Flights loaded from disk. (" + flights.size() + " records)");
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[DataContext] ERROR loading flights: " + e.getMessage());
        }
        return false;
    }

    /**
     * Deserializes the tickets map from its binary .dat file using ObjectInputStream.
     * @return true if data was successfully loaded, false if file not found or on error
     */
    @SuppressWarnings("unchecked")
    public boolean loadTicketsFromDisk() {
        File file = new File(TICKETS_FILE);
        if (!file.exists()) {
            return false;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                ticketsMap = (Map<String, Ticket>) obj;
                System.out.println("[DataContext] Tickets loaded from disk. (" + ticketsMap.size() + " records)");
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[DataContext] ERROR loading tickets: " + e.getMessage());
        }
        return false;
    }

    /**
     * Convenience method that loads both flights and tickets from disk in one call.
     * @return true if both loads succeeded
     */
    public boolean loadFromDisk() {
        boolean flightsLoaded = loadFlightsFromDisk();
        boolean ticketsLoaded = loadTicketsFromDisk();
        return flightsLoaded && ticketsLoaded;
    }

    // ── Flight CRUD Operations ─────────────────────────────────────────────────

    /**
     * Adds a new flight to the in-memory list.
     * @param flight The Flight object to add
     */
    public void addFlight(Flight flight) {
        if (flight != null && getFlightByNumber(flight.getFlightNumber()) == null) {
            flights.add(flight);
        }
    }

    /**
     * Retrieves a flight by its unique flight number.
     * @param flightNumber The flight number to search for
     * @return The matching Flight object, or null if not found
     */
    public Flight getFlightByNumber(String flightNumber) {
        return flights.stream()
                .filter(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber))
                .findFirst()
                .orElse(null);
    }

    /**
     * Removes a flight by its flight number.
     * @param flightNumber The flight number of the flight to remove
     * @return true if removal succeeded, false if flight was not found
     */
    public boolean removeFlight(String flightNumber) {
        return flights.removeIf(f -> f.getFlightNumber().equalsIgnoreCase(flightNumber));
    }

    /**
     * Returns a defensive copy of all flights in the system.
     * @return Unmodifiable list of all Flight records
     */
    public List<Flight> getAllFlights() {
        return Collections.unmodifiableList(flights);
    }

    // ── Ticket CRUD Operations ─────────────────────────────────────────────────

    /**
     * Stores a new ticket in the tickets map keyed by its ticketId.
     * @param ticket The Ticket object to store
     */
    public void addTicket(Ticket ticket) {
        if (ticket != null) {
            ticketsMap.put(ticket.getTicketId(), ticket);
        }
    }

    /**
     * Retrieves a ticket by its unique ticket ID.
     * @param ticketId The ticket ID to look up
     * @return The matching Ticket object, or null if not found
     */
    public Ticket getTicketById(String ticketId) {
        return ticketsMap.get(ticketId);
    }

    /**
     * Removes a ticket from the map by its ticket ID.
     * @param ticketId The ticket ID to remove
     * @return The removed Ticket object, or null if not found
     */
    public Ticket removeTicket(String ticketId) {
        return ticketsMap.remove(ticketId);
    }

    /**
     * Returns an unmodifiable view of all tickets in the system.
     * @return Unmodifiable map of all Ticket records (ticketId → Ticket)
     */
    public Map<String, Ticket> getAllTickets() {
        return Collections.unmodifiableMap(ticketsMap);
    }

    /**
     * Returns all tickets as a list, useful for UI display.
     * @return List of all Ticket objects
     */
    public List<Ticket> getAllTicketsList() {
        return new ArrayList<>(ticketsMap.values());
    }

    // ── Seed Data ──────────────────────────────────────────────────────────────

    /**
     * Populates the flights list with realistic sample data for first-run scenarios.
     * Covers a mix of routes, capacities, and price points across multiple airlines.
     */
    private void seedSampleData() {
        flights.add(new Flight("ET101", "Ethiopian Airlines", "Addis Ababa", "New York",    "2026-08-01 09:00", 180, 20,  850.00));
        flights.add(new Flight("ET202", "Ethiopian Airlines", "Addis Ababa", "London",      "2026-08-02 11:00", 150, 10,  720.00));
        flights.add(new Flight("ET303", "Ethiopian Airlines", "Addis Ababa", "Dubai",       "2026-08-03 14:00", 200, 45,  390.00));
        flights.add(new Flight("ET404", "Ethiopian Airlines", "Addis Ababa", "Beijing",     "2026-08-05 08:30", 160, 5,   640.00));
        flights.add(new Flight("EK501", "Emirates",           "Dubai",       "Addis Ababa", "2026-08-06 16:00", 250, 80,  410.00));
        flights.add(new Flight("EK602", "Emirates",           "Dubai",       "New York",    "2026-08-07 23:00", 300, 120, 950.00));
        flights.add(new Flight("QR701", "Qatar Airways",      "Doha",        "Addis Ababa", "2026-08-08 07:00", 200, 60,  380.00));
        flights.add(new Flight("QR802", "Qatar Airways",      "Doha",        "London",      "2026-08-09 13:00", 180, 30,  690.00));
        flights.add(new Flight("TK901", "Turkish Airlines",   "Istanbul",    "Addis Ababa", "2026-08-10 10:30", 220, 15,  490.00));
        flights.add(new Flight("KL001", "KLM",                "Amsterdam",   "Addis Ababa", "2026-08-11 06:00", 170, 170, 730.00)); // Fully booked — used to test overbooking guard
        System.out.println("[DataContext] Seeded " + flights.size() + " sample flights.");
    }

    // ── Summary ────────────────────────────────────────────────────────────────

    /**
     * Returns a summary count of flights and tickets currently in memory.
     * @return Summary string
     */
    @Override
    public String toString() {
        return String.format("DataContext[Flights: %d | Tickets: %d]", flights.size(), ticketsMap.size());
    }
}
