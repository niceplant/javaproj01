package mtba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MovieTicketBookingApp extends JFrame {
    private static final long serialVersionUID = 1L; 
    private static final Color ACCENT_YELLOW = new Color(255, 213, 0); // Saturated Yellow (#FFD500)
    private static final Color AVAILABLE_GREEN = new Color(46, 204, 113); // Solid Green for Available
    private static final Color BACKGROUND_LIGHT = new Color(245, 245, 245); // Light Gray Background
    private static final Color FOREGROUND_DARK = new Color(51, 51, 51); // Dark Text (for seat numbers, buttons, headers)
    private static final Color ERROR_RED = new Color(231, 76, 60); // Solid Red for Booked
    private static final Color NEUTRAL_GRAY = new Color(189, 195, 199); // Light Gray for secondary elements
    
    private static final Font MODERN_FONT_BOLD_16 = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font MODERN_FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font MODERN_FONT_TITLE_20 = new Font("Segoe UI", Font.BOLD, 20);
    // --------------------------------------

    private DatabaseManager dbManager; 
    private JComboBox<String> movieCombo, theatreCombo, dateCombo;
    private JPanel seatsPanel;
    private JButton[][] seatButtons;
    private ArrayList<String> selectedSeats;
    private JLabel totalLabel;

    private static final int ROWS = 8;
    private static final int COLS = 10;
    private static final double TICKET_PRICE = 250.0;

    public MovieTicketBookingApp() {
        setTitle("Movie Ticket Booking System - User Booking");
        setSize(1000, 700);
        // Changed to DISPOSE_ON_CLOSE so closing the user frame doesn't kill the whole app process
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null);
        
        getContentPane().setBackground(BACKGROUND_LIGHT);

        selectedSeats = new ArrayList<>();
        // Note: DatabaseManager must be implemented.
        dbManager = new DatabaseManager(); 
        
        // Setting the default font needs to be done once at startup via UIManager, 
        // but we'll apply it manually to components here for reliability.
        setUIFont(MODERN_FONT_PLAIN_14);

        initComponents();
        setVisible(true);
    }
    
    // Helper method to apply font to UIManager defaults (fix for previous error)
    private static void setUIFont(Font font) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement(); 
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }

    private void styleModernButton(JButton button, Color background, Color foreground) {
        button.setFont(MODERN_FONT_BOLD_16);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setBorder(new EmptyBorder(10, 20, 10, 20)); 
        button.setFocusPainted(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        
        // --- Top Panel - Selection (White Card) ---
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 15, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(15, 15, 10, 15), 
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1)
        ));

        // Labels
        JLabel movieLabel = new JLabel("Select Movie:");
        movieLabel.setFont(MODERN_FONT_BOLD_16);
        movieLabel.setForeground(FOREGROUND_DARK);
        topPanel.add(movieLabel);
        
        movieCombo = new JComboBox<>();
        movieCombo.setFont(MODERN_FONT_PLAIN_14);
        movieCombo.setBackground(Color.WHITE);
        movieCombo.setBorder(new LineBorder(NEUTRAL_GRAY));
        loadMovies();
        topPanel.add(movieCombo);

        JLabel theatreLabel = new JLabel("Select Theatre:");
        theatreLabel.setFont(MODERN_FONT_BOLD_16);
        theatreLabel.setForeground(FOREGROUND_DARK);
        topPanel.add(theatreLabel);

        theatreCombo = new JComboBox<>();
        theatreCombo.setFont(MODERN_FONT_PLAIN_14);
        theatreCombo.setBackground(Color.WHITE);
        theatreCombo.setBorder(new LineBorder(NEUTRAL_GRAY));
        loadTheatres();
        topPanel.add(theatreCombo);

        JLabel dateLabel = new JLabel("Select Date:");
        dateLabel.setFont(MODERN_FONT_BOLD_16);
        dateLabel.setForeground(FOREGROUND_DARK);
        topPanel.add(dateLabel);

        dateCombo = new JComboBox<>();
        dateCombo.setFont(MODERN_FONT_PLAIN_14);
        dateCombo.setBackground(Color.WHITE);
        dateCombo.setBorder(new LineBorder(NEUTRAL_GRAY));
        loadDates();
        topPanel.add(dateCombo);

        // Load Seats Button (Yellow Accent)
        JButton loadSeatsBtn = new JButton("Load Seats");
        styleModernButton(loadSeatsBtn, ACCENT_YELLOW, FOREGROUND_DARK); 
        loadSeatsBtn.addActionListener(e -> loadSeats());
        topPanel.add(new JLabel());
        topPanel.add(loadSeatsBtn);

        add(topPanel, BorderLayout.NORTH);

        // --- Screen Label (Yellow Bar) ---
        JPanel screenPanel = new JPanel();
        screenPanel.setBackground(ACCENT_YELLOW);
        screenPanel.setBorder(new EmptyBorder(5, 0, 5, 0));
        JLabel screenLabel = new JLabel("🎬 SCREEN 🎬");
        screenLabel.setFont(MODERN_FONT_TITLE_20);
        screenLabel.setForeground(FOREGROUND_DARK); 
        screenPanel.add(screenLabel);

        // --- Center Panel - Seats ---
        seatsPanel = new JPanel(new GridLayout(ROWS, COLS, 5, 5));
        seatsPanel.setBackground(Color.WHITE);
        seatsPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(NEUTRAL_GRAY),
            "Select Seats (Green=Available, Red=Booked, Yellow=Selected)",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            MODERN_FONT_PLAIN_14,
            FOREGROUND_DARK
        ));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 15, 0, 15));
        centerPanel.setBackground(BACKGROUND_LIGHT);
        centerPanel.add(screenPanel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(seatsPanel);
        scrollPane.setBorder(null); 
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Panel - Booking ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        // Total Label (Yellow Accent)
        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(MODERN_FONT_TITLE_20);
        totalLabel.setForeground(ACCENT_YELLOW);
        bottomPanel.add(totalLabel);

        // Book Button (Yellow Accent)
        JButton bookBtn = new JButton("Book Tickets");
        styleModernButton(bookBtn, ACCENT_YELLOW, FOREGROUND_DARK); 
        bookBtn.addActionListener(e -> bookTickets());
        bottomPanel.add(bookBtn);

        // View Bookings Button (Subtle Gray/Neutral style)
        JButton viewBookingsBtn = new JButton("View Bookings");
        styleModernButton(viewBookingsBtn, NEUTRAL_GRAY, FOREGROUND_DARK); 
        viewBookingsBtn.addActionListener(e -> viewBookings());
        bottomPanel.add(viewBookingsBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    // --- Data Loading (Unchanged) ---
    private void loadMovies() {
        ArrayList<String> movies = dbManager.getMovies();
        for (String movie : movies) {
            movieCombo.addItem(movie);
        }
    }

    private void loadTheatres() {
        ArrayList<String> theatres = dbManager.getTheatres();
        for (String theatre : theatres) {
            theatreCombo.addItem(theatre);
        }
    }

    private void loadDates() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().plusDays(i);
            dateCombo.addItem(date.format(formatter));
        }
    }
    
    // --- Seat Logic (Visibility Fixed) ---
    private void loadSeats() {
        if (movieCombo.getSelectedItem() == null || theatreCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select movie and theatre first!");
            return;
        }
        seatsPanel.removeAll();
        seatButtons = new JButton[ROWS][COLS];
        selectedSeats.clear();
        updateTotal();

        String movie = (String) movieCombo.getSelectedItem();
        String theatre = ((String) theatreCombo.getSelectedItem()).split(" - ")[0]; 
        String date = (String) dateCombo.getSelectedItem();

        ArrayList<String> bookedSeats = dbManager.getBookedSeats(movie, theatre, date);

        for (int i = 0; i < ROWS; i++) {
            char row = (char) ('A' + i);
            for (int j = 0; j < COLS; j++) {
                int seatNum = j + 1;
                String seatLabel = row + String.valueOf(seatNum);
                JButton seatBtn = new JButton(seatLabel);
                seatBtn.setPreferredSize(new Dimension(60, 40));
                seatBtn.setFont(MODERN_FONT_PLAIN_14);
                seatBtn.setBorder(new LineBorder(new Color(220, 220, 220))); 
                seatBtn.setFocusPainted(false);
                
                // Universal Black text for max legibility
                seatBtn.setForeground(FOREGROUND_DARK); 

                if (bookedSeats.contains(seatLabel)) {
                    // Booked Seat (Red)
                    seatBtn.setBackground(ERROR_RED);
                    seatBtn.setEnabled(false);
                } else {
                    // Available Seat (Green)
                    seatBtn.setBackground(AVAILABLE_GREEN);
                    seatBtn.addActionListener(e -> toggleSeat(seatBtn, seatLabel));
                }

                seatButtons[i][j] = seatBtn;
                seatsPanel.add(seatBtn);
            }
        }
        seatsPanel.revalidate();
        seatsPanel.repaint();
    }

    private void toggleSeat(JButton btn, String seatLabel) {
        if (selectedSeats.contains(seatLabel)) {
            // Deselect: Revert to Available (Green, Black Text)
            selectedSeats.remove(seatLabel);
            btn.setBackground(AVAILABLE_GREEN);
        } else {
            // Select: Set to Yellow (Yellow, Black Text)
            selectedSeats.add(seatLabel);
            btn.setBackground(ACCENT_YELLOW);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = selectedSeats.size() * TICKET_PRICE;
        totalLabel.setText(String.format("Total: ₹%.2f", total));
    }

    private void bookTickets() {
        if (selectedSeats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one seat!");
            return;
        }
        
        // Input fields use the modern font
        JTextField nameField = new JTextField();
        nameField.setFont(MODERN_FONT_PLAIN_14);
        JTextField phoneField = new JTextField();
        phoneField.setFont(MODERN_FONT_PLAIN_14);
        JTextField emailField = new JTextField();
        emailField.setFont(MODERN_FONT_PLAIN_14);

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:")).setFont(MODERN_FONT_PLAIN_14);
        panel.add(nameField);
        panel.add(new JLabel("Phone:")).setFont(MODERN_FONT_PLAIN_14);
        panel.add(phoneField);
        panel.add(new JLabel("Email:")).setFont(MODERN_FONT_PLAIN_14);
        panel.add(emailField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Enter Customer Details", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Phone are required!");
                return;
            }

            String movie = (String) movieCombo.getSelectedItem();
            String theatre = ((String) theatreCombo.getSelectedItem()).split(" - ")[0];
            String date = (String) dateCombo.getSelectedItem();

            boolean success = dbManager.bookTickets(movie, theatre, date, selectedSeats, name, phone);

            if (success) {
                JOptionPane.showMessageDialog(this, "✅ Booking Successful!\n\n"
                    + "Movie: " + movie + "\n"
                    + "Theatre: " + theatre + "\n"
                    + "Date: " + date + "\n"
                    + "Seats: " + String.join(", ", selectedSeats) + "\n"
                    + "Total: ₹" + String.format("%.2f", selectedSeats.size() * TICKET_PRICE) + "\n\n"
                    + "Customer: " + name, 
                    "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                loadSeats();
            } else {
                JOptionPane.showMessageDialog(this, "❌ Booking Failed! Please try again.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewBookings() {
        ArrayList<String[]> bookings = dbManager.getAllBookings();
        if (bookings.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No bookings found!");
            return;
        }

        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Movie", "Theatre", "Date", "Seat", "Customer", "Phone"}, 0);
        for (String[] booking : bookings) {
            model.addRow(booking);
        }

        JTable table = new JTable(model);
        table.setFont(MODERN_FONT_PLAIN_14);
        table.getTableHeader().setFont(MODERN_FONT_BOLD_16);
        table.setRowHeight(28);
        
        // Clean table styling (using yellow accent for header)
        table.getTableHeader().setBackground(ACCENT_YELLOW);
        table.getTableHeader().setForeground(FOREGROUND_DARK); // Black text on yellow header
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 500));
        scrollPane.setBorder(new LineBorder(new Color(200, 200, 200)));

        JOptionPane.showMessageDialog(this, scrollPane, "All Bookings", JOptionPane.INFORMATION_MESSAGE);
    }

    // This main method is intentionally removed/commented out here
    // as the entry point is now LoginFrame.java

    /*
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginFrame()); 
    }
    */
}