package model;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Driver {
    private int driver_id;
    private int user_id;
    private String schedule_mon;
    private String schedule_tue;
    private String schedule_wed;
    private String schedule_thu;
    private String schedule_fri;
    private String schedule_sat;
    private String schedule_sun;
    public Driver() {
        this.schedule_mon = "000000000000000000000000000000000000000000000000";
        this.schedule_tue = "000000000000000000000000000000000000000000000000";
        this.schedule_wed = "000000000000000000000000000000000000000000000000";
        this.schedule_thu = "000000000000000000000000000000000000000000000000";
        this.schedule_fri = "000000000000000000000000000000000000000000000000";
        this.schedule_sat = "000000000000000000000000000000000000000000000000";
        this.schedule_sun = "000000000000000000000000000000000000000000000000";
    }

    // Getters & Setters
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public int getDriver_id() { return driver_id; }
    public void setDriver_id(int driver_id) { this.driver_id = driver_id; }

    public String getSchedule_mon() {
        return schedule_mon;
    }
    public void setSchedule_mon(String schedule_mon) {
        this.schedule_mon = schedule_mon;
    }

    public String getSchedule_tue() {
        return schedule_tue;
    }

    public void setSchedule_tue(String schedule_tue) {
        this.schedule_tue = schedule_tue;
    }

    public String getSchedule_wed() {
        return schedule_wed;
    }

    public void setSchedule_wed(String schedule_wed) {
        this.schedule_wed = schedule_wed;
    }

    public String getSchedule_thu() {
        return schedule_thu;
    }

    public void setSchedule_thu(String schedule_thu) {
        this.schedule_thu = schedule_thu;
    }

    public String getSchedule_fri() {
        return schedule_fri;
    }

    public void setSchedule_fri(String schedule_fri) {
        this.schedule_fri = schedule_fri;
    }

    public String getSchedule_sat() {
        return schedule_sat;
    }

    public void setSchedule_sat(String schedule_sat) {
        this.schedule_sat = schedule_sat;
    }

    public String getSchedule_sun() {
        return schedule_sun;
    }

    public void setSchedule_sun(String schedule_sun) {
        this.schedule_sun = schedule_sun;
    }
}
