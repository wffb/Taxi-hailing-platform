package model;

import java.time.ZonedDateTime;

public class Payment {
    private int payment_id;
    private int rider_id;
    private int driver_id;
    private int amount;
    private ZonedDateTime payment_time;

    public Payment() {}

    public int getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(int payment_id) {
        this.payment_id = payment_id;
    }

    public int getRider_id() {
        return rider_id;
    }

    public void setRider_id(int rider_id) {
        this.rider_id = rider_id;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ZonedDateTime getPayment_time() {
        return payment_time;
    }

    public void setPayment_time(ZonedDateTime payment_time) {
        this.payment_time = payment_time;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "payment_id=" + payment_id +
                ", rider_id=" + rider_id +
                ", driver_id=" + driver_id +
                ", amount=" + amount +
                ", payment_time=" + payment_time +
                '}';
    }
}
