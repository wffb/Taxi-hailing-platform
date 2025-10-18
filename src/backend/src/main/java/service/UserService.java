package service;

import DAO.DriverDAO;
import DAO.RideDAO;
import DAO.RiderDAO;
import DAO.UserDAO;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import model.Driver;
import model.Rider;
import model.User;

import java.math.BigDecimal;
import java.sql.SQLException;

public class UserService {

    private UserDAO userDAO;
    private DriverDAO driverDAO;


    private UserService() {

        userDAO = UserDAO.getInstance();
        driverDAO = DriverDAO.getInstance();
    }

    // INSTANCE
    private static volatile UserService INSTANCE;

    public static UserService getInstance() {
        if (INSTANCE == null) {
            synchronized (UserService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserService();
                }
            }
        }
        return INSTANCE;
    }

    public int getUserIDbyUsernameAndPassword(String username, String password)  {

        int userId = -1;
        try {
           userId = userDAO.getUserIdByUsernameAndPassword(username, password);
        } catch (SQLException e) {
            System.out.println("[Error] error in getUserIDbyUsernameAndPassword: " + e.getMessage());
        }

        return userId;
    }

    public User getUserByUserID (int userId)  {

        User user = null;
        try {
            user = userDAO.getUserById(userId);
        }catch (SQLException e) {
            System.out.println("[Error] error in getUserByUserID: " + e.getMessage());
        }
        return user;
    }

    public CommonResult<String> registerUser(User user) {

        if (    user == null ||
                user.getUser_name()== null ||
                user.getPassword() == null
        ) {
             System.out.println("[Error] error in registerUser: user object is null or missing fields");
             return CommonResult.failed("User information is incomplete");
        }


        try {

            //existing user check
            User existingUser = UserDAO.getInstance().getUserByUsername(user.getUser_name());
            if (existingUser!= null) {
                System.out.println("[Error] error in registerUser: user already exists");
                return CommonResult.failed("User already exists");
            }


            //add user to database
            user.setWallet_balance(BigDecimal.valueOf(0));
            userDAO.addUser(user);

        } catch (SQLException e) {
            System.out.println("[Error] error in registerUser: " + e.getMessage());
            return CommonResult.failed("Failed to register user");
        }

        //TODO: aotomic transaction creation
        if(user.getIdentity() == 1){

            try {
                //get user id from database
                int id = UserDAO.getInstance().getUserIdByUsernameAndPassword(user.getUser_name(), user.getPassword());

                //create default driver
                Driver driver = new Driver();
                driver.setUser_id(id);

                DriverDAO.getInstance().insertDriver(driver);


            } catch (SQLException e) {
                System.out.println("[Error] error in registerUser: " + e.getMessage());
                return CommonResult.failed("Failed to register user");
            }

        }
        else {

            try {
                //get user id from database
                int id = UserDAO.getInstance().getUserIdByUsernameAndPassword(user.getUser_name(), user.getPassword());

                //create default driver
                Rider rider = new Rider();
                rider.setUser_id(id);

                RiderDAO.getInstance().addRider(rider);


            } catch (SQLException e) {
                System.out.println("[Error] error in registerUser: " + e.getMessage());
                return CommonResult.failed("Failed to register user");
            }

        }

        return CommonResult.success("User registered successfully");

    }

    public User getUserByUsername(String username)  {
        User user = null;
        try {
            user = userDAO.getUserByUsername(username);
        }catch (SQLException e) {
            System.out.println("[Error] error in getUserByUsername: " + e.getMessage());
        }
        return user;
    }


}
