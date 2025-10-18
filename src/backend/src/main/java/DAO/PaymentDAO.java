package DAO;

import model.Payment;
import config.DBconfig;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    private static final String URL = DBconfig.DB_URL;
    private static final String USER = DBconfig.DB_USER;
    private static final String PASSWORD = DBconfig.DB_PASSWORD;

    private static volatile PaymentDAO INSTANCE;

    private PaymentDAO() {}

    public static PaymentDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (PaymentDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PaymentDAO();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * create a new payment record.
     */
    public boolean createPayment(int rider_id, int driver_id, BigDecimal amount) {
        String sql = "INSERT INTO payment (rider_id, driver_id, amount, payment_time) " +
                "VALUES (?, ?, ?, CURRENT_TIMESTAMP AT TIME ZONE 'Australia/Melbourne')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rider_id);
            stmt.setInt(2, driver_id);
            stmt.setBigDecimal(3, amount);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * create a new payment record for riders' top up.
     */
    public boolean createPaymentForTopUp(int rider_id, BigDecimal amount) {
        String sql = "INSERT INTO payment (rider_id, driver_id, amount, payment_time) " +
                "VALUES (?, 0, ?, CURRENT_TIMESTAMP AT TIME ZONE 'Australia/Melbourne')";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rider_id);
            stmt.setBigDecimal(2, amount);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all payment records for a given driver ID.
     */
    public List<Payment> getByDriverID(int driver_id) {
        String sql = "SELECT * FROM payment WHERE driver_id = ?";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, driver_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payment payment = extractPayment(rs);
                payments.add(payment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    /**
     * Get all payment records for a given rider ID.
     */
    public List<Payment> getByRiderID(int rider_id) {
        String sql = "SELECT * FROM payment WHERE rider_id = ?";
        List<Payment> payments = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rider_id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payment payment = extractPayment(rs);
                payments.add(payment);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return payments;
    }

    /**
     * Extract a Payment object from a ResultSet.
     */
    private Payment extractPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPayment_id(rs.getInt("payment_id"));
        payment.setRider_id(rs.getInt("rider_id"));
        payment.setDriver_id(rs.getInt("driver_id"));
        payment.setAmount(rs.getInt("amount"));

        Timestamp timestamp = rs.getTimestamp("payment_time");
        if (timestamp != null) {
            payment.setPayment_time(timestamp.toInstant().atZone(ZoneId.of("Australia/Melbourne")));
        }

        return payment;
    }

    /**
     * Simple test function to demonstrate DAO usage.
     */
    public static void testPaymentDAO() {
        PaymentDAO dao = PaymentDAO.getInstance();

        System.out.println("=== Creating a new payment record ===");
        boolean success = dao.createPayment(1, 2, BigDecimal.valueOf(50.0));
        System.out.println("Insert success: " + success);

        System.out.println("\n=== Fetching payments by driver_id = 2 ===");
        List<Payment> driverPayments = dao.getByDriverID(2);
        for (Payment p : driverPayments) {
            System.out.println(p);
        }

        System.out.println("\n=== Fetching payments by rider_id = 1 ===");
        List<Payment> riderPayments = dao.getByRiderID(1);
        for (Payment p : riderPayments) {
            System.out.println(p);
        }

        System.out.println("\n=== Creating a new top-up payment record ===");
        boolean topUpSuccess = dao.createPaymentForTopUp(1, BigDecimal.valueOf(100));
        System.out.println("Top-up insert success: " + topUpSuccess);
    }

    public static void main(String[] args) {
        PaymentDAO.testPaymentDAO();
    }
}
