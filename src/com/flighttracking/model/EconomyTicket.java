package com.flighttracking.model;

/**
 * Economy ticket subclass with baggage allowance and extra bag calculation rules.
 */
public class EconomyTicket extends Ticket {
    private static final long serialVersionUID = 1L;

    private int extraBaggageCount;
    private double baggageFeePerBag;

    public EconomyTicket() {
        super();
        this.baggageFeePerBag = 35.0;
    }

    public EconomyTicket(String ticketId, Passenger passenger, Flight flight, 
                         String bookingDate, String seatNumber, int extraBaggageCount) {
        super(ticketId, passenger, flight, bookingDate, seatNumber);
        this.extraBaggageCount = extraBaggageCount;
        this.baggageFeePerBag = 35.0;
    }

    public EconomyTicket(String ticketId, Passenger passenger, Flight flight, 
                         String bookingDate, String seatNumber, int extraBaggageCount, double baggageFeePerBag) {
        super(ticketId, passenger, flight, bookingDate, seatNumber);
        this.extraBaggageCount = extraBaggageCount;
        this.baggageFeePerBag = baggageFeePerBag;
    }

    @Override
    public double calculateTotalPrice() {
        if (getFlight() == null) {
            return 0.0;
        }
        return getFlight().getBasePrice() + (extraBaggageCount * baggageFeePerBag);
    }

    @Override
    public String getTierName() {
        return "Economy";
    }

    // Getters and Setters

    public int getExtraBaggageCount() {
        return extraBaggageCount;
    }

    public void setExtraBaggageCount(int extraBaggageCount) {
        this.extraBaggageCount = extraBaggageCount;
    }

    public double getBaggageFeePerBag() {
        return baggageFeePerBag;
    }

    public void setBaggageFeePerBag(double baggageFeePerBag) {
        this.baggageFeePerBag = baggageFeePerBag;
    }
}
