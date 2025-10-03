package mtba;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class AdminFrame extends JFrame {

    private static final long serialVersionUID = 1L; 
    
    private DatabaseManager dbManager; 
    
    // Theme Constants (FIXED: Added NEUTRAL_GRAY)
    private static final Color ACCENT_YELLOW = new Color(255, 213, 0); 
    private static final Color BACKGROUND_LIGHT = new Color(245, 245, 245);
    private static final Color FOREGROUND_DARK = new Color(51, 51, 51);
    private static final Color NEUTRAL_GRAY = new Color(189, 195, 199); // FIX: Missing constant
    private static final Font MODERN_FONT_BOLD_16 = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font MODERN_FONT_PLAIN_14 = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font MODERN_FONT_TITLE_20 = new Font("Segoe UI", Font.BOLD, 20); // Not used, but kept for consistency
    
    // Input Fields for Add Movie
    private JTextField movieField, genreField, durationField, ratingField;
    
    // Input Fields for Add Theatre
    private JTextField theatreNameField, theatreLocationField, totalSeatsField;
    
    // Input Fields for Add Show (Not used, but kept for clarity on the warnings)
    private JTextField dateField, showTimeField; 

    public AdminFrame() {
        setTitle("Admin Panel - Movie/Theatre Management");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_LIGHT);
        
        dbManager = new DatabaseManager(); 
        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JLabel titleLabel = new JLabel("ADMINISTRATION: ADD MOVIES AND THEATRES");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(FOREGROUND_DARK);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(new EmptyBorder(15, 10, 10, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20)); 
        mainPanel.setBackground(BACKGROUND_LIGHT);
        mainPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- 1. Add Movie Section ---
        mainPanel.add(createMoviePanel());

        // --- 2. Add Theatre Section ---
        mainPanel.add(createTheatrePanel());
        
        // Suppress unused field warnings locally if necessary, but best practice is removal:
        // dateField = new JTextField(); // Removed, as it is unused
        // showTimeField = new JTextField(); // Removed, as it is unused

        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createMoviePanel() {
        JPanel moviePanel = new JPanel(new BorderLayout(10, 10));
        moviePanel.setBackground(Color.WHITE);
        moviePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(NEUTRAL_GRAY), // FIX: NEUTRAL_GRAY is now defined
            "Add New Movie Details", TitledBorder.LEFT, TitledBorder.TOP, MODERN_FONT_BOLD_16, FOREGROUND_DARK));

        JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        fieldsPanel.setBackground(Color.WHITE);

        movieField = addField(fieldsPanel, "Title:");
        genreField = addField(fieldsPanel, "Genre:");
        durationField = addField(fieldsPanel, "Duration (min):", true); 
        ratingField = addField(fieldsPanel, "Rating (e.g., PG-13):");
        
        moviePanel.add(fieldsPanel, BorderLayout.CENTER);

        JButton addMovieBtn = new JButton("ADD MOVIE");
        styleButton(addMovieBtn);
        addMovieBtn.addActionListener(e -> addMovie());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(addMovieBtn);
        moviePanel.add(buttonWrapper, BorderLayout.SOUTH);
        
        return moviePanel;
    }

    private JPanel createTheatrePanel() {
        JPanel theatrePanel = new JPanel(new BorderLayout(10, 10));
        theatrePanel.setBackground(Color.WHITE);
        theatrePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(NEUTRAL_GRAY), // FIX: NEUTRAL_GRAY is now defined
            "Add New Theatre Details", TitledBorder.LEFT, TitledBorder.TOP, MODERN_FONT_BOLD_16, FOREGROUND_DARK));

        JPanel fieldsPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        fieldsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        fieldsPanel.setBackground(Color.WHITE);

        theatreNameField = addField(fieldsPanel, "Theatre Name:");
        theatreLocationField = addField(fieldsPanel, "Location:");
        totalSeatsField = addField(fieldsPanel, "Total Seats (e.g., 80):", true); 

        theatrePanel.add(fieldsPanel, BorderLayout.CENTER);

        JButton addTheatreBtn = new JButton("ADD THEATRE");
        styleButton(addTheatreBtn);
        addTheatreBtn.addActionListener(e -> addTheatre());
        
        JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonWrapper.setBackground(Color.WHITE);
        buttonWrapper.add(addTheatreBtn);
        theatrePanel.add(buttonWrapper, BorderLayout.SOUTH);
        
        return theatrePanel;
    }

    // Helper to add label and field to a panel
    private JTextField addField(JPanel panel, String labelText) {
        return addField(panel, labelText, false);
    }
    
    private JTextField addField(JPanel panel, String labelText, boolean numericOnly) {
        JLabel label = new JLabel(labelText);
        label.setFont(MODERN_FONT_PLAIN_14);
        panel.add(label);
        
        JTextField field = new JTextField(15);
        field.setFont(MODERN_FONT_PLAIN_14);
        panel.add(field);
        
        // Basic input validation for numeric fields
        if (numericOnly) {
            field.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    char c = evt.getKeyChar();
                    if (!((c >= '0') && (c <= '9') || (c == java.awt.event.KeyEvent.VK_BACK_SPACE) || (c == java.awt.event.KeyEvent.VK_DELETE))) {
                        getToolkit().beep();
                        evt.consume();
                    }
                }
            });
        }
        return field;
    }

    private void styleButton(JButton button) {
        button.setFont(MODERN_FONT_BOLD_16);
        button.setBackground(ACCENT_YELLOW);
        button.setForeground(FOREGROUND_DARK);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
    }
    
    // --- ADMIN ACTION IMPLEMENTATIONS ---

    private void addMovie() {
        String title = movieField.getText().trim();
        String genre = genreField.getText().trim();
        String durationStr = durationField.getText().trim();
        String rating = ratingField.getText().trim();

        if (title.isEmpty() || genre.isEmpty() || durationStr.isEmpty() || rating.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All movie fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int duration;
        try {
            duration = Integer.parseInt(durationStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Duration must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = dbManager.addMovie(title, genre, duration, rating); 
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Movie '" + title + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            movieField.setText("");
            genreField.setText("");
            durationField.setText("");
            ratingField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add movie. Check if title is unique.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addTheatre() {
        String name = theatreNameField.getText().trim();
        String location = theatreLocationField.getText().trim();
        String seatsStr = totalSeatsField.getText().trim();

        if (name.isEmpty() || location.isEmpty() || seatsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All theatre fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int totalSeats;
        try {
            totalSeats = Integer.parseInt(seatsStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Total Seats must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = dbManager.addTheatre(name, location, totalSeats); 
        
        if (success) {
            JOptionPane.showMessageDialog(this, "Theatre '" + name + "' added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            theatreNameField.setText("");
            theatreLocationField.setText("");
            totalSeatsField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add theatre. Check if name is unique.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}