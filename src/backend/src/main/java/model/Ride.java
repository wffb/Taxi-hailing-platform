package model;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ride {
    private int ride_id;
    private int driver_id;
    private int rider_id;
    private String pickup_location;
    private String destination;
    private double estimate_fare;
    private double actual_fare;
    private String ride_state;
    private Duration required_time;
    private LocalDateTime start_time;
    public LocalDateTime getStart_time() { return start_time; }
    public void setStart_time(LocalDateTime start_time) { this.start_time = start_time; }
    public Ride() {}
    public int getRide_id() {
        return ride_id;
    }

    public void setRide_id(int ride_id) {
        this.ride_id = ride_id;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getRider_id() {
        return rider_id;
    }

    public void setRider_id(int rider_id) {
        this.rider_id = rider_id;
    }

    public String getPickup_location() {
        return pickup_location;
    }

    public void setPickup_location(String pickup_location) {
        this.pickup_location = pickup_location;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getEstimate_fare() {
        return estimate_fare;
    }

    public void setEstimate_fare(double estimate_fare) {
        this.estimate_fare = estimate_fare;
    }

    public double getActual_fare() {
        return actual_fare;
    }

    public void setActual_fare(double actual_fare) {
        this.actual_fare = actual_fare;
    }

    public String getRide_state() {
        return ride_state;
    }

    public void setRide_state(String ride_state) {
        this.ride_state = ride_state;
    }

    public Duration getRequired_time() {
        return required_time;
    }

    public void setRequired_time(Duration required_time) {
        this.required_time = required_time;
    }




}
