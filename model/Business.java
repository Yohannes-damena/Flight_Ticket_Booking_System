package com.flighttracking.model;

/**
 * Business ticket alias class extending BusinessTicket for compatibility.
 */
public class Business extends BusinessTicket {
    private static final long serialVersionUID = 1L;

    public Business() {
        super();
    }

    public Business(String ticketId, Passenger passenger, Flight flight, 
                    String bookingDate, String seatNumber, boolean loungeAccessIncluded) {
        super(ticketId, passenger, flight, bookingDate, seatNumber, loungeAccessIncluded);
    }
}
