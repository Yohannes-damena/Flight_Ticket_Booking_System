package com.flighttracking.view;

import com.flighttracking.model.*;
import com.flighttracking.service.BookingService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * FlightListPanel — a JPanel that displays all available flights in a styled JTable.
 * Includes a live search/filter bar and a real-time seat availability colour indicator.
 */
public class FlightListPanel extends JPanel {

    // ── Palette ───────────────────────────────────────────────────────────────
    static final Color BG_DARK       = new Color(0x0F1117);
    static final Color BG_CARD       = new Color(0x1A1D27);
    static final Color BG_TABLE_ROW  = new Color(0x1E2130);
    static final Color BG_TABLE_ALT  = new Color(0x181B28);
    static final Color BG_HEADER     = new Color(0x12151F);
    static final Color ACCENT_BLUE   = new Color(0x4A90E2);
    static final Color ACCENT_PURPLE = new Color(0x7C5CBF);
    static final Color TEXT_PRIMARY  = new Color(0xECEFF4);
    static final Color TEXT_MUTED    = new Color(0x8892A4);
    static final Color BORDER_COLOR  = new Color(0x2A2D3E);
    static final Color GREEN_AVAIL   = new Color(0x27AE60);
    static final Color YELLOW_LOW    = new Color(0xF39C12);
    static final Color RED_FULL      = new Color(0xE74C3C);

    private final BookingService service;
    private JTable flightTable;
    private FlightTableModel tableModel;
    private JTextField searchField;
    private JLabel statusLabel;

    public FlightListPanel(BookingService service) {
        this.service = service;
        setLayout(new BorderLayout(0, 12));
        setBackground(BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        refresh();
    }

    // ── Search Bar ─────────────────────────────────────────────────────────────
    private JPanel buildSearchBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(BG_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel icon = new JLabel("🔍");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        searchField = new JTextField();
        searchField.setBackground(BG_CARD);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by flight number, airline, origin or destination...");

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG_CARD);
        wrapper.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        wrapper.add(icon, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);

        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { applyFilter(); }
        });

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    // ── Table Panel ────────────────────────────────────────────────────────────
    private JScrollPane buildTablePanel() {
        tableModel = new FlightTableModel();
        flightTable = new JTable(tableModel);
        styleTable(flightTable);

        JScrollPane scroll = new JScrollPane(flightTable);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_TABLE_ROW);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scroll.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        return scroll;
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_TABLE_ROW);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(ACCENT_BLUE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));

        // Column widths
        int[] widths = {90, 160, 130, 130, 140, 80, 100, 110, 100};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Custom renderers
        table.setDefaultRenderer(Object.class, new FlightRowRenderer());
        // Seat availability badge renderer in last column
        table.getColumnModel().getColumn(7).setCellRenderer(new SeatBadgeRenderer());
    }

    // ── Status Bar ─────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        statusLabel = new JLabel("Loading...");
        statusLabel.setForeground(TEXT_MUTED);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        bar.add(statusLabel, BorderLayout.WEST);

        JLabel legend = new JLabel("  ● Available   ● Low (<20%)   ● Full");
        legend.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legend.setForeground(TEXT_MUTED);
        bar.add(legend, BorderLayout.EAST);
        return bar;
    }

    // ── Data Refresh ───────────────────────────────────────────────────────────
    public void refresh() {
        applyFilter();
    }

    private void applyFilter() {
        String query = searchField != null ? searchField.getText().trim() : "";
        List<Flight> results = service.searchFlights(query);
        tableModel.setFlights(results);
        statusLabel.setText(results.size() + " flight(s) found  •  Total available seats: " + service.getTotalAvailableSeats());
    }

    /** Returns the Flight object selected in the table, or null if none selected. */
    public Flight getSelectedFlight() {
        int row = flightTable.getSelectedRow();
        if (row < 0) return null;
        return tableModel.getFlightAt(row);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Flight Table Model
    // ══════════════════════════════════════════════════════════════════════════
    static class FlightTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
            "Flight #", "Airline", "Origin", "Destination",
            "Departure", "Total", "Booked", "Available", "Base Price"
        };
        private List<Flight> flights = new java.util.ArrayList<>();

        void setFlights(List<Flight> flights) {
            this.flights = flights;
            fireTableDataChanged();
        }

        Flight getFlightAt(int row) {
            return flights.get(row);
        }

        @Override public int getRowCount()    { return flights.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Flight f = flights.get(row);
            return switch (col) {
                case 0 -> f.getFlightNumber();
                case 1 -> f.getAirline();
                case 2 -> f.getOrigin();
                case 3 -> f.getDestination();
                case 4 -> f.getDepartureTime();
                case 5 -> f.getTotalSeats();
                case 6 -> f.getBookedSeats();
                case 7 -> f.getAvailableSeats();
                case 8 -> String.format("ETB %.0f", f.getBasePrice());
                default -> "";
            };
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Alternating Row Renderer
    // ══════════════════════════════════════════════════════════════════════════
    static class FlightRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (isSelected) {
                setBackground(ACCENT_BLUE.darker());
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? BG_TABLE_ROW : BG_TABLE_ALT);
                setForeground(TEXT_PRIMARY);
            }
            return this;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Seat Availability Badge Renderer
    // ══════════════════════════════════════════════════════════════════════════
    class SeatBadgeRenderer extends JLabel implements TableCellRenderer {
        SeatBadgeRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Flight f = tableModel.getFlightAt(row);
            int available = f.getAvailableSeats();
            double ratio  = (double) available / f.getTotalSeats();

            setText(String.valueOf(available));

            if (isSelected) {
                setBackground(ACCENT_BLUE.darker());
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? BG_TABLE_ROW : BG_TABLE_ALT);
                if (available == 0) {
                    setForeground(RED_FULL);
                } else if (ratio < 0.2) {
                    setForeground(YELLOW_LOW);
                } else {
                    setForeground(GREEN_AVAIL);
                }
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return this;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Minimal dark scroll bar UI
    // ══════════════════════════════════════════════════════════════════════════
    static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor = new Color(0x3A3D52);
            trackColor = BG_DARK;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
        private JButton zeroButton() {
            JButton b = new JButton(); b.setPreferredSize(new Dimension(0, 0)); return b;
        }
    }
}
