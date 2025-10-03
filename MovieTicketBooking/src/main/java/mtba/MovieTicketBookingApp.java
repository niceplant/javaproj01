package mtba;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class MovieTicketBookingApp extends JFrame {
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
        setTitle("Movie Ticket Booking System");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        selectedSeats = new ArrayList<>();
        dbManager = new DatabaseManager();
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top Panel - Selection
        JPanel topPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        topPanel.add(new JLabel("Select Movie:"));
        movieCombo = new JComboBox<>();
        loadMovies();
        topPanel.add(movieCombo);

        topPanel.add(new JLabel("Select Theatre:"));
        theatreCombo = new JComboBox<>();
        loadTheatres();
        topPanel.add(theatreCombo);

        topPanel.add(new JLabel("Select Date:"));
        dateCombo = new JComboBox<>();
        loadDates();
        topPanel.add(dateCombo);

        JButton loadSeatsBtn = new JButton("Load Seats");
        loadSeatsBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loadSeatsBtn.addActionListener(e -> loadSeats());
        topPanel.add(new JLabel());
        topPanel.add(loadSeatsBtn);

        add(topPanel, BorderLayout.NORTH);

        // Screen Label
        JPanel screenPanel = new JPanel();
        screenPanel.setBackground(new Color(230, 230, 230));
        JLabel screenLabel = new JLabel("🎬 SCREEN 🎬");
        screenLabel.setFont(new Font("Arial", Font.BOLD, 18));
        screenPanel.add(screenLabel);
        
        // Center Panel - Seats
        seatsPanel = new JPanel(new GridLayout(ROWS, COLS, 5, 5));
        seatsPanel.setBorder(BorderFactory.createTitledBorder("Select Seats (Green=Available, Red=Booked, Blue=Selected)"));
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(screenPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(seatsPanel), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel - Booking
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        totalLabel = new JLabel("Total: ₹0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(0, 128, 0));
        bottomPanel.add(totalLabel);

        JButton bookBtn = new JButton("Book Tickets");
        bookBtn.setFont(new Font("Arial", Font.BOLD, 14));
        bookBtn.setBackground(new Color(0, 128, 255));
        bookBtn.setForeground(Color.WHITE);
        bookBtn.addActionListener(e -> bookTickets());
        bottomPanel.add(bookBtn);

        JButton viewBookingsBtn = new JButton("View Bookings");
        viewBookingsBtn.setFont(new Font("Arial", Font.BOLD, 14));
        viewBookingsBtn.addActionListener(e -> viewBookings());
        bottomPanel.add(viewBookingsBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

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
                seatBtn.setFont(new Font("Arial", Font.BOLD, 12));

                if (bookedSeats.contains(seatLabel)) {
                    seatBtn.setBackground(Color.RED);
                    seatBtn.setForeground(Color.WHITE);
                    seatBtn.setEnabled(false);
                } else {
                    seatBtn.setBackground(new Color(50, 205, 50));
                    seatBtn.setForeground(Color.WHITE);
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
            selectedSeats.remove(seatLabel);
            btn.setBackground(new Color(50, 205, 50));
        } else {
            selectedSeats.add(seatLabel);
            btn.setBackground(new Color(30, 144, 255));
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

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Email:"));
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
                JOptionPane.showMessageDialog(this, 
                        "✅ Booking Successful!\n\n" +
                        "Movie: " + movie + "\n" +
                        "Theatre: " + theatre + "\n" +
                        "Date: " + date + "\n" +
                        "Seats: " + String.join(", ", selectedSeats) + "\n" +
                        "Total: ₹" + String.format("%.2f", selectedSeats.size() * TICKET_PRICE) + "\n\n" +
                        "Customer: " + name,
                        "Booking Confirmed",
                        JOptionPane.INFORMATION_MESSAGE);
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
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(900, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "All Bookings", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new MovieTicketBookingApp());
    }
}
