package com.flighttracking.model;

/**
 * Business class ticket subclass with premium modifiers, lounge access, and priority rules.
 */
public class BusinessTicket extends Ticket {
    private static final long serialVersionUID = 1L;

    private boolean loungeAccessIncluded;
    private boolean priorityBoarding;
    private double loungeFee;
    private double priceMultiplier;

    public BusinessTicket() {
        super();
        this.loungeAccessIncluded = true;
        this.priorityBoarding = true;
        this.loungeFee = 75.0;
        this.priceMultiplier = 1.5;
    }

    public BusinessTicket(String ticketId, Passenger passenger, Flight flight, 
                          String bookingDate, String seatNumber, boolean loungeAccessIncluded) {
        super(ticketId, passenger, flight, bookingDate, seatNumber);
        this.loungeAccessIncluded = loungeAccessIncluded;
        this.priorityBoarding = true;
        this.loungeFee = 75.0;
        this.priceMultiplier = 1.5;
    }

    public BusinessTicket(String ticketId, Passenger passenger, Flight flight, 
                          String bookingDate, String seatNumber, boolean loungeAccessIncluded, 
                          boolean priorityBoarding, double loungeFee, double priceMultiplier) {
        super(ticketId, passenger, flight, bookingDate, seatNumber);
        this.loungeAccessIncluded = loungeAccessIncluded;
        this.priorityBoarding = priorityBoarding;
        this.loungeFee = loungeFee;
        this.priceMultiplier = priceMultiplier;
    }

    @Override
    public double calculateTotalPrice() {
        if (getFlight() == null) {
            return 0.0;
        }
        double base = getFlight().getBasePrice() * priceMultiplier;
        double lounge = loungeAccessIncluded ? loungeFee : 0.0;
        return base + lounge;
    }

    @Override
    public String getTierName() {
        return "Business";
    }

    // Getters and Setters

    public boolean isLoungeAccessIncluded() {
        return loungeAccessIncluded;
    }

    public void setLoungeAccessIncluded(boolean loungeAccessIncluded) {
        this.loungeAccessIncluded = loungeAccessIncluded;
    }

    public boolean isPriorityBoarding() {
        return priorityBoarding;
    }

    public void setPriorityBoarding(boolean priorityBoarding) {
        this.priorityBoarding = priorityBoarding;
    }

    public double getLoungeFee() {
        return loungeFee;
    }

    public void setLoungeFee(double loungeFee) {
        this.loungeFee = loungeFee;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }

    public void setPriceMultiplier(double priceMultiplier) {
        this.priceMultiplier = priceMultiplier;
    }
}
