package DAO;// package ...

import java.sql.*;
import java.util.*;

import config.DBconfig;
import model.Driver;


public class DriverDAO {
    // Database connection details
    private static final String URL = DBconfig.DB_URL;
    private static final String USER = DBconfig.DB_USER;
    private static final String PASSWORD = DBconfig.DB_PASSWORD;

    //INSTANCE
    private static volatile DriverDAO INSTANCE;
    public DriverDAO() {}
    public static DriverDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DriverDAO();
                }
            }
        }
        return INSTANCE;
    }

    // insert a new driver into database
    public boolean insertDriver(Driver driver) throws SQLException {
        String sql = "INSERT INTO driver (user_id, schedule_mon, schedule_tue, schedule_wed, " +
                "schedule_thu, schedule_fri, schedule_sat, schedule_sun) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driver.getUser_id());
            stmt.setString(2, driver.getSchedule_mon());
            stmt.setString(3, driver.getSchedule_tue());
            stmt.setString(4, driver.getSchedule_wed());
            stmt.setString(5, driver.getSchedule_thu());
            stmt.setString(6, driver.getSchedule_fri());
            stmt.setString(7, driver.getSchedule_sat());
            stmt.setString(8, driver.getSchedule_sun());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    // Delete a driver by driver_id
    public boolean deleteDriver(int driverId) throws SQLException {
        String sql = "DELETE FROM driver WHERE driver_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driverId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;  // true -> delete successful. false -> driver not found
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<Driver> getAllDrivers() throws SQLException {

        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT driver_id, user_id, schedule_mon, schedule_tue, schedule_wed, schedule_thu, schedule_fri, schedule_sat, schedule_sun FROM driver";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Driver d = new Driver();
                d.setDriver_id(rs.getInt("driver_id"));
                d.setUser_id(rs.getInt("user_id"));
                d.setSchedule_mon(rs.getString("schedule_mon"));
                d.setSchedule_tue(rs.getString("schedule_tue"));
                d.setSchedule_wed(rs.getString("schedule_wed"));
                d.setSchedule_thu(rs.getString("schedule_thu"));
                d.setSchedule_fri(rs.getString("schedule_fri"));
                d.setSchedule_sat(rs.getString("schedule_sat"));
                d.setSchedule_sun(rs.getString("schedule_sun"));
                drivers.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return drivers;
    }


    // get driver's scheduleString based on driverId
    // get driver's weekly schedule as a List<String>
    public List<String> getDriverSchedulesByDriverID(int driverId) throws SQLException {
        String sql = "SELECT schedule_mon, schedule_tue, schedule_wed, schedule_thu, " +
                "schedule_fri, schedule_sat, schedule_sun " +
                "FROM driver WHERE driver_id = ?";
        List<String> scheduleList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    scheduleList.add(rs.getString("schedule_mon"));
                    scheduleList.add(rs.getString("schedule_tue"));
                    scheduleList.add(rs.getString("schedule_wed"));
                    scheduleList.add(rs.getString("schedule_thu"));
                    scheduleList.add(rs.getString("schedule_fri"));
                    scheduleList.add(rs.getString("schedule_sat"));
                    scheduleList.add(rs.getString("schedule_sun"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return scheduleList;
    }
    public List<String> getDriverSchedulesByUserID(int userId) throws SQLException {
        String sql = "SELECT schedule_mon, schedule_tue, schedule_wed, schedule_thu, schedule_fri, schedule_sat, schedule_sun " +
                "FROM driver WHERE user_id = ?";
        List<String> scheduleList = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    scheduleList.add(rs.getString("schedule_mon"));
                    scheduleList.add(rs.getString("schedule_tue"));
                    scheduleList.add(rs.getString("schedule_wed"));
                    scheduleList.add(rs.getString("schedule_thu"));
                    scheduleList.add(rs.getString("schedule_fri"));
                    scheduleList.add(rs.getString("schedule_sat"));
                    scheduleList.add(rs.getString("schedule_sun"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return scheduleList;
    }

    // get driver_id based on user_id
    public int getDriverIDByUserID(int userId) throws SQLException {
        String sql = "SELECT driver_id FROM driver WHERE user_id = ?";
        int driverId = -1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    driverId = rs.getInt("driver_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return driverId;
    }

    // get driver_id based on user_id
    public int getUserIdByDriverId(int driverId) throws SQLException {
        String sql = "SELECT user_id FROM driver WHERE driver_id = ?";
        int user_id = -1;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driverId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user_id = rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return user_id;
    }

    // updateDriverSchedule: Based on the input "scheduleString", compare the new and old "scheduleString" to determine if the changes are valid.
    // If they are valid, then store them in the database.
    public boolean updateDriverSchedules(int driverId, List<String> schedules) throws SQLException {
        if (schedules == null || schedules.size() != 7) {
            System.out.println("Invalid schedules format: must contain 7 elements (Mon ~ Sun).");
            return false;
        }

        for (int i = 0; i < schedules.size(); i++) {
            String s = schedules.get(i);
            if (s == null || s.length() != 48) {
                System.out.println("Invalid schedule format at index " + i + ": must be 48 characters long.");
                return false;
            }
        }

        String selectSql = "SELECT schedule_mon, schedule_tue, schedule_wed, schedule_thu, " +
                "schedule_fri, schedule_sat, schedule_sun " +
                "FROM driver WHERE driver_id = ?";
        String updateSql = "UPDATE driver SET schedule_mon = ?, schedule_tue = ?, schedule_wed = ?, " +
                "schedule_thu = ?, schedule_fri = ?, schedule_sat = ?, schedule_sun = ? " +
                "WHERE driver_id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            // old values
            selectStmt.setInt(1, driverId);
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Driver not found with id: " + driverId);
                    return false;
                }

                boolean isSame = true;
                for (int i = 0; i < 7; i++) {
                    String oldVal = rs.getString(i + 1); // ResultSet 的列下标从 1 开始
                    String newVal = schedules.get(i);
                    if ((oldVal == null && newVal != null) ||
                            (oldVal != null && !oldVal.equals(newVal))) {
                        isSame = false;
                        break;
                    }
                }

                if (isSame) {
                    System.out.println("New schedules are same as old schedules, no update performed.");
                    return false;
                }
            }

            // update to new values
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                for (int i = 0; i < 7; i++) {
                    updateStmt.setString(i + 1, schedules.get(i));
                }
                updateStmt.setInt(8, driverId);

                int rowsAffected = updateStmt.executeUpdate();
                return rowsAffected > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /*
    public static void main(String[] args) {
        System.out.println("=== DriverDAO Testing Started ===\n");

        DriverDAO dao = DriverDAO.getInstance();

        try {
            // 1. Test getting all drivers
            System.out.println("1. Testing getAllDrivers():");
            List<Driver> allDrivers = dao.getAllDrivers();
            System.out.println("Total drivers in database: " + allDrivers.size());
            for (Driver driver : allDrivers) {
                System.out.println("  Driver ID: " + driver.getDriver_id() +
                        ", User ID: " + driver.getUser_id());
            }
            System.out.println();

            // 2. Test getting specific driver's schedule
            System.out.println("2. Testing getDriverSchedules(1):");
            List<String> currentSchedules = dao.getDriverSchedules(1);
            if (!currentSchedules.isEmpty()) {
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                for (int i = 0; i < currentSchedules.size(); i++) {
                    String schedule = currentSchedules.get(i);
                    System.out.println("  " + days[i] + ": " +
                            (schedule != null ? schedule.substring(0, Math.min(20, schedule.length())) + "..."
                                    : "null"));
                }
            } else {
                System.out.println("  Driver with ID 1 not found");
            }
            System.out.println();
            // 3. Test updating driver schedule
            System.out.println("3. Testing updateDriverSchedules(1, newSchedules):");
            List<String> newSchedules = Arrays.asList(
                    "000000000000000011111111111111111100000000000000", // Monday: 8:00-16:00 working
                    "000000000000000011111111111111111100000000000000", // Tuesday: 8:00-16:00 working
                    "000000000000000011111111111111111100000000000000", // Wednesday: 8:00-16:00 working
                    "000000000000000011111111111111111100000000000000", // Thursday: 8:00-16:00 working
                    "000000000000000011111111111111111100000000000000", // Friday: 8:00-16:00 working
                    "000000000000000000000000000000000000000000000000", // Saturday: rest
                    "000000000000000000000000000000000000000000000000"  // Sunday: rest
            );

            boolean updateResult = dao.updateDriverSchedules(1, newSchedules);
            System.out.println("  Update result: " + (updateResult ? "Success" : "Failed or no change"));
            System.out.println();

            // 4. Verify the updated schedule
            System.out.println("4. Verifying updated schedule:");
            List<String> updatedSchedules = dao.getDriverSchedules(1);
            if (!updatedSchedules.isEmpty()) {
                String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                for (int i = 0; i < updatedSchedules.size(); i++) {
                    String schedule = updatedSchedules.get(i);
                    System.out.println("  " + days[i] + ": " +
                            (schedule != null ? schedule.substring(0, Math.min(20, schedule.length())) + "..."
                                    : "null"));
                }
            }
            System.out.println();

            // 5. Test duplicate update (should return false)
            System.out.println("5. Testing duplicate update with same schedule:");
            boolean duplicateUpdateResult = dao.updateDriverSchedules(1, newSchedules);
            System.out.println("  Duplicate update result: " + (duplicateUpdateResult ? "Success" : "Failed or no change (expected)"));
            System.out.println();
            // 6. Test inserting new driver
            System.out.println("6. Testing insertDriver():");
            Driver newDriver = new Driver();
            newDriver.setUser_id(999); // Assume user ID 999 exists
            newDriver.setSchedule_mon("111111111111111100000000000000000000000000000000"); // 0:00-8:00 working
            newDriver.setSchedule_tue("111111111111111100000000000000000000000000000000");
            newDriver.setSchedule_wed("111111111111111100000000000000000000000000000000");
            newDriver.setSchedule_thu("111111111111111100000000000000000000000000000000");
            newDriver.setSchedule_fri("111111111111111100000000000000000000000000000000");
            newDriver.setSchedule_sat("000000000000000000000000000000000000000000000000");
            newDriver.setSchedule_sun("000000000000000000000000000000000000000000000000");

            try {
                boolean insertResult = dao.insertDriver(newDriver);
                System.out.println("  Insert new driver result: " + (insertResult ? "Success" : "Failed"));
            } catch (SQLException e) {
                System.out.println("  Insert failed (possibly foreign key constraint): " + e.getMessage());
            }
            System.out.println();

            // 7. Check all drivers after insertion
            System.out.println("7. Checking all drivers after insertion:");
            List<Driver> finalDrivers = dao.getAllDrivers();
            System.out.println("Total drivers in database now: " + finalDrivers.size());
            for (Driver driver : finalDrivers) {
                System.out.println("  Driver ID: " + driver.getDriver_id() +
                        ", User ID: " + driver.getUser_id());
            }
            System.out.println();

            // 8. Test deleting newly inserted driver (if insertion succeeded)
            if (finalDrivers.size() > 1) {
                System.out.println("8. Testing deleteDriver():");
                // Assume the newly inserted driver has the maximum ID
                int maxDriverId = finalDrivers.stream()
                        .mapToInt(Driver::getDriver_id)
                        .max()
                        .orElse(-1);

                if (maxDriverId > 1) { // Ensure we don't delete the original data
                    boolean deleteResult = dao.deleteDriver(maxDriverId);
                    System.out.println("  Delete driver ID " + maxDriverId + " result: " +
                            (deleteResult ? "Success" : "Failed"));
                }
            }

            // 9. Test edge cases
            System.out.println("9. Testing edge cases:");

            // Test non-existent driver ID
            List<String> notFoundSchedule = dao.getDriverSchedules(9999);
            System.out.println("  Query non-existent driver ID 9999: " +
                    (notFoundSchedule.isEmpty() ? "Correctly returned empty list" : "Incorrectly returned data"));

            // Test invalid schedule format
            List<String> invalidSchedules = Arrays.asList(
                    "12345", // Wrong length
                    "000000000000000011111111111111111100000000000000",
                    "000000000000000011111111111111111100000000000000",
                    "000000000000000011111111111111111100000000000000",
                    "000000000000000011111111111111111100000000000000",
                    "000000000000000011111111111111111100000000000000",
                    "000000000000000011111111111111111100000000000000"
            );
            boolean invalidUpdateResult = dao.updateDriverSchedules(1, invalidSchedules);
            System.out.println("  Update with invalid schedule format: " +
                    (invalidUpdateResult ? "Error: Should fail but succeeded" : "Correct: Validation failed"));

        } catch (SQLException e) {
            System.err.println("Database operation error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Other error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== DriverDAO Testing Completed ===");
    }
    */
}



