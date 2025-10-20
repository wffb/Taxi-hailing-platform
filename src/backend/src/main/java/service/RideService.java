package service;

import DAO.DriverDAO;
import DAO.RideDAO;
import DAO.RiderDAO;
import DAO.UserDAO;
import common.util.CacheUtil;
import model.Ride;
import model.User;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class RideService {

    public RideDAO rideDAO;
    public UserDAO userDAO;
    public DriverDAO driverDAO;
    public RiderDAO riderDAO;

    private RideService() {
        rideDAO = RideDAO.getInstance();
        driverDAO = DriverDAO.getInstance();
        userDAO = UserDAO.getInstance();
        riderDAO = RiderDAO.getInstance();
    }

    // INSTANCE
    private static volatile RideService INSTANCE;


    public static RideService getInstance() {
        if (INSTANCE == null) {
            synchronized (RideService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RideService();
                }
            }
        }
        return INSTANCE;
    }

    // Service functions

    /**
     * Get all rides from database
     * @return List of all rides
     * @throws SQLException if database operation fails
     */
    public List<Ride> getAllRides() throws SQLException {
        return rideDAO.getAllRides();
    }

    /**
     * Get ride by ID
     * @param rideId the ride ID to search for
     * @return Ride object or null if not found
     * @throws SQLException if database operation fails
     */
    public Ride getRideById(int rideId) throws SQLException {
        return rideDAO.getRideById(rideId);
    }

    public List<Ride> getRidesByRiderId(int riderId) throws SQLException {
        return rideDAO.getRidesByRiderId(riderId);
    }

    public List<Ride> getRidesByDriverId(int driverId) throws SQLException {
        return rideDAO.getRidesByDriverId(driverId);
    }


    //change state of ride
    public boolean updateRideState(int rideId, int driverId, String newState) throws SQLException {

        // get current ride and check transition
        Ride ride = rideDAO.getRideById(rideId);
        if(ride == null){
            System.out.println("[ERROR] Ride not found: " + rideId);
            return false;
        }

        String currentState = ride.getRide_state();
        if(!isValidTransition(currentState, newState)){
            System.out.println("[ERROR] Invalid state transition: " + currentState + " -> " + newState);
            return false;
        }

        //get lock
        String lockId = CacheUtil.tryLock("ride:" + rideId);
        while(lockId == null){
            try {
                sleep(500);
                lockId = CacheUtil.tryLock("ride:" + rideId);

            }catch (Exception e){
                System.out.println("[ERROR] Thread interrupted:"+e.getMessage());
                return false;
            }
        }

        //lock get failed
        if(driverId == 0)
            //driver no change
            rideDAO.updateRideState(rideId, newState);
        else
            //driver change
            rideDAO.updateRideState(rideId, driverId, newState);

        //release lock
        CacheUtil.releaseLock("ride:" + rideId, lockId);

        return true;
    }


    /**
     * Accept a ride request by driver
     * @param rideId the ride to accept
     * @param driverId the driver who accepts the ride
     * @return true if acceptance successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean acceptRide(int rideId, int driverId) throws SQLException {

        // Check if ride exists and is in REQUESTED state
        Ride ride = rideDAO.getRideById(rideId);
        if (ride == null || !"REQUESTED".equals(ride.getRide_state())) {
            return false;
        }

        // Check if driver is available at start time
        if (!isDriverAvailable(driverId, ride.getStart_time())) {
            System.out.println(ride.getStart_time());
            return false;
        }

        // Update ride state to ACCEPTED
        return rideDAO.updateRideState(rideId, driverId,"ACCEPTED");
    }

    /**
     * Request a new ride
     * @param riderId the rider requesting the ride
     * @param pickupLocation pickup location code
     * @param destination destination location code
     * @return the created Ride object, null if creation failed
     * @throws SQLException if database operation fails
     */
    public Ride requestRide(int riderId, String pickupLocation, String destination) throws SQLException {

        // Validate pickup and destination are different
        if (pickupLocation.equals(destination)) {
            return null;
        }

        // Calculate estimate fare based on locations
        double estimateFare = calculateFare(pickupLocation, destination);

        // Check if rider has sufficient fund
        int userId = riderDAO.getRiderById(riderId).getUser_id();
        boolean hasSufficientFund = hasSufficientFund(userId, estimateFare);
        if(!hasSufficientFund){
            Ride ride = new Ride();
            ride.setRide_id(-1);
            return ride;
        }

        // Calculate required time (estimated based on distance)
        Duration requiredTime = calculateRequiredTime(pickupLocation, destination);

        // Set start time to current time in Melbourne
        LocalDateTime startTime = LocalDateTime.now(ZoneId.of("Australia/Melbourne"));


        // Create new ride object
        Ride newRide = new Ride();
        newRide.setDriver_id(0);
        newRide.setRider_id(riderId);
        newRide.setPickup_location(pickupLocation);
        newRide.setDestination(destination);
        newRide.setEstimate_fare(estimateFare);
        newRide.setActual_fare(estimateFare); // Same as estimate due to design issue
        newRide.setRide_state("REQUESTED");
        newRide.setRequired_time(requiredTime);
        newRide.setStart_time(startTime);

        // Add ride to database
        int res = rideDAO.addRide(newRide);

        if(res != 0){
            newRide.setRide_id(res);
            return newRide;
        }

        return null;
    }

    /**
     * Check if driver is available at specified time based on their schedule
     * @param driverId the driver to check
     * @param rideStartTime the start time to check
     * @return true if driver is available, false otherwise
     * @throws SQLException if database operation fails
     */
    private boolean isDriverAvailable(int driverId, LocalDateTime rideStartTime) throws SQLException {
        // Get the driver's weekly schedule (should have 7 entries, one per day)
        List<String> schedules = driverDAO.getDriverSchedulesByDriverID(driverId);
        if (schedules == null || schedules.size() != 7) {
            System.out.println("Schedules are null or do not contain 7 days.");
            return false;
        }

        // Get the day of the week (1 = Monday, 7 = Sunday)
        int dayOfWeek = rideStartTime.getDayOfWeek().getValue();
        int scheduleIndex = dayOfWeek - 1; // Convert to 0-based index (Monday=0, Sunday=6)

        // Safety check for index range
        if (scheduleIndex < 0 || scheduleIndex >= schedules.size()) {
            System.out.println("Invalid scheduleIndex: " + scheduleIndex);
            return false;
        }

        // Determine which time slot (0–47) this ride start time falls into
        int timeSlot = getCurrentTimeSlot(rideStartTime);
        if (timeSlot < 0 || timeSlot >= 48) {
            System.out.println("Invalid timeSlot: " + timeSlot);
            return false;
        }

        // Retrieve today's schedule string
        String todaySchedule = schedules.get(scheduleIndex);
        if (todaySchedule == null || todaySchedule.length() != 48) {
            System.out.println("Invalid schedule format: " +
                    (todaySchedule == null ? "null" : todaySchedule.length()));
            return false;
        }

        // Each character represents a 30-minute slot: '1' = available, '0' = not available
        char availability = todaySchedule.charAt(timeSlot);

        // Debug log
        System.out.println("Driver ID: " + driverId +
                ", Day index: " + scheduleIndex +
                ", Slot: " + timeSlot +
                ", Availability: " + availability);

        // Return true if available
        return availability == '1';
    }


    /**
     * Get time slot (0-47) based on specified time
     * Each slot represents 30 minutes, starting from 00:00
     * @param time the time to convert
     * @return time slot index (0-47)
     */
    private int getCurrentTimeSlot(LocalDateTime time) {
        int hour = time.getHour();
        int minute = time.getMinute();

        // Calculate 30-minute time slot
        // 00:00-00:29 = slot 0, 00:30-00:59 = slot 1, etc.
        int timeSlot = hour * 2 + (minute >= 30 ? 1 : 0);

        // Ensure within valid range (0-47)
        return Math.min(47, Math.max(0, timeSlot));
    }

    /**
     * Calculate fare based on pickup and destination locations according to zone rules
     * @param pickupLocation pickup location code
     * @param destination destination location code
     * @return calculated fare amount
     */
    private double calculateFare(String pickupLocation, String destination) {
        int pickup = Integer.parseInt(pickupLocation);
        int dest = Integer.parseInt(destination);

        // Rule 1: Airport Fare - If either location is Melbourne Airport (3045)
        if (pickup == 3045 || dest == 3045) {
            return 60.00;
        }

        // Rule 2: Interstate Fare (Zone 3) - If either location is outside Victoria (not 3xxx)
        if (pickup < 3000 || pickup >= 4000 || dest < 3000 || dest >= 4000) {
            return 500.00;
        }

        // Rule 3: Regional Fare (Zone 2) - If either location is Regional Victoria (3xxx but not 3000-3299)
        boolean pickupRegional = pickup >= 3300;
        boolean destRegional = dest >= 3300;
        if (pickupRegional || destRegional) {
            return 220.00;
        }

        // Rule 4: Metro Fare (Zone 1) - Both locations in Metro Melbourne (3000-3299)
        return 40.00;
    }

    /**
     * Calculate required time based on pickup and destination locations
     * @param pickupLocation pickup location code
     * @param destination destination location code
     * @return estimated duration for the trip
     */
    private Duration calculateRequiredTime(String pickupLocation, String destination) {
        // Simple time calculation based on location codes
        int pickup = Integer.parseInt(pickupLocation);
        int dest = Integer.parseInt(destination);
        long baseMinutes = 15; // Base travel time
        long distanceMinutes = Math.abs(dest - pickup) / 100; // no actual logic in this :)
        return Duration.ofMinutes(baseMinutes + distanceMinutes);
    }

    private boolean hasSufficientFund(int userId, double estimateFare) throws SQLException{
        double balance = userDAO.getUserById(userId).getWallet_balance().doubleValue();
        return balance >= estimateFare;
    }


    /**
     * Validate whether a state transition is allowed.
     * @param currentState current state of the ride
     * @param newState target state to transition to
     * @return true if transition is valid, false otherwise
     */
    // check if state transition is valid
    private boolean isValidTransition(String currentState, String newState) {

        if(currentState == null || newState == null)
            return false;

        if(currentState.equals("REQUESTED") && (newState.equals("ACCEPTED") || newState.equals("CANCELLED")))
            return true;

        if(currentState.equals("ACCEPTED") && newState.equals("ENROUTE"))
            return true;

        if(currentState.equals("ENROUTE") && newState.equals("COMPLETED"))
            return true;

        return false;
    }

}