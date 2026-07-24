package com.flighttracking.view;

import com.flighttracking.exception.FlightOverbookedException;
import com.flighttracking.model.*;
import com.flighttracking.service.BookingService;
import com.flighttracking.view.FlightListPanel.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.UUID;

/**
 * BookingFormDialog — a modal JDialog that captures passenger information,
 * flight selection, ticket tier, and add-on options. Computes the live price
 * and submits via BookingService.bookFlight().
 */
public class BookingFormDialog extends JDialog {

    private final BookingService service;
    private final List<Flight>   flights;

    // Result
    private Ticket bookedTicket = null;

    // ── Form Fields ────────────────────────────────────────────────────────────
    private JComboBox<String> flightCombo;
    private JTextField        fullNameField, emailField, passportField, phoneField;
    private JComboBox<String> tierCombo;
    private JSpinner          baggageSpinner;
    private JCheckBox         loungeCheck;
    private JLabel            priceLabel;

    // Pre-selected flight (from table selection)
    private final Flight preSelectedFlight;

    public BookingFormDialog(Frame owner, BookingService service, Flight preSelectedFlight) {
        super(owner, "Book a Flight Ticket", true);
        this.service          = service;
        this.flights          = (List<Flight>) service.getAllFlights();
        this.preSelectedFlight = preSelectedFlight;

        setSize(560, 680);
        setResizable(false);
        setLocationRelativeTo(owner);
        setUndecorated(true);

        JPanel root = buildRootPanel();
        setContentPane(root);
    }

    // ── Root Panel ─────────────────────────────────────────────────────────────
    private JPanel buildRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FlightListPanel.BG_CARD);
        root.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR, 1));

        root.add(buildTitleBar(),   BorderLayout.NORTH);
        root.add(buildFormBody(),   BorderLayout.CENTER);
        root.add(buildFooter(),     BorderLayout.SOUTH);
        return root;
    }

    // ── Title Bar ──────────────────────────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(FlightListPanel.BG_DARK);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel title = new JLabel("✈  Book a Flight Ticket");
        title.setForeground(FlightListPanel.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton closeBtn = new JButton("✕");
        closeBtn.setBackground(FlightListPanel.BG_DARK);
        closeBtn.setForeground(FlightListPanel.TEXT_MUTED);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(hoverEffect(closeBtn, FlightListPanel.RED_FULL, FlightListPanel.BG_DARK));

        bar.add(title,    BorderLayout.WEST);
        bar.add(closeBtn, BorderLayout.EAST);
        return bar;
    }

    // ── Form Body ──────────────────────────────────────────────────────────────
    private JScrollPane buildFormBody() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(FlightListPanel.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));

        // Section: Flight Selection
        form.add(sectionLabel("Flight Selection"));
        form.add(Box.createVerticalStrut(8));

        String[] flightOptions = flights.stream()
                .map(f -> f.getFlightNumber() + " — " + f.getOrigin() + " → " + f.getDestination()
                        + "  (" + f.getAvailableSeats() + " seats)  $" + (int) f.getBasePrice())
                .toArray(String[]::new);
        flightCombo = styledCombo(flightOptions);
        if (preSelectedFlight != null) {
            for (int i = 0; i < flights.size(); i++) {
                if (flights.get(i).getFlightNumber().equals(preSelectedFlight.getFlightNumber())) {
                    flightCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        form.add(labeledRow("Flight", flightCombo));
        form.add(Box.createVerticalStrut(16));

        // Section: Passenger Information
        form.add(sectionLabel("Passenger Information"));
        form.add(Box.createVerticalStrut(8));
        fullNameField = styledField("e.g. Abebe Bikila");
        emailField    = styledField("e.g. abebe@email.com");
        passportField = styledField("e.g. EP112233");
        phoneField    = styledField("e.g. +251911000000");
        form.add(labeledRow("Full Name",       fullNameField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledRow("Email",           emailField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledRow("Passport No.",    passportField));
        form.add(Box.createVerticalStrut(8));
        form.add(labeledRow("Phone Number",    phoneField));
        form.add(Box.createVerticalStrut(16));

        // Section: Ticket Options
        form.add(sectionLabel("Ticket Options"));
        form.add(Box.createVerticalStrut(8));

        tierCombo = styledCombo(new String[]{"Economy", "Business"});
        tierCombo.addActionListener(e -> updateOptionsVisibility(form));
        form.add(labeledRow("Class Tier", tierCombo));
        form.add(Box.createVerticalStrut(8));

        // Extra baggage (Economy)
        baggageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        styleSpinner(baggageSpinner);
        form.add(labeledRow("Extra Bags ($35 each)", baggageSpinner));
        form.add(Box.createVerticalStrut(8));

        // Lounge access (Business)
        loungeCheck = new JCheckBox("Include Airport Lounge Access (+$75)");
        loungeCheck.setBackground(FlightListPanel.BG_CARD);
        loungeCheck.setForeground(FlightListPanel.TEXT_PRIMARY);
        loungeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loungeCheck.setSelected(true);
        loungeCheck.setVisible(false);
        form.add(loungeCheck);
        form.add(Box.createVerticalStrut(16));

        // Live price preview
        JPanel priceBanner = new JPanel(new BorderLayout());
        priceBanner.setBackground(new Color(0x1A2540));
        priceBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.ACCENT_BLUE.darker(), 1),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)
        ));
        JLabel priceTitle = new JLabel("Estimated Total");
        priceTitle.setForeground(FlightListPanel.TEXT_MUTED);
        priceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        priceLabel = new JLabel("$0.00");
        priceLabel.setForeground(FlightListPanel.ACCENT_BLUE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        priceBanner.add(priceTitle, BorderLayout.WEST);
        priceBanner.add(priceLabel, BorderLayout.EAST);
        form.add(priceBanner);

        // Attach live price listeners
        flightCombo.addActionListener(e -> updateLivePrice());
        tierCombo.addActionListener(e -> { updateOptionsVisibility(form); updateLivePrice(); });
        baggageSpinner.addChangeListener(e -> updateLivePrice());
        loungeCheck.addActionListener(e -> updateLivePrice());

        updateLivePrice();

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBackground(FlightListPanel.BG_CARD);
        scroll.getViewport().setBackground(FlightListPanel.BG_CARD);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUI(new FlightListPanel.DarkScrollBarUI());
        return scroll;
    }

    private void updateOptionsVisibility(JPanel form) {
        boolean isBusiness = "Business".equals(tierCombo.getSelectedItem());
        baggageSpinner.getParent().setVisible(!isBusiness);
        loungeCheck.setVisible(isBusiness);
        form.revalidate();
        form.repaint();
    }

    private void updateLivePrice() {
        int idx = flightCombo.getSelectedIndex();
        if (idx < 0 || idx >= flights.size()) { priceLabel.setText("$0.00"); return; }
        Flight f    = flights.get(idx);
        boolean eco = "Economy".equals(tierCombo.getSelectedItem());
        double price;
        if (eco) {
            int bags = (int) baggageSpinner.getValue();
            price = f.getBasePrice() + bags * 35.0;
        } else {
            boolean lounge = loungeCheck.isSelected();
            price = f.getBasePrice() * 1.5 + (lounge ? 75.0 : 0.0);
        }
        priceLabel.setText(String.format("$%.2f", price));
    }

    // ── Footer ─────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        footer.setBackground(FlightListPanel.BG_DARK);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, FlightListPanel.BORDER_COLOR));

        JButton cancelBtn = styledButton("Cancel", FlightListPanel.BG_CARD, FlightListPanel.TEXT_MUTED);
        cancelBtn.addActionListener(e -> dispose());

        JButton bookBtn = styledButton("  Confirm Booking  ", FlightListPanel.ACCENT_BLUE, Color.WHITE);
        bookBtn.addActionListener(e -> handleBooking());

        footer.add(cancelBtn);
        footer.add(bookBtn);
        return footer;
    }

    // ── Booking Handler ────────────────────────────────────────────────────────
    private void handleBooking() {
        // Validate fields
        if (fullNameField.getText().isBlank()) { shake(fullNameField); showError("Full name is required."); return; }
        if (emailField.getText().isBlank())    { shake(emailField);    showError("Email is required.");     return; }
        if (passportField.getText().isBlank()) { shake(passportField); showError("Passport number is required."); return; }
        if (phoneField.getText().isBlank())    { shake(phoneField);    showError("Phone number is required."); return; }

        int idx = flightCombo.getSelectedIndex();
        if (idx < 0) { showError("Please select a flight."); return; }

        Flight flight = flights.get(idx);
        String tier   = (String) tierCombo.getSelectedItem();
        int    bags   = (int) baggageSpinner.getValue();
        boolean lounge = loungeCheck.isSelected() && "Business".equals(tier);

        Passenger passenger = new Passenger(
            "P-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(),
            fullNameField.getText().trim(),
            emailField.getText().trim(),
            passportField.getText().trim(),
            phoneField.getText().trim()
        );

        try {
            bookedTicket = service.bookFlight(passenger, flight, tier, bags, lounge);
            dispose();
        } catch (FlightOverbookedException ex) {
            showError("Flight " + flight.getFlightNumber() + " is fully booked!\nNo available seats remaining.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    public Ticket getBookedTicket() { return bookedTicket; }

    // ── UI Helpers ─────────────────────────────────────────────────────────────
    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setForeground(FlightListPanel.ACCENT_BLUE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, FlightListPanel.BORDER_COLOR));
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return lbl;
    }

    private JPanel labeledRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(FlightListPanel.BG_CARD);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(FlightListPanel.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setPreferredSize(new Dimension(140, 30));
        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private JTextField styledField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(FlightListPanel.BG_DARK);
        f.setForeground(FlightListPanel.TEXT_PRIMARY);
        f.setCaretColor(FlightListPanel.TEXT_PRIMARY);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return f;
    }

    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setBackground(FlightListPanel.BG_DARK);
        box.setForeground(FlightListPanel.TEXT_PRIMARY);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));
        return box;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(FlightListPanel.BG_DARK);
        spinner.setForeground(FlightListPanel.TEXT_PRIMARY);
        spinner.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(FlightListPanel.BG_DARK);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(FlightListPanel.TEXT_PRIMARY);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker()),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Booking Error",
                JOptionPane.ERROR_MESSAGE);
    }

    private void shake(JComponent comp) {
        Point origin = comp.getLocation();
        Timer timer = new Timer(30, null);
        final int[] step = {0};
        int[] offsets = {-8, 8, -6, 6, -4, 4, 0};
        timer.addActionListener(e -> {
            comp.setLocation(origin.x + offsets[step[0]], origin.y);
            step[0]++;
            if (step[0] >= offsets.length) { timer.stop(); comp.setLocation(origin); }
        });
        timer.start();
    }

    private MouseAdapter hoverEffect(JButton btn, Color hover, Color normal) {
        return new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }
}
