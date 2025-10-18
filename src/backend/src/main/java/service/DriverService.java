package service;

import DAO.DriverDAO;
import model.Driver;

import java.sql.SQLException;
import java.util.List;

public class DriverService {

     public DriverDAO driverDAO;

     private DriverService() {
         driverDAO = DriverDAO.getInstance();
     }

    //INSTANCE
    private static volatile DriverService INSTANCE;

    public static DriverService getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DriverService();
                }
            }
        }
        return INSTANCE;
    }

    //service function
    public List<Driver> getAllDrivers() throws SQLException {
        return driverDAO.getAllDrivers();
    }

    public List<String> getDriverScheduleByDriverID(int driverId) throws SQLException{
        return driverDAO.getDriverSchedulesByDriverID(driverId);
    }
    public List<String> getDriverScheduleByUserID(int userId) throws SQLException{
        return driverDAO.getDriverSchedulesByUserID(userId);
    }

    public boolean updateDriverSchedule(int driverId, List<String> schedule) throws SQLException {
        return driverDAO.updateDriverSchedules(driverId, schedule);
    }

}
