package model;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;



public class User {
    private int user_id;
    private String user_name;
    private BigDecimal wallet_balance;
    private String password;
    private int identity;
    private String email;
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public int getIdentity() {
        return identity;
    }

    public void setIdentity(int identity) {
        this.identity = identity;
    }
    public User() {}
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public BigDecimal getWallet_balance() {
        return wallet_balance;
    }

    public void setWallet_balance(BigDecimal wallet_balance) {
        this.wallet_balance = wallet_balance;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



}
