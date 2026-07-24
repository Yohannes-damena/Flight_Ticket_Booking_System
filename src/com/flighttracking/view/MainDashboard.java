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
 * MainDashboard — the primary JFrame application window.
 *
 * Layout:
 *  ┌──────────────────────────────────────────────────────────┐
 *  │  Header: App title + subtitle                             │
 *  ├──────────────────────────────────────────────────────────┤
 *  │  Stat Cards: Flights | Bookings | Revenue | Available     │
 *  ├──────────────────────────────────────────────────────────┤
 *  │  Toolbar: Book | Cancel | Refresh | Save                  │
 *  ├──────────────────────────────────────────────────────────┤
 *  │  Tabs:  ✈ Flights   |   🎫 My Bookings                   │
 *  │         FlightListPanel  │  JTable of Tickets             │
 *  └──────────────────────────────────────────────────────────┘
 */
public class MainDashboard extends JFrame {

    private final BookingService service;

    // Sub-panels
    private FlightListPanel flightListPanel;
    private JTable          ticketTable;
    private TicketTableModel ticketTableModel;

    // Stat card labels (updated on refresh)
    private JLabel statFlights, statBookings, statRevenue, statAvailable;

    // Static init block — set L&F BEFORE any Swing component is created
    static {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ignored) {}
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    public MainDashboard(BookingService service) {
        super("Flight Ticket Booking System");
        this.service = service;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        applyGlobalTheme();

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(FlightListPanel.BG_DARK);

        root.add(buildHeader(),   BorderLayout.NORTH);
        root.add(buildCenter(),   BorderLayout.CENTER);

        setContentPane(root);
        refreshAll();
        setVisible(true);
    }

    // ── Global Look & Feel ─────────────────────────────────────────────────────
    private void applyGlobalTheme() {
        // Metal LAF is already set in static block — configure colours here

        // Override colours globally
        UIManager.put("OptionPane.background",            FlightListPanel.BG_CARD);
        UIManager.put("Panel.background",                 FlightListPanel.BG_CARD);
        UIManager.put("OptionPane.messageForeground",     FlightListPanel.TEXT_PRIMARY);
        UIManager.put("Button.background",                FlightListPanel.BG_CARD);
        UIManager.put("Button.foreground",                FlightListPanel.TEXT_PRIMARY);
        UIManager.put("Button.select",                    FlightListPanel.BG_CARD.brighter());
        UIManager.put("Button.focus",                     new Color(0, 0, 0, 0));
        UIManager.put("ComboBox.background",              FlightListPanel.BG_DARK);
        UIManager.put("ComboBox.foreground",              FlightListPanel.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",     FlightListPanel.ACCENT_BLUE);
        UIManager.put("ComboBox.selectionForeground",     Color.WHITE);
        UIManager.put("TabbedPane.background",            FlightListPanel.BG_DARK);
        UIManager.put("TabbedPane.foreground",            FlightListPanel.TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",              FlightListPanel.BG_CARD);
        UIManager.put("TabbedPane.contentAreaColor",      FlightListPanel.BG_DARK);
        UIManager.put("ScrollBar.background",             FlightListPanel.BG_DARK);
        UIManager.put("ScrollBar.thumb",                  new Color(0x3A3D52));
    }

    // ── Header Band ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FlightListPanel.BG_DARK);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setBackground(FlightListPanel.BG_DARK);

        JLabel title = new JLabel("Flight Ticket Booking System");
        title.setForeground(FlightListPanel.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitle = new JLabel("Manage flights, issue tickets, and track bookings in real time");
        subtitle.setForeground(FlightListPanel.TEXT_MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(subtitle);
        header.add(titleGroup, BorderLayout.WEST);

        // Stat cards (right-aligned in header)
        JPanel statsRow = buildStatCards();
        header.add(statsRow, BorderLayout.EAST);

        return header;
    }

    // ── Stat Cards ─────────────────────────────────────────────────────────────
    private JPanel buildStatCards() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        row.setBackground(FlightListPanel.BG_DARK);

        statFlights   = new JLabel("0");
        statBookings  = new JLabel("0");
        statRevenue   = new JLabel("$0");
        statAvailable = new JLabel("0");

        row.add(statCard("Flights",   statFlights,   "F", FlightListPanel.ACCENT_BLUE));
        row.add(statCard("Bookings",  statBookings,  "B", FlightListPanel.ACCENT_PURPLE));
        row.add(statCard("Revenue",   statRevenue,   "$", FlightListPanel.GREEN_AVAIL));
        row.add(statCard("Available", statAvailable, "S", FlightListPanel.YELLOW_LOW));
        return row;
    }

    private JPanel statCard(String label, JLabel valueLabel, String icon, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 2));
        card.setBackground(FlightListPanel.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        card.setPreferredSize(new Dimension(120, 62));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        valueLabel.setForeground(accent);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel title = new JLabel(label);
        title.setForeground(FlightListPanel.TEXT_MUTED);
        title.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(iconLabel,   BorderLayout.WEST);
        card.add(valueLabel,  BorderLayout.CENTER);
        card.add(title,       BorderLayout.SOUTH);
        return card;
    }

    // ── Center: Toolbar + Tabbed Pane ─────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(FlightListPanel.BG_DARK);

        center.add(buildToolbar(),    BorderLayout.NORTH);
        center.add(buildTabbedPane(), BorderLayout.CENTER);
        return center;
    }

