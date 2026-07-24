package com.flighttracking.view;

import com.flighttracking.model.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

/**
 * TicketReceiptDialog — a modal confirmation dialog displaying the full
 * details of a successfully issued ticket after a booking is made.
 */
public class TicketReceiptDialog extends JDialog {

    public TicketReceiptDialog(Frame owner, Ticket ticket) {
        super(owner, "Booking Confirmation", true);
        setSize(480, 540);
        setResizable(false);
        setLocationRelativeTo(owner);
        setContentPane(buildContent(ticket));
    }

    private JPanel buildContent(Ticket t) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FlightListPanel.BG_CARD);
        root.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));

        // ── Header ─────────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FlightListPanel.ACCENT_BLUE.darker().darker());
        header.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));

        JLabel checkmark = new JLabel("✔  Booking Confirmed!");
        checkmark.setForeground(Color.WHITE);
        checkmark.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("Your ticket has been issued and saved.");
        sub.setForeground(new Color(0xBBCCEE));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel textStack = new JPanel();
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.setOpaque(false);
        textStack.add(checkmark);
        textStack.add(Box.createVerticalStrut(4));
        textStack.add(sub);
        header.add(textStack, BorderLayout.CENTER);

        // Tier badge
        boolean isBusiness = "Business".equals(t.getTierName());
        JLabel badge = new JLabel(" " + t.getTierName().toUpperCase() + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(isBusiness ? new Color(0x7C5CBF) : new Color(0x27AE60));
        badge.setForeground(Color.WHITE);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        header.add(badge, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // ── Ticket Body ────────────────────────────────────────────────────────
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(FlightListPanel.BG_CARD);
        body.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        // Ticket ID strip
        JPanel idStrip = new JPanel(new BorderLayout());
        idStrip.setBackground(FlightListPanel.BG_DARK);
        idStrip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        JLabel idLabel = new JLabel("Ticket ID");
        idLabel.setForeground(FlightListPanel.TEXT_MUTED);
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel idValue = new JLabel(t.getTicketId());
        idValue.setForeground(FlightListPanel.ACCENT_BLUE);
        idValue.setFont(new Font("Consolas", Font.BOLD, 14));
        idStrip.add(idLabel, BorderLayout.WEST);
        idStrip.add(idValue, BorderLayout.EAST);
        idStrip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        body.add(idStrip);
        body.add(Box.createVerticalStrut(18));

        // Details grid
        Flight f = t.getFlight();
        Passenger p = t.getPassenger();

        body.add(detailRow("Passenger",    p.getFullName()));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Email",        p.getEmail()));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Passport",     p.getPassportNumber()));
        body.add(Box.createVerticalStrut(14));
        body.add(divider());
        body.add(Box.createVerticalStrut(14));
        body.add(detailRow("Flight",       f.getFlightNumber() + "  (" + f.getAirline() + ")"));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Route",        f.getOrigin() + "  →  " + f.getDestination()));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Departure",    f.getDepartureTime()));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Seat Number",  t.getSeatNumber()));
        body.add(Box.createVerticalStrut(8));
        body.add(detailRow("Booking Date", t.getBookingDate()));

        // Add-ons
        if (t instanceof BusinessTicket bt) {
            body.add(Box.createVerticalStrut(8));
            body.add(detailRow("Lounge Access",    bt.isLoungeAccessIncluded() ? "✓ Included" : "—"));
            body.add(Box.createVerticalStrut(8));
            body.add(detailRow("Priority Boarding", bt.isPriorityBoarding()     ? "✓ Included" : "—"));
        } else if (t instanceof EconomyTicket et) {
            body.add(Box.createVerticalStrut(8));
            body.add(detailRow("Extra Bags", et.getExtraBaggageCount() + "  (×$35 each)"));
        }

        body.add(Box.createVerticalStrut(14));
        body.add(divider());
        body.add(Box.createVerticalStrut(14));

        // Total price
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setBackground(FlightListPanel.BG_CARD);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel totalLabel = new JLabel("Total Charged");
        totalLabel.setForeground(FlightListPanel.TEXT_MUTED);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel totalValue = new JLabel(String.format("$%.2f", t.calculateTotalPrice()));
        totalValue.setForeground(FlightListPanel.GREEN_AVAIL);
        totalValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalRow.add(totalLabel, BorderLayout.WEST);
        totalRow.add(totalValue, BorderLayout.EAST);
        body.add(totalRow);

        root.add(body, BorderLayout.CENTER);

        // ── Footer ─────────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        footer.setBackground(FlightListPanel.BG_DARK);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, FlightListPanel.BORDER_COLOR));

        JButton closeBtn = new JButton("  Close  ");
        closeBtn.setBackground(FlightListPanel.ACCENT_BLUE);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setOpaque(true);
        closeBtn.setContentAreaFilled(true);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        closeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.ACCENT_BLUE.darker()),
                BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        root.add(footer, BorderLayout.SOUTH);
        return root;
    }

    private JPanel detailRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(FlightListPanel.BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(FlightListPanel.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setPreferredSize(new Dimension(130, 22));

        JLabel val = new JLabel(value);
        val.setForeground(FlightListPanel.TEXT_PRIMARY);
        val.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private JSeparator divider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(FlightListPanel.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }
}
