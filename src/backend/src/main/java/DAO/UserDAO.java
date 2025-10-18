package DAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import DAO.uow.UoW;
import config.DBconfig;
import model.User;
import org.jetbrains.annotations.NotNull;

public class UserDAO {
    // Database connection details
    private static final String URL = DBconfig.DB_URL;
    private static final String USER = DBconfig.DB_USER;
    private static final String PASSWORD = DBconfig.DB_PASSWORD;

    // Single instance
    private static volatile UserDAO INSTANCE;
    public UserDAO() {}
    public static UserDAO getInstance() {
        if (INSTANCE == null) {
            synchronized (UserDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserDAO();
                }
            }
        }
        return INSTANCE;
    }

    // 1. Add a new user
    public void addUser(@NotNull User user) throws SQLException {
        String sql = "INSERT INTO \"user\" (user_name, password, wallet_balance, identity, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getUser_name());
            stmt.setString(2, user.getPassword());
            stmt.setBigDecimal(3, user.getWallet_balance());
            stmt.setInt(4, user.getIdentity());
            stmt.setString(5, user.getEmail());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 2. Delete an existing user
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM \"user\" WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 3. Find user by user_id
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, user_name, password, wallet_balance, identity, email FROM \"user\" WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUser_id(rs.getInt("user_id"));
                    user.setUser_name(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setWallet_balance(rs.getBigDecimal("wallet_balance"));
                    user.setIdentity(rs.getInt("identity"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, user_name, password, wallet_balance, identity, email FROM \"user\" WHERE user_name = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUser_id(rs.getInt("user_id"));
                    user.setUser_name(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setWallet_balance(rs.getBigDecimal("wallet_balance"));
                    user.setIdentity(rs.getInt("identity"));
                    user.setEmail(rs.getString("email"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return null;
    }



    // 4. Get all users and return as List<User>
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, user_name, password, wallet_balance, identity, email FROM \"user\"";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUser_id(rs.getInt("user_id"));
                user.setUser_name(rs.getString("user_name"));
                user.setPassword(rs.getString("password"));
                user.setWallet_balance(rs.getBigDecimal("wallet_balance"));
                user.setIdentity(rs.getInt("identity"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return users;
    }
    

    // 6. Update user's user_name
    public void updateUserName(int userId, String newUserName) throws SQLException {
        String sql = "UPDATE \"user\" SET user_name = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newUserName);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 7. Update user's password
    public void updatePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE \"user\" SET password = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // 8. Update user's identity
    public void updateIdentity(int userId, int newIdentity) throws SQLException {
        String sql = "UPDATE \"user\" SET identity = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newIdentity);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
    // 8. Update user's email
    public void updateEmail(int userId, String newEmail) throws SQLException {
        String sql = "UPDATE \"user\" SET email = ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newEmail);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public int getUserIdByUsernameAndPassword(String username, String password) throws SQLException {
        int userId = -1; // Default value if user is not found
        String sql = "SELECT user_id FROM \"user\" WHERE user_name = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return userId;
    }

    /**
     * Get the wallet balance of a user.
     * @param userID user's ID
     * @return wallet balance as BigDecimal, or null if user not found
     */
    public BigDecimal getWallet(int userID) {
        String sql = "SELECT wallet_balance FROM \"user\" WHERE user_id = ?"; // replace 'user' with your actual table name
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("wallet_balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Charge (add money to) a user's wallet.
     * @param userID user's ID
     * @param amount amount to add
     * @return true if successful, false otherwise
     */
    public boolean chargeWallet(int userID, BigDecimal amount) {
        String sql = "UPDATE \"user\" SET wallet_balance = wallet_balance + ? WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, amount);
            stmt.setInt(2, userID);
            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Transfer money between two users (transaction with rollback on error).
     * @param fromID sender's user ID
     * @param toID receiver's user ID
     * @param amount transfer amount
     * @return true if successful, false otherwise
     */
    public void transferWallet(UoW uow, int fromID, int toID, BigDecimal amount) {

        String deductSQL = "UPDATE \"user\" SET wallet_balance = wallet_balance - ? WHERE user_id = ? AND wallet_balance >= ?";
        String addSQL = "UPDATE \"user\" SET wallet_balance = wallet_balance + ? WHERE user_id = ?";

        uow.registerUpdate(deductSQL, deductStmt -> {
            try {
                // deduct from sender
                deductStmt.setBigDecimal(1, amount);
                deductStmt.setInt(2, fromID);
                deductStmt.setBigDecimal(3, amount);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        uow.registerUpdate(addSQL, addStmt -> {
            try {

                // add to receiver
                addStmt.setBigDecimal(1, amount);
                addStmt.setInt(2, toID);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }



    /*
    public static void main(String[] args) {
        UserDAO userDAO = UserDAO.getInstance();
        System.out.println("=== UserDAO Function Testing Started ===\n");
        System.out.println("Note: Database already contains users with IDs 1-4");

        try {
            // Test data variables
            int testUserId = -1; // Store the ID of newly added user

            // 1. Test getting all existing users first
            System.out.println("1. Testing getAllUsers() - Show existing data...");
            List<User> existingUsers = userDAO.getAllUsers();
            System.out.println("Total number of users: " + existingUsers.size());

            System.out.println("Existing users in database:");
            for (User user : existingUsers) {
                System.out.println("  - ID: " + user.getUser_id() +
                        ", Name: " + user.getUser_name() +
                        ", Balance: " + user.getWallet_balance() +
                        ", Identity: " + user.getIdentity() + " (" + (user.getIdentity() == 0 ? "rider" : "driver") + ")" +
                        ", Email: " + user.getEmail());
            }

            // 2. Test adding new user with email
            System.out.println("\n2. Testing addUser() with email field...");
            User newUser = new User();
            newUser.setUser_name("test_user_email_001");
            newUser.setPassword("password123");
            newUser.setWallet_balance(new BigDecimal("100.50"));
            newUser.setIdentity(0); // rider
            newUser.setEmail("test001@example.com");

            userDAO.addUser(newUser);
            System.out.println("Add user result: SUCCESS");

            // 3. Find the newly added user
            System.out.println("\n3. Finding newly added user...");
            List<User> allUsers = userDAO.getAllUsers();
            for (User user : allUsers) {
                if ("test_user_email_001".equals(user.getUser_name())) {
                    testUserId = user.getUser_id();
                    System.out.println("Found test user with ID: " + testUserId);
                    break;
                }
            }

            if (testUserId == -1) {
                System.out.println("Test user not found, add operation might have failed");
                return;
            }

            // 4. Test getUserById() with email
            System.out.println("\n4. Testing getUserById()...");
            User foundUser = userDAO.getUserById(testUserId);
            if (foundUser != null) {
                System.out.println("Successfully retrieved user:");
                printUserInfo(foundUser);
            } else {
                System.out.println("User with specified ID not found");
            }

            // 5. Test updateUserName()
            System.out.println("\n5. Testing updateUserName()...");
            String newUserName = "updated_test_user_email";
            userDAO.updateUserName(testUserId, newUserName);
            System.out.println("Update username result: SUCCESS");

            // Verify username update
            User updatedUser = userDAO.getUserById(testUserId);
            if (updatedUser != null) {
                System.out.println("Verification: Username updated to " + updatedUser.getUser_name());
                System.out.println("Update status: " + (newUserName.equals(updatedUser.getUser_name()) ? "MATCH" : "MISMATCH"));
            }

            // 6. Test updatePassword()
            System.out.println("\n6. Testing updatePassword()...");
            String newPassword = "newpassword456";
            userDAO.updatePassword(testUserId, newPassword);
            System.out.println("Update password result: SUCCESS");

            // Verify password update
            User passwordUpdatedUser = userDAO.getUserById(testUserId);
            if (passwordUpdatedUser != null) {
                System.out.println("Verification: Password updated to " + passwordUpdatedUser.getPassword());
                System.out.println("Update status: " + (newPassword.equals(passwordUpdatedUser.getPassword()) ? "MATCH" : "MISMATCH"));
            }

            // 7. Test updateEmail()
            System.out.println("\n7. Testing updateEmail()...");
            String newEmail = "updated.email@example.com";
            userDAO.updateEmail(testUserId, newEmail);
            System.out.println("Update email result: SUCCESS");

            // Verify email update
            User emailUpdatedUser = userDAO.getUserById(testUserId);
            if (emailUpdatedUser != null) {
                System.out.println("Verification: Email updated to " + emailUpdatedUser.getEmail());
                System.out.println("Update status: " + (newEmail.equals(emailUpdatedUser.getEmail()) ? "MATCH" : "MISMATCH"));
            }

            // 8. Test updateWalletBalance()
            System.out.println("\n8. Testing updateWalletBalance()...");
            BigDecimal newBalance = new BigDecimal("250.75");
            userDAO.updateWalletBalance(testUserId, newBalance);
            System.out.println("Update wallet balance result: SUCCESS");

            // Verify balance update
            User balanceUpdatedUser = userDAO.getUserById(testUserId);
            if (balanceUpdatedUser != null) {
                System.out.println("Verification: Balance updated to " + balanceUpdatedUser.getWallet_balance());
                System.out.println("Update status: " + (newBalance.compareTo(balanceUpdatedUser.getWallet_balance()) == 0 ? "MATCH" : "MISMATCH"));
            }

            // 9. Test updateIdentity()
            System.out.println("\n9. Testing updateIdentity()...");
            int newIdentity = 1; // change to driver
            userDAO.updateIdentity(testUserId, newIdentity);
            System.out.println("Update identity result: SUCCESS");

            // Verify identity update
            User identityUpdatedUser = userDAO.getUserById(testUserId);
            if (identityUpdatedUser != null) {
                System.out.println("Verification: Identity updated to " + identityUpdatedUser.getIdentity() +
                        " (" + (identityUpdatedUser.getIdentity() == 0 ? "rider" : "driver") + ")");
                System.out.println("Update status: " + (newIdentity == identityUpdatedUser.getIdentity() ? "MATCH" : "MISMATCH"));
            }

            // 10. Test multiple balance updates (simulate wallet transactions)
            System.out.println("\n10. Testing multiple wallet balance operations...");

            // Deduct money (simulate ride payment)
            BigDecimal currentBalance = identityUpdatedUser.getWallet_balance();
            BigDecimal deductAmount = new BigDecimal("25.30");
            BigDecimal newBalanceAfterDeduct = currentBalance.subtract(deductAmount);

            userDAO.updateWalletBalance(testUserId, newBalanceAfterDeduct);
            System.out.println("Deducted " + deductAmount + " from wallet");

            User afterDeductUser = userDAO.getUserById(testUserId);
            System.out.println("Balance after deduction: " + afterDeductUser.getWallet_balance());

            // Add money (simulate wallet top-up)
            BigDecimal topUpAmount = new BigDecimal("50.00");
            BigDecimal newBalanceAfterTopUp = afterDeductUser.getWallet_balance().add(topUpAmount);

            userDAO.updateWalletBalance(testUserId, newBalanceAfterTopUp);
            System.out.println("Added " + topUpAmount + " to wallet");

            User afterTopUpUser = userDAO.getUserById(testUserId);
            System.out.println("Balance after top-up: " + afterTopUpUser.getWallet_balance());

            // 11. Test adding another user with different email
            System.out.println("\n11. Testing addUser() with different email...");
            User secondUser = new User();
            secondUser.setUser_name("test_user_email_002");
            secondUser.setPassword("password789");
            secondUser.setWallet_balance(new BigDecimal("75.25"));
            secondUser.setIdentity(1); // driver
            secondUser.setEmail("driver002@example.com");

            userDAO.addUser(secondUser);
            System.out.println("Add second user result: SUCCESS");

            // Find second user ID
            int secondUserId = -1;
            List<User> allUsersAfterSecond = userDAO.getAllUsers();
            for (User user : allUsersAfterSecond) {
                if ("test_user_email_002".equals(user.getUser_name())) {
                    secondUserId = user.getUser_id();
                    System.out.println("Found second test user with ID: " + secondUserId +
                            ", Identity: " + user.getIdentity() + " (" + (user.getIdentity() == 0 ? "rider" : "driver") + ")" +
                            ", Email: " + user.getEmail());
                    break;
                }
            }

            // 12. Test email update on second user
            if (secondUserId != -1) {
                System.out.println("\n12. Testing updateEmail() on second user...");
                String secondUserNewEmail = "updated.driver@example.com";
                userDAO.updateEmail(secondUserId, secondUserNewEmail);

                User updatedSecondUser = userDAO.getUserById(secondUserId);
                if (updatedSecondUser != null) {
                    System.out.println("Second user email updated to: " + updatedSecondUser.getEmail());
                    System.out.println("Update status: " + (secondUserNewEmail.equals(updatedSecondUser.getEmail()) ? "MATCH" : "MISMATCH"));
                }
            }

            // 13. Test operations on existing users (ID 1-4) without deleting them
            System.out.println("\n13. Testing operations on existing users...");
            for (int existingUserId = 1; existingUserId <= 4; existingUserId++) {
                User existingUser = userDAO.getUserById(existingUserId);
                if (existingUser != null) {
                    System.out.println("User ID " + existingUserId + " exists: " + existingUser.getUser_name() +
                            " (Email: " + existingUser.getEmail() + ")");
                } else {
                    System.out.println("User ID " + existingUserId + " not found");
                }
            }

            // 14. Test operations on non-existent user
            System.out.println("\n14. Testing operations on non-existent user...");

            // Test getUserById with non-existent ID
            User nonExistUser = userDAO.getUserById(99999);
            if (nonExistUser == null) {
                System.out.println("CORRECT: getUserById() returned null for non-existent user");
            } else {
                System.out.println("UNEXPECTED: Retrieved a user that shouldn't exist");
            }

            // Test update operations on non-existent user
            try {
                userDAO.updateEmail(99999, "ghost@example.com");
                System.out.println("Update email on non-existent user: No exception thrown");
            } catch (SQLException e) {
                System.out.println("Update email on non-existent user: Exception - " + e.getMessage());
            }

            // 15. Cleanup - Delete only test users
            System.out.println("\n15. Cleanup - Deleting only test users...");

            // Delete the second test user first
            if (secondUserId != -1) {
                userDAO.deleteUser(secondUserId);
                System.out.println("Delete second test user result: SUCCESS");

                // Verify deletion
                User deletedSecondUser = userDAO.getUserById(secondUserId);
                if (deletedSecondUser == null) {
                    System.out.println("Verification: Second test user successfully deleted");
                } else {
                    System.out.println("WARNING: Second test user still exists after deletion");
                }
            }

            // Delete the first test user
            userDAO.deleteUser(testUserId);
            System.out.println("Delete first test user result: SUCCESS");

            // Verify deletion
            User deletedUser = userDAO.getUserById(testUserId);
            if (deletedUser == null) {
                System.out.println("Verification: First test user successfully deleted");
            } else {
                System.out.println("WARNING: First test user still exists after deletion");
            }

            // 16. Final state check
            System.out.println("\n16. Final database state check...");
            List<User> finalUsers = userDAO.getAllUsers();
            System.out.println("Final number of users in database: " + finalUsers.size());
            System.out.println("Original users (1-4) should remain, test users should be cleaned up");

            System.out.println("Remaining users:");
            for (User user : finalUsers) {
                System.out.println("  - ID: " + user.getUser_id() +
                        ", Name: " + user.getUser_name() +
                        ", Email: " + user.getEmail());
            }

            System.out.println("\n=== UserDAO Function Testing Completed ===");

        } catch (SQLException e) {
            System.err.println("Database operation exception: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Other exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
    // Helper method: Print user information including email
    private static void printUserInfo(User user) {
        System.out.println("  User ID: " + user.getUser_id());
        System.out.println("  Username: " + user.getUser_name());
        System.out.println("  Password: " + user.getPassword());
        System.out.println("  Email: " + user.getEmail());
        System.out.println("  Wallet Balance: " + user.getWallet_balance());
        System.out.println("  Identity: " + user.getIdentity() + " (" + (user.getIdentity() == 0 ? "rider" : "driver") + ")");
    }
}