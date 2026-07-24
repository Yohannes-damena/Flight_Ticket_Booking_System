package com.flighttracking.view;

import com.flighttracking.exception.FlightOverbookedException;
import com.flighttracking.model.*;
import com.flighttracking.service.BookingService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.UUID;

/**
 * BookingFormDialog — a fully resized and properly laid-out modal JDialog.
 * Uses GridBagLayout for the form body to ensure all fields align correctly.
 */
public class BookingFormDialog extends JDialog {

    private static final int DIALOG_W = 640;
    private static final int DIALOG_H = 660;

    private final BookingService service;
    private final List<Flight>   flights;
    private final Flight         preSelectedFlight;

    // Result
    private Ticket bookedTicket = null;

    // ── Form controls ──────────────────────────────────────────────────────────
    private JComboBox<String> flightCombo;
    private JTextField        fullNameField, emailField, passportField, phoneField;
    private JComboBox<String> tierCombo;
    private JSpinner          baggageSpinner;
    private JCheckBox         loungeCheck;
    private JLabel            priceLabel;
    private JPanel            baggageRow, loungeRow;

    // ── Constructor ────────────────────────────────────────────────────────────
    public BookingFormDialog(Frame owner, BookingService service, Flight preSelectedFlight) {
        super(owner, "Book a Flight Ticket", true);
        this.service           = service;
        this.flights           = (List<Flight>) service.getAllFlights();
        this.preSelectedFlight = preSelectedFlight;

        setSize(DIALOG_W, DIALOG_H);
        setResizable(false);
        setLocationRelativeTo(owner);
        setUndecorated(true);

        setContentPane(buildRoot());
    }

    // ── Root ───────────────────────────────────────────────────────────────────
    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(FlightListPanel.BG_CARD);
        root.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR, 1));

        root.add(buildTitleBar(), BorderLayout.NORTH);
        root.add(buildForm(),     BorderLayout.CENTER);
        root.add(buildFooter(),   BorderLayout.SOUTH);
        return root;
    }

    // ── Title bar ──────────────────────────────────────────────────────────────
    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x12151F));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 22, 14, 14)
        ));

        JLabel title = new JLabel("✈   Book a Flight Ticket");
        title.setForeground(FlightListPanel.TEXT_PRIMARY);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        bar.add(title, BorderLayout.WEST);

        JButton closeBtn = new JButton("✕");
        closeBtn.setForeground(FlightListPanel.TEXT_MUTED);
        closeBtn.setBackground(new Color(0x12151F));
        closeBtn.setOpaque(true);
        closeBtn.setContentAreaFilled(true);
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        closeBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { closeBtn.setForeground(FlightListPanel.RED_FULL); }
            @Override public void mouseExited(MouseEvent e)  { closeBtn.setForeground(FlightListPanel.TEXT_MUTED); }
        });
        bar.add(closeBtn, BorderLayout.EAST);
        return bar;
    }

    // ── Form body ──────────────────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(FlightListPanel.BG_CARD);
        wrapper.setBorder(BorderFactory.createEmptyBorder(22, 30, 16, 30));

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(FlightListPanel.BG_CARD);
        GridBagConstraints gbc = baseGbc();

        // ── SECTION: Flight Selection ──────────────────────────────────────────
        addSectionHeader(grid, gbc, "FLIGHT SELECTION");

        String[] flightOptions = flights.stream()
                .map(f -> f.getFlightNumber() + "  —  " + f.getOrigin()
                        + " → " + f.getDestination()
                        + "   (" + f.getAvailableSeats() + " seats)"
                        + "   $" + (int) f.getBasePrice())
                .toArray(String[]::new);

        flightCombo = makeCombo(flightOptions);
        if (preSelectedFlight != null) {
            for (int i = 0; i < flights.size(); i++) {
                if (flights.get(i).getFlightNumber().equals(preSelectedFlight.getFlightNumber())) {
                    flightCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        addRow(grid, gbc, "Flight", flightCombo);

        // ── SECTION: Passenger ─────────────────────────────────────────────────
        addGap(grid, gbc, 10);
        addSectionHeader(grid, gbc, "PASSENGER INFORMATION");

        fullNameField = makeField("e.g. Abebe Bikila");
        emailField    = makeField("e.g. abebe@email.com");
        passportField = makeField("e.g. EP112233");
        phoneField    = makeField("e.g. +251 911 000 000");

        addRow(grid, gbc, "Full Name",    fullNameField);
        addRow(grid, gbc, "Email",        emailField);
        addRow(grid, gbc, "Passport No.", passportField);
        addRow(grid, gbc, "Phone",        phoneField);

        // ── SECTION: Ticket Options ────────────────────────────────────────────
        addGap(grid, gbc, 10);
        addSectionHeader(grid, gbc, "TICKET OPTIONS");

        tierCombo = makeCombo(new String[]{"Economy", "Business"});
        addRow(grid, gbc, "Class Tier", tierCombo);

        // Extra baggage row (Economy)
        baggageSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        styleSpinner(baggageSpinner);
        baggageRow = makeRowPanel("Extra Bags (+$35 each)", baggageSpinner);
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        grid.add(baggageRow, gbc);

        // Lounge access (Business)
        loungeCheck = new JCheckBox("  Include Airport Lounge Access  (+$75)");
        loungeCheck.setBackground(FlightListPanel.BG_CARD);
        loungeCheck.setForeground(FlightListPanel.TEXT_PRIMARY);
        loungeCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loungeCheck.setSelected(true);
        loungeRow = makeRowPanel("Lounge Access", loungeCheck);
        loungeRow.setVisible(false);
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        grid.add(loungeRow, gbc);

        // ── Price Banner ───────────────────────────────────────────────────────
        addGap(grid, gbc, 12);
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        grid.add(buildPriceBanner(), gbc);

        // Listeners
        flightCombo.addActionListener(e -> updateLivePrice());
        tierCombo.addActionListener(e -> { swapTierOptions(); updateLivePrice(); });
        baggageSpinner.addChangeListener(e -> updateLivePrice());
        loungeCheck.addActionListener(e -> updateLivePrice());

        updateLivePrice();

        wrapper.add(grid, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildPriceBanner() {
        JPanel banner = new JPanel(new BorderLayout(0, 0));
        banner.setBackground(new Color(0x141C35));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FlightListPanel.ACCENT_BLUE.darker(), 1),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        JLabel caption = new JLabel("Estimated Total");
        caption.setForeground(FlightListPanel.TEXT_MUTED);
        caption.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JLabel note = new JLabel("Includes base fare + add-ons");
        note.setForeground(new Color(0x4A5068));
        note.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        left.add(caption);
        left.add(note);

        priceLabel = new JLabel("$0.00");
        priceLabel.setForeground(FlightListPanel.ACCENT_BLUE);
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        priceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        banner.add(left,       BorderLayout.WEST);
        banner.add(priceLabel, BorderLayout.EAST);
        return banner;
    }

    // ── Footer ─────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 12));
        footer.setBackground(new Color(0x12151F));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, FlightListPanel.BORDER_COLOR));

        JButton cancelBtn = makeButton("   Cancel   ", FlightListPanel.BG_CARD, FlightListPanel.TEXT_MUTED);
        cancelBtn.addActionListener(e -> dispose());

        JButton bookBtn = makeButton("  Confirm Booking  ", FlightListPanel.ACCENT_BLUE, Color.WHITE);
        bookBtn.addActionListener(e -> handleBooking());

        footer.add(cancelBtn);
        footer.add(bookBtn);
        return footer;
    }

    // ── Tier toggle ────────────────────────────────────────────────────────────
    private void swapTierOptions() {
        boolean isBusiness = "Business".equals(tierCombo.getSelectedItem());
        baggageRow.setVisible(!isBusiness);
        loungeRow.setVisible(isBusiness);
    }

    // ── Live price ─────────────────────────────────────────────────────────────
    private void updateLivePrice() {
        int idx = flightCombo.getSelectedIndex();
        if (idx < 0 || idx >= flights.size()) { priceLabel.setText("$0.00"); return; }
        Flight f   = flights.get(idx);
        boolean eco = "Economy".equals(tierCombo.getSelectedItem());
        double price;
        if (eco) {
            price = f.getBasePrice() + (int) baggageSpinner.getValue() * 35.0;
        } else {
            price = f.getBasePrice() * 1.5 + (loungeCheck.isSelected() ? 75.0 : 0.0);
        }
        priceLabel.setText(String.format("$%.2f", price));
    }

    // ── Booking handler ────────────────────────────────────────────────────────
    private void handleBooking() {
        if (fullNameField.getText().isBlank()) { shake(fullNameField); err("Full name is required.");     return; }
        if (emailField.getText().isBlank())    { shake(emailField);    err("Email is required.");         return; }
        if (passportField.getText().isBlank()) { shake(passportField); err("Passport number is required."); return; }
        if (phoneField.getText().isBlank())    { shake(phoneField);    err("Phone number is required.");  return; }

        int idx = flightCombo.getSelectedIndex();
        if (idx < 0) { err("Please select a flight."); return; }

        Flight  flight = flights.get(idx);
        String  tier   = (String) tierCombo.getSelectedItem();
        int     bags   = (int) baggageSpinner.getValue();
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
            err("Flight " + flight.getFlightNumber() + " is fully booked! No seats available.");
        } catch (IllegalArgumentException ex) {
            err(ex.getMessage());
        }
    }

    public Ticket getBookedTicket() { return bookedTicket; }

    // ── GridBag helpers ────────────────────────────────────────────────────────
    private GridBagConstraints baseGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(4, 0, 4, 0);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;
        g.gridy   = 0;
        return g;
    }

    private void addSectionHeader(JPanel grid, GridBagConstraints gbc, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(FlightListPanel.ACCENT_BLUE);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x2A3155)),
                BorderFactory.createEmptyBorder(2, 0, 4, 0)
        ));
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        grid.add(lbl, gbc);
    }

    private void addRow(JPanel grid, GridBagConstraints gbc, String label, JComponent field) {
        gbc.gridy++;
        gbc.gridx     = 0;
        gbc.gridwidth = 1;
        gbc.weightx   = 0.0;
        gbc.insets    = new Insets(5, 0, 5, 16);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(FlightListPanel.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(130, 32));
        grid.add(lbl, gbc);

        gbc.gridx   = 1;
        gbc.weightx = 1.0;
        gbc.insets  = new Insets(5, 0, 5, 0);
        field.setPreferredSize(new Dimension(380, 34));
        grid.add(field, gbc);
    }

    private JPanel makeRowPanel(String label, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setBackground(FlightListPanel.BG_CARD);
        row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel lbl = new JLabel(label);
        lbl.setForeground(FlightListPanel.TEXT_MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setPreferredSize(new Dimension(130, 32));

        field.setPreferredSize(new Dimension(380, 34));
        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    private void addGap(JPanel grid, GridBagConstraints gbc, int height) {
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        grid.add(Box.createVerticalStrut(height), gbc);
    }

    // ── Widget factories ───────────────────────────────────────────────────────
    private JTextField makeField(String placeholder) {
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

    private <T> JComboBox<T> makeCombo(T[] items) {
        JComboBox<T> box = new JComboBox<>(items);
        box.setBackground(FlightListPanel.BG_DARK);
        box.setForeground(FlightListPanel.TEXT_PRIMARY);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));
        box.setOpaque(true);
        return box;
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(FlightListPanel.BG_DARK);
        spinner.setForeground(FlightListPanel.TEXT_PRIMARY);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinner.setBorder(BorderFactory.createLineBorder(FlightListPanel.BORDER_COLOR));
        JTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        tf.setBackground(FlightListPanel.BG_DARK);
        tf.setForeground(FlightListPanel.TEXT_PRIMARY);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setCaretColor(FlightListPanel.TEXT_PRIMARY);
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.equals(FlightListPanel.ACCENT_BLUE)
                        ? FlightListPanel.ACCENT_BLUE.darker() : FlightListPanel.BORDER_COLOR),
                BorderFactory.createEmptyBorder(9, 22, 9, 22)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color hover = bg.equals(FlightListPanel.ACCENT_BLUE)
                ? new Color(0x5AA0F0) : FlightListPanel.BG_HEADER;
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // ── Utilities ──────────────────────────────────────────────────────────────
    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Booking Error", JOptionPane.ERROR_MESSAGE);
    }

    private void shake(JComponent comp) {
        Point origin = comp.getLocation();
        int[] offsets = {-8, 8, -6, 6, -4, 4, 0};
        final int[] step = {0};
        Timer t = new Timer(28, null);
        t.addActionListener(e -> {
            comp.setLocation(origin.x + offsets[step[0]], origin.y);
            if (++step[0] >= offsets.length) { t.stop(); comp.setLocation(origin); }
        });
        t.start();
    }
}
