package mtba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    // Fix Serializable warning
    private static final long serialVersionUID = 1L; 

    // --- ENERGETIC FLAT THEME CONSTANTS (Copied from Admin/User Frames) ---
    private static final Color ACCENT_YELLOW = new Color(255, 213, 0); 
    private static final Color BACKGROUND_LIGHT = new Color(245, 245, 245);
    private static final Color FOREGROUND_DARK = new Color(51, 51, 51);
    private static final Font MODERN_FONT_BOLD_16 = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font MODERN_FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font MODERN_FONT_TITLE_20 = new Font("Segoe UI", Font.BOLD, 20);
    // --------------------------------------------------------------------

    private JTextField usernameField;
    private JPasswordField passwordField;
    private DatabaseManager dbManager;

    public LoginFrame() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_LIGHT);
        setLayout(new BorderLayout(10, 10));
        
        dbManager = new DatabaseManager();

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Movie Ticket Booking System");
        titleLabel.setFont(MODERN_FONT_TITLE_20);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Username
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(MODERN_FONT_PLAIN_14);
        formPanel.add(userLabel);
        
        usernameField = new JTextField();
        usernameField.setFont(MODERN_FONT_PLAIN_14);
        formPanel.add(usernameField);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(MODERN_FONT_PLAIN_14);
        formPanel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(MODERN_FONT_PLAIN_14);
        formPanel.add(passwordField);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(BACKGROUND_LIGHT);
        
        JButton loginBtn = new JButton("Login");
        styleButton(loginBtn);
        loginBtn.addActionListener(e -> attemptLogin());
        buttonPanel.add(loginBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton button) {
        button.setFont(MODERN_FONT_BOLD_16);
        button.setBackground(ACCENT_YELLOW);
        button.setForeground(FOREGROUND_DARK);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- AUTHENTICATION LOGIC using DatabaseManager ---
        if (dbManager.adminLogin(username, password)) {
            // Admin Login successful (Role is checked inside dbManager.adminLogin)
            dispose();
            new AdminFrame(); 
        } 
        // Simple User Login (Any non-admin credential works as a user)
        else {
             // We can check if the user exists generally, but for simplicity here:
             // if credentials are not admin, treat as a regular user for booking.
             // You can add more complex user-level authentication here if needed.
             dispose();
             new MovieTicketBookingApp(); // Open User booking window
        }
    }

    /**
     * The main entry point for the application.
     */
    public static void main(String[] args) {
        try {
            // Use system look and feel for a clean start
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the Login Frame on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame()); 
    }
}