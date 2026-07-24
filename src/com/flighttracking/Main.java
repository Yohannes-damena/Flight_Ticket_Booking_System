package com.flighttracking;

import com.flighttracking.repository.DataContext;
import com.flighttracking.service.BookingService;
import com.flighttracking.view.MainDashboard;

import javax.swing.*;

/**
 * Application entry point.
 * Initialises the DataContext and BookingService, then launches the Swing UI on the EDT.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataContext    db      = new DataContext();
            BookingService service = new BookingService(db);
            new MainDashboard(service);
        });
    }
}
