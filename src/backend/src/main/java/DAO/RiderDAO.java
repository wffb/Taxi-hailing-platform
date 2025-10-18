package DAO;

import java.sql.*;
import java.time.Duration;
import java.util.*;

import config.DBconfig;
import model.Ride;
import model.Rider;
import model.User;
import org.jetbrains.annotations.NotNull;

public class RiderDAO {
    // Database connection details
    private static final String URL = DBconfig.DB_URL;
    private static final String USER = DBconfig.DB_USER;
    private static final String PASSWORD = DBconfig.DB_PASSWORD;

    // Single instance
    private static volatile RiderDAO INSTANCE;
    public RiderDAO() {}
    public static RiderDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (RiderDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RiderDAO();
                }
            }
        }
        return INSTANCE;
    }

    // Add a new Rider based on user_id
    public void addRider(@NotNull Rider rider) throws SQLException {
        String sql = "INSERT INTO \"rider\" (user_id) VALUES (?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, rider.getUser_id());
            stmt.executeUpdate();

            // Retrieve generated rider_id (optional)
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int riderId = rs.getInt(1);
                    System.out.println("New Rider created with rider_id: " + riderId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Delete Rider by user_id
    public void deleteRider(int userId) throws SQLException {
        String sql = "DELETE FROM \"rider\" WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Rider with user_id " + userId + " deleted.");
            } else {
                System.out.println("No Rider found for user_id " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // Get all riders
    public List<Rider> getAllRiders() throws SQLException {
        String sql = "SELECT rider_id, user_id FROM \"rider\"";
        List<Rider> riders = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Rider rider = new Rider();
                rider.setRider_id(rs.getInt("rider_id"));
                rider.setUser_id(rs.getInt("user_id"));
                riders.add(rider);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return riders;
    }

    public int getRiderIDByUserID(int userId) throws SQLException {
        String sql = "SELECT rider_id FROM rider WHERE user_id = ?";
        int driverId = -1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    driverId = rs.getInt("rider_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return driverId;
    }


    // Get a rider by rider_id
    public Rider getRiderById(int riderId) throws SQLException{
        String sql = "SELECT rider_id, user_id FROM \"rider\" where rider_id = ?";
        Rider rider = new Rider();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, riderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRide(rs);
                }
            }
        }
        return rider;
    }
/*
    public static void main(String[] args) {
        RiderDAO riderDAO = RiderDAO.getInstance();
        System.out.println("=== RiderDAO Function Testing Started ===\n");
        System.out.println("Note: Existing data - Rider IDs: 1,2,3 with User IDs: 2,4,1 respectively");
        System.out.println("rider_id is unique and auto-generated\n");

        try {
            // Test data variables
            List<Integer> newRiderIds = new ArrayList<>(); // Store IDs of newly created test riders

            // 1. Test getting all riders (show existing data)
            System.out.println("1. Testing getAllRiders() - Show existing data...");
            List<Rider> initialRiders = riderDAO.getAllRiders();
            System.out.println("Initial number of riders: " + initialRiders.size());

            System.out.println("Existing riders in database:");
            for (Rider rider : initialRiders) {
                System.out.println("  - Rider ID: " + rider.getRider_id() +
                        ", User ID: " + rider.getUser_id());
            }

            // 2. Test adding new riders with unused user_ids
            System.out.println("\n2. Testing addRider() with new user_ids...");

            // Add rider with user_id = 3 (assuming this user exists but no rider yet)
            try {
                Rider rider3 = new Rider();
                rider3.setUser_id(3);

                riderDAO.addRider(rider3);
                System.out.println("Add rider with user_id=3: SUCCESS");
            } catch (SQLException e) {
                System.out.println("Add rider with user_id=3: FAILED - " + e.getMessage());
            }

            // Add rider with user_id = 5 (test with higher user_id)
            try {
                Rider rider5 = new Rider();
                rider5.setUser_id(5);

                riderDAO.addRider(rider5);
                System.out.println("Add rider with user_id=5: SUCCESS");
            } catch (SQLException e) {
                System.out.println("Add rider with user_id=5: FAILED - " + e.getMessage());
            }

            // 3. Test adding duplicate rider (existing user_id)
            System.out.println("\n3. Testing addRider() with existing user_id...");
            try {
                Rider duplicateRider = new Rider();
                duplicateRider.setUser_id(1); // user_id=1 already exists (rider_id=3)

                riderDAO.addRider(duplicateRider);
                System.out.println("Add duplicate rider (user_id=1): SUCCESS (Multiple riders per user allowed)");
            } catch (SQLException e) {
                System.out.println("Add duplicate rider (user_id=1): FAILED (Unique constraint on user_id) - " + e.getMessage());
            }

            try {
                Rider duplicateRider2 = new Rider();
                duplicateRider2.setUser_id(2); // user_id=2 already exists (rider_id=1)

                riderDAO.addRider(duplicateRider2);
                System.out.println("Add duplicate rider (user_id=2): SUCCESS (Multiple riders per user allowed)");
            } catch (SQLException e) {
                System.out.println("Add duplicate rider (user_id=2): FAILED (Unique constraint on user_id) - " + e.getMessage());
            }

            // 4. Test getAllRiders() after additions
            System.out.println("\n4. Testing getAllRiders() after additions...");
            List<Rider> ridersAfterAdd = riderDAO.getAllRiders();
            System.out.println("Number of riders after additions: " + ridersAfterAdd.size());

            System.out.println("All riders after additions:");
            for (Rider rider : ridersAfterAdd) {
                System.out.println("  - Rider ID: " + rider.getRider_id() +
                        ", User ID: " + rider.getUser_id());

                // Track newly created riders (rider_id > 3)
                if (rider.getRider_id() > 3) {
                    newRiderIds.add(rider.getRider_id());
                }
            }

            // 5. Test adding rider with non-existent user_id
            System.out.println("\n5. Testing addRider() with non-existent user_id...");
            try {
                Rider invalidRider = new Rider();
                invalidRider.setUser_id(99999); // Assuming this user doesn't exist

                riderDAO.addRider(invalidRider);
                System.out.println("Add rider with invalid user_id=99999: SUCCESS (No foreign key constraint)");
            } catch (SQLException e) {
                System.out.println("Add rider with invalid user_id=99999: FAILED (Foreign key constraint exists) - " + e.getMessage());
            }

            // 6. Test deleteRider() with existing user_id (that we won't restore)
            System.out.println("\n6. Testing deleteRider() with existing user_id...");

            // Test deleting a newly created rider first (if any)
            if (!newRiderIds.isEmpty()) {
                // Find user_id for the first new rider
                int testDeleteUserId = -1;
                for (Rider rider : ridersAfterAdd) {
                    if (newRiderIds.contains(rider.getRider_id())) {
                        testDeleteUserId = rider.getUser_id();
                        break;
                    }
                }

                if (testDeleteUserId != -1) {
                    riderDAO.deleteRider(testDeleteUserId);
                    System.out.println("Delete newly created rider with user_id=" + testDeleteUserId + ": Executed");

                    // Verify deletion
                    List<Rider> ridersAfterDelete = riderDAO.getAllRiders();
                    System.out.println("Number of riders after deletion: " + ridersAfterDelete.size());

                    boolean found = false;
                    for (Rider rider : ridersAfterDelete) {
                        if (rider.getUser_id() == testDeleteUserId) {
                            found = true;
                            break;
                        }
                    }
                    System.out.println("Verification: Rider with user_id=" + testDeleteUserId +
                            " " + (found ? "still EXISTS" : "successfully DELETED"));
                }
            }

            // 7. Test deleteRider() with non-existent user_id
            System.out.println("\n7. Testing deleteRider() with non-existent user_id...");
            riderDAO.deleteRider(88888); // Try to delete non-existent rider
            System.out.println("Delete rider with user_id=88888: Executed (should show 'No Rider found' message)");

            // 8. Test boundary values
            System.out.println("\n8. Testing boundary values...");
            try {
                // Test with user_id = 0
                Rider boundaryRider1 = new Rider();
                boundaryRider1.setUser_id(0);
                riderDAO.addRider(boundaryRider1);
                System.out.println("Add rider with user_id=0: SUCCESS");
            } catch (SQLException e) {
                System.out.println("Add rider with user_id=0: FAILED - " + e.getMessage());
            }

            try {
                // Test with negative user_id
                Rider boundaryRider2 = new Rider();
                boundaryRider2.setUser_id(-1);
                riderDAO.addRider(boundaryRider2);
                System.out.println("Add rider with user_id=-1: SUCCESS");
            } catch (SQLException e) {
                System.out.println("Add rider with user_id=-1: FAILED - " + e.getMessage());
            }

            // 9. Cleanup - Only delete test riders we created
            System.out.println("\n9. Cleanup - Deleting only test riders...");
            System.out.println("Note: Preserving original riders (rider_ids: 1,2,3)");

            // Get current state and delete only riders we created during testing
            List<Rider> currentRiders = riderDAO.getAllRiders();
            for (Rider rider : currentRiders) {
                // Delete riders created during this test (keep original 3)
                if (rider.getUser_id() == 3 || rider.getUser_id() == 5 ||
                        rider.getUser_id() == 99999 || rider.getUser_id() == 0 ||
                        rider.getUser_id() == -1) {

                    riderDAO.deleteRider(rider.getUser_id());
                    System.out.println("Cleanup: Delete test rider with user_id=" + rider.getUser_id());
                }
            }

            // 10. Final state check
            System.out.println("\n10. Final database state check...");
            List<Rider> finalRiders = riderDAO.getAllRiders();
            System.out.println("Final number of riders in database: " + finalRiders.size());

            System.out.println("Final riders in database:");
            for (Rider rider : finalRiders) {
                System.out.println("  - Rider ID: " + rider.getRider_id() +
                        ", User ID: " + rider.getUser_id());
            }

            System.out.println("\nExpected: Original 3 riders should remain (rider_ids: 1,2,3 with user_ids: 2,4,1)");
            System.out.println("Test riders have been cleaned up");

            System.out.println("\n=== RiderDAO Function Testing Completed ===");

        } catch (SQLException e) {
            System.err.println("Database operation exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Other exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/
    private @NotNull Rider mapResultSetToRide(ResultSet rs) throws SQLException {
        Rider rider = new Rider();
        rider.setUser_id(rs.getInt("user_id"));
        rider.setRider_id(rs.getInt("rider_id"));


//        Ride ride = new Ride();
//        ride.setRide_id(rs.getInt("ride_id"));
//        ride.setDriver_id(rs.getInt("driver_id"));
//        ride.setRider_id(rs.getInt("rider_id"));
//        ride.setPickup_location(rs.getString("pickup_location"));
//        ride.setDestination(rs.getString("destination"));
//        ride.setEstimate_fare(rs.getDouble("estimate_fare"));
//        ride.setActual_fare(rs.getDouble("actual_fare"));
//        ride.setRide_state(rs.getString("ride_state"));
//        ride.setStart_time(rs.getTimestamp("start_time") != null ?
//                rs.getTimestamp("start_time").toLocalDateTime() : null);
//
//        long seconds = rs.getLong("required_time"); // required_time indicates the seconds
//        ride.setRequired_time(Duration.ofSeconds(seconds));

        return rider;
    }
}