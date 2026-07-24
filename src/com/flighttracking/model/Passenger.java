package com.flighttracking.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a customer profile structure for passengers booking flight tickets.
 */
public class Passenger implements Serializable {
    private static final long serialVersionUID = 1L;

    private String passengerId;
    private String fullName;
    private String email;
    private String passportNumber;
    private String phoneNumber;

    public Passenger() {
    }

    public Passenger(String passengerId, String fullName, String email, String passportNumber, String phoneNumber) {
        this.passengerId = passengerId;
        this.fullName = fullName;
        this.email = email;
        this.passportNumber = passportNumber;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return Objects.equals(passengerId, passenger.passengerId) ||
                Objects.equals(passportNumber, passenger.passportNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerId, passportNumber);
    }

    @Override
    public String toString() {
        return String.format("Passenger[%s, Name: %s, Passport: %s, Email: %s]",
                passengerId, fullName, passportNumber, email);
    }
}