    // ── Toolbar ────────────────────────────────────────────────────────────────
    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(FlightListPanel.BG_DARK);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, FlightListPanel.BORDER_COLOR));

        JButton bookBtn   = toolbarButton("Book Ticket",       FlightListPanel.ACCENT_BLUE, Color.WHITE);
        JButton cancelBtn = toolbarButton("Cancel Booking",    FlightListPanel.BG_CARD, FlightListPanel.TEXT_PRIMARY);
        JButton refreshBtn= toolbarButton("Refresh",           FlightListPanel.BG_CARD, FlightListPanel.TEXT_PRIMARY);
        JButton saveBtn   = toolbarButton("Save Data",         FlightListPanel.BG_CARD, FlightListPanel.TEXT_PRIMARY);

        bookBtn.addActionListener(e -> onBookClicked());
        cancelBtn.addActionListener(e -> onCancelClicked());
        refreshBtn.addActionListener(e -> refreshAll());
        saveBtn.addActionListener(e -> {
            boolean ok = service.getDataContext().saveToDisk();
            JOptionPane.showMessageDialog(this,
                    ok ? "Data saved successfully." : "Save failed — check console.",
                    "Save", ok ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        });

        bar.add(bookBtn);
        bar.add(cancelBtn);
        bar.add(Box.createHorizontalStrut(6));
        bar.add(refreshBtn);
        bar.add(saveBtn);
        return bar;
    }

    private JButton toolbarButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);                    // must be true for custom bg to show
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(7, 16, 7, 16)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover effect
        Color hoverBg = bg.equals(FlightListPanel.ACCENT_BLUE)
                ? FlightListPanel.ACCENT_BLUE.brighter()
                : FlightListPanel.BG_HEADER;
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hoverBg); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Tabbed Pane ────────────────────────────────────────────────────────────
    private JTabbedPane buildTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(FlightListPanel.BG_DARK);
        tabs.setForeground(FlightListPanel.TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBorder(null);

        // Tab 1: Flights
        flightListPanel = new FlightListPanel(service);
        JPanel flightTab = new JPanel(new BorderLayout());
        flightTab.setBackground(FlightListPanel.BG_DARK);
        flightTab.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        flightTab.add(flightListPanel, BorderLayout.CENTER);
        tabs.addTab("  Flights  ", flightTab);

        // Tab 2: Bookings
        JPanel bookingsTab = buildBookingsTab();
        tabs.addTab("  My Bookings  ", bookingsTab);

        return tabs;
    }

    // ── Bookings Tab ───────────────────────────────────────────────────────────
    private JPanel buildBookingsTab() {
        JPanel tab = new JPanel(new BorderLayout(0, 0));
        tab.setBackground(FlightListPanel.BG_DARK);
        tab.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        ticketTableModel = new TicketTableModel();
        ticketTable = new JTable(ticketTableModel);
        styleTicketTable(ticketTable);

        JScrollPane scroll = new JScrollPane(ticketTable);
        scroll.setBackground(FlightListPanel.BG_DARK);
        scroll.getViewport().setBackground(FlightListPanel.BG_TABLE_ROW);
        scroll.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));
        scroll.getVerticalScrollBar().setUI(new FlightListPanel.DarkScrollBarUI());

        tab.add(scroll, BorderLayout.CENTER);
        return tab;
    }

    private void styleTicketTable(JTable table) {
        table.setBackground(FlightListPanel.BG_TABLE_ROW);
        table.setForeground(FlightListPanel.TEXT_PRIMARY);
        table.setSelectionBackground(FlightListPanel.ACCENT_PURPLE.darker());
        table.setSelectionForeground(Color.WHITE);
        table.setGridColor(FlightListPanel.BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setRowHeight(36);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setFocusable(false);
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(FlightListPanel.BG_HEADER);
        header.setForeground(FlightListPanel.TEXT_MUTED);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 42));

        // Tier badge renderer on column 5
        table.setDefaultRenderer(Object.class, new FlightListPanel.FlightRowRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new TierBadgeRenderer());

        int[] widths = {140, 130, 130, 100, 100, 90, 90, 100};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // ── Action Handlers ────────────────────────────────────────────────────────
    private void onBookClicked() {
        Flight selected = flightListPanel.getSelectedFlight();
        BookingFormDialog dialog = new BookingFormDialog(this, service, selected);
        dialog.setVisible(true);

        Ticket booked = dialog.getBookedTicket();
        if (booked != null) {
            refreshAll();
            TicketReceiptDialog receipt = new TicketReceiptDialog(this, booked);
            receipt.setVisible(true);
        }
    }

    private void onCancelClicked() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a ticket in the 'My Bookings' tab to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ticketId = (String) ticketTableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + ticketId + "?\nThis will free the seat and cannot be undone.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.cancelBooking(ticketId);
                refreshAll();
                JOptionPane.showMessageDialog(this, "Booking " + ticketId + " has been cancelled.",
                        "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Refresh All ────────────────────────────────────────────────────────────
    private void refreshAll() {
        // Stat cards
        List<Ticket> tickets = service.getAllTickets();
        statFlights.setText(String.valueOf(service.getAllFlights().size()));
        statBookings.setText(String.valueOf(tickets.size()));
        statRevenue.setText(String.format("$%.0f", service.getTotalRevenue()));
        statAvailable.setText(String.valueOf(service.getTotalAvailableSeats()));

        // Flight list panel
        if (flightListPanel != null) flightListPanel.refresh();

        // Ticket table
        if (ticketTableModel != null) ticketTableModel.setTickets(tickets);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Ticket Table Model
    // ══════════════════════════════════════════════════════════════════════════
    static class TicketTableModel extends AbstractTableModel {
        private static final String[] COLS = {
            "Ticket ID", "Passenger", "Passport", "Flight", "Route", "Tier", "Seat", "Total Price"
        };
        private List<Ticket> tickets = new java.util.ArrayList<>();

        void setTickets(List<Ticket> tickets) {
            this.tickets = tickets;
            fireTableDataChanged();
        }

        @Override public int getRowCount()    { return tickets.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            Ticket t = tickets.get(row);
            Passenger p = t.getPassenger();
            Flight    f = t.getFlight();
            return switch (col) {
                case 0 -> t.getTicketId();
                case 1 -> p.getFullName();
                case 2 -> p.getPassportNumber();
                case 3 -> f.getFlightNumber();
                case 4 -> f.getOrigin() + " → " + f.getDestination();
                case 5 -> t.getTierName();
                case 6 -> t.getSeatNumber();
                case 7 -> String.format("$%.2f", t.calculateTotalPrice());
                default -> "";
            };
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Inner class: Tier Badge Renderer (Economy=Green, Business=Purple)
    // ══════════════════════════════════════════════════════════════════════════
    static class TierBadgeRenderer extends JLabel implements TableCellRenderer {
        TierBadgeRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            setText(String.valueOf(value));
            boolean isBusiness = "Business".equals(value);
            if (isSelected) {
                setBackground(FlightListPanel.ACCENT_PURPLE.darker());
                setForeground(Color.WHITE);
            } else {
                setBackground(isBusiness
                    ? new Color(0x2D1B4E)
                    : new Color(0x0D2E1A));
                setForeground(isBusiness
                    ? FlightListPanel.ACCENT_PURPLE
                    : FlightListPanel.GREEN_AVAIL);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            return this;
        }
    }
}
