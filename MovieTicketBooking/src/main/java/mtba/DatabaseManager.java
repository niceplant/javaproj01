package mtba;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private Connection conn;

    public DatabaseManager() {
        initDatabase();
    }

    private void initDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:movie_booking.db");
            System.out.println("Database connected successfully!");
            createTables();
            insertSampleData();
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Movies table
        stmt.execute("CREATE TABLE IF NOT EXISTS movies (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "genre TEXT, " +
                "duration INTEGER, " +
                "rating TEXT)");

        // Theatres table
        stmt.execute("CREATE TABLE IF NOT EXISTS theatres (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL UNIQUE, " +
                "location TEXT, " +
                "total_seats INTEGER)");

        // Bookings table
        stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "movie_id INTEGER, " +
                "theatre_id INTEGER, " +
                "booking_date TEXT, " +
                "seat_number TEXT, " +
                "customer_name TEXT, " +
                "phone TEXT, " +
                "booking_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(movie_id) REFERENCES movies(id), " +
                "FOREIGN KEY(theatre_id) REFERENCES theatres(id))");

        stmt.close();
        System.out.println("Tables created successfully!");
    }

    private void insertSampleData() throws SQLException {
        Statement checkStmt = conn.createStatement();
        ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM movies");
        rs.next();
        if (rs.getInt(1) > 0) {
            rs.close();
            checkStmt.close();
            System.out.println("Sample data already exists.");
            return;
        }
        rs.close();
        checkStmt.close();

        // Insert sample movies
        PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO movies (name, genre, duration, rating) VALUES (?, ?, ?, ?)");
        
        String[][] movies = {
            {"The Adventure Begins", "Action", "150", "PG-13"},
            {"Love in Paris", "Romance", "120", "PG"},
            {"The Mystery Manor", "Thriller", "135", "R"},
            {"Cosmic Journey", "Sci-Fi", "160", "PG-13"},
            {"Comedy Nights", "Comedy", "110", "PG"},
            {"Dark Secrets", "Horror", "125", "R"}
        };

        for (String[] movie : movies) {
            pstmt.setString(1, movie[0]);
            pstmt.setString(2, movie[1]);
            pstmt.setInt(3, Integer.parseInt(movie[2]));
            pstmt.setString(4, movie[3]);
            pstmt.executeUpdate();
        }

        // Insert sample theatres
        pstmt = conn.prepareStatement(
                "INSERT INTO theatres (name, location, total_seats) VALUES (?, ?, ?)");
        
        String[][] theatres = {
            {"PVR Cinemas", "Mall Road", "80"},
            {"INOX Theatre", "City Center", "80"},
            {"Cinepolis", "Downtown Plaza", "80"},
            {"Carnival Cinemas", "Metro Station", "80"}
        };

        for (String[] theatre : theatres) {
            pstmt.setString(1, theatre[0]);
            pstmt.setString(2, theatre[1]);
            pstmt.setInt(3, Integer.parseInt(theatre[2]));
            pstmt.executeUpdate();
        }

        pstmt.close();
        System.out.println("Sample data inserted successfully!");
    }

    public ArrayList<String> getMovies() {
        ArrayList<String> movies = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, genre, duration FROM movies ORDER BY name");
            while (rs.next()) {
                movies.add(rs.getString("name") + " (" + rs.getString("genre") + ", " + 
                          rs.getInt("duration") + " min)");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading movies: " + e.getMessage());
            e.printStackTrace();
        }
        return movies;
    }

    public ArrayList<String> getTheatres() {
        ArrayList<String> theatres = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, location FROM theatres ORDER BY name");
            while (rs.next()) {
                theatres.add(rs.getString("name") + " - " + rs.getString("location"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading theatres: " + e.getMessage());
            e.printStackTrace();
        }
        return theatres;
    }

    public ArrayList<String> getBookedSeats(String movieInfo, String theatre, String date) {
        ArrayList<String> bookedSeats = new ArrayList<>();
        try {
            // Extract movie name from the combo box format
            String movieName = movieInfo.split(" \\(")[0];
            
            PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT b.seat_number FROM bookings b " +
                    "JOIN movies m ON b.movie_id = m.id " +
                    "JOIN theatres t ON b.theatre_id = t.id " +
                    "WHERE m.name = ? AND t.name = ? AND b.booking_date = ?");
            pstmt.setString(1, movieName);
            pstmt.setString(2, theatre);
            pstmt.setString(3, date);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_number"));
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading booked seats: " + e.getMessage());
            e.printStackTrace();
        }
        return bookedSeats;
    }

    public boolean bookTickets(String movieInfo, String theatre, String date, 
                               ArrayList<String> seats, String name, String phone) {
        try {
            // Extract movie name from the combo box format
            String movieName = movieInfo.split(" \\(")[0];
            
            int movieId = getId("movies", "name", movieName);
            int theatreId = getId("theatres", "name", theatre);

            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO bookings (movie_id, theatre_id, booking_date, seat_number, customer_name, phone) " +
                    "VALUES (?, ?, ?, ?, ?, ?)");

            for (String seat : seats) {
                pstmt.setInt(1, movieId);
                pstmt.setInt(2, theatreId);
                pstmt.setString(3, date);
                pstmt.setString(4, seat);
                pstmt.setString(5, name);
                pstmt.setString(6, phone);
                pstmt.executeUpdate();
            }

            pstmt.close();
            System.out.println("Booking successful for " + name);
            return true;
        } catch (SQLException e) {
            System.err.println("Booking error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<String[]> getAllBookings() {
        ArrayList<String[]> bookings = new ArrayList<>();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT m.name as movie, t.name as theatre, b.booking_date, " +
                    "b.seat_number, b.customer_name, b.phone " +
                    "FROM bookings b " +
                    "JOIN movies m ON b.movie_id = m.id " +
                    "JOIN theatres t ON b.theatre_id = t.id " +
                    "ORDER BY b.booking_date DESC, b.booking_time DESC");

            while (rs.next()) {
                bookings.add(new String[]{
                        rs.getString("movie"),
                        rs.getString("theatre"),
                        rs.getString("booking_date"),
                        rs.getString("seat_number"),
                        rs.getString("customer_name"),
                        rs.getString("phone")
                });
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            e.printStackTrace();
        }
        return bookings;
    }

    private int getId(String table, String column, String value) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(
                "SELECT id FROM " + table + " WHERE " + column + " = ?");
        pstmt.setString(1, value);
        ResultSet rs = pstmt.executeQuery();
        int id = rs.getInt("id");
        rs.close();
        pstmt.close();
        return id;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
