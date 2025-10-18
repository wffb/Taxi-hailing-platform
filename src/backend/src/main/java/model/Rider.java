package model;

import java.math.BigDecimal;


public class Rider extends User{
    private int rider_id;
    private int user_id;

    public int getRider_id() {
        return rider_id;
    }

    public void setRider_id(int rider_id) {
        this.rider_id = rider_id;
    }

    @Override
    public int getUser_id() {
        return user_id;
    }

    @Override
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public Rider(){}
}
