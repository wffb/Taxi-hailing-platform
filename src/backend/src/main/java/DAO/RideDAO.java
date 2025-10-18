package DAO;

import java.sql.*;
import java.time.ZoneId;
import java.util.*;
import java.time.Duration;
import java.time.LocalDateTime;

import config.DBconfig;
import model.Ride;
import org.jetbrains.annotations.NotNull;

public class RideDAO {
    // Database connection details
    private static final String URL = DBconfig.DB_URL;
    private static final String USER = DBconfig.DB_USER;
    private static final String PASSWORD = DBconfig.DB_PASSWORD;

    // Single instance
    private static volatile RideDAO INSTANCE;
    private RideDAO() {}
    public static RideDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (RideDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RideDAO();
                }
            }
        }
        return INSTANCE;
    }

    // 1. Add a new ride
    public boolean addRide(Ride ride) throws SQLException {
        String sql = "INSERT INTO ride (driver_id, rider_id, pickup_location, destination, " +
                "estimate_fare, actual_fare, ride_state, required_time, start_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ride.getDriver_id());
            stmt.setInt(2, ride.getRider_id());
            stmt.setString(3, ride.getPickup_location());
            stmt.setString(4, ride.getDestination());
            stmt.setDouble(5, ride.getEstimate_fare());
            stmt.setDouble(6, ride.getActual_fare());
            stmt.setString(7, ride.getRide_state());
            // store Duration as seconds
            stmt.setLong(8, ride.getRequired_time() != null ? ride.getRequired_time().getSeconds() : 0);
            stmt.setTimestamp(9, ride.getStart_time() != null ?
                    Timestamp.valueOf(ride.getStart_time()) : null);

            return stmt.executeUpdate() > 0;
        }
    }

    // 2. Delete a ride by ride_id (throws if not found)
    public void deleteRide(int rideId) throws SQLException {
        String sql = "DELETE FROM ride WHERE ride_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rideId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No ride found with ride_id=" + rideId);
            }
        }
    }

    // 3. Find a ride by ride_id
    public Ride getRideById(int rideId) throws SQLException {
        String sql = "SELECT * FROM ride WHERE ride_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rideId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRide(rs);
                }
            }
        }
        return null;
    }


    // 4. Find rides by rider_id
    public List<Ride> getRidesByRiderId(int riderId) throws SQLException {
        String sql = "SELECT * FROM ride WHERE rider_id = ?";
        List<Ride> rides = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, riderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rides.add(mapResultSetToRide(rs));
                }
            }
        }
        return rides;
    }

    // 5. Find rides by driver_id
    public List<Ride> getRidesByDriverId(int driverId) throws SQLException {
        String sql = "SELECT * FROM ride WHERE driver_id = ?";
        List<Ride> rides = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rides.add(mapResultSetToRide(rs));
                }
            }
        }
        return rides;
    }

    // 6. Get all rides
    public List<Ride> getAllRides() throws SQLException {
        String sql = "SELECT * FROM ride";
        List<Ride> rides = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rides.add(mapResultSetToRide(rs));
            }
        }
        return rides;
    }

    // 7. Update ride_state by ride_id
    public boolean updateRideState(int rideId, int driverId, String newState) throws SQLException {
        String sql = "UPDATE ride SET ride_state = ?, driver_id = ? WHERE ride_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newState);
            stmt.setInt(2, driverId);
            stmt.setInt(3, rideId);
            return stmt.executeUpdate() > 0;
        }
    }
    public boolean updateRideState(int rideId, String newState) throws SQLException {
        String sql = "UPDATE ride SET ride_state = ? WHERE ride_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newState);
            stmt.setInt(2, rideId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Update driver assignment and ride state atomically
    public boolean acceptRideByDriver(int rideId, int driverId) throws SQLException {
        String sql = "UPDATE ride SET driver_id = ?, ride_state = 'ACCEPTED' WHERE ride_id = ? AND ride_state = 'REQUESTED'";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, driverId);
            stmt.setInt(2, rideId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Utility method to map ResultSet -> Ride
    private @NotNull Ride mapResultSetToRide(ResultSet rs) throws SQLException {
        Ride ride = new Ride();
        ride.setRide_id(rs.getInt("ride_id"));
        ride.setDriver_id(rs.getInt("driver_id"));
        ride.setRider_id(rs.getInt("rider_id"));
        ride.setPickup_location(rs.getString("pickup_location"));
        ride.setDestination(rs.getString("destination"));
        ride.setEstimate_fare(rs.getDouble("estimate_fare"));
        ride.setActual_fare(rs.getDouble("actual_fare"));
        ride.setRide_state(rs.getString("ride_state"));
        ride.setStart_time(rs.getTimestamp("start_time") != null ?
                rs.getTimestamp("start_time").toInstant()
                        .atZone(ZoneId.of("Australia/Melbourne"))
                        .toLocalDateTime()
                : null);

        long seconds = rs.getLong("required_time"); // required_time indicates the seconds
        ride.setRequired_time(Duration.ofSeconds(seconds));

        return ride;
    }

}