package config;

import servlet.TestServelet;
import servlet.driver.GetDriverScheduleServlet;
import servlet.driver.UpdateDriverScheduleServlet;
import servlet.ride.AcceptRideServlet;
import servlet.ride.GetAllRidesServlet;
import servlet.ride.RequestRideServlet;
import servlet.user.LoginServlet;
import servlet.user.LogoutServlet;
import servlet.user.RegisterServlet;

import javax.servlet.http.HttpServlet;
import java.util.Map;

public class ServeletConfig {

    public static Map<String, HttpServlet> loadServlets() {
        return Map.of(
                "/test", new TestServelet(),
                //driver servlets
                "/schedules/getAll",new GetDriverScheduleServlet(),
                "/driver/getDrivers",new GetDriverScheduleServlet(),
                "/schedules/update",new UpdateDriverScheduleServlet(),

                //ride servlets
                "/ride/getAllRides",new GetAllRidesServlet(),
                "/ride/requestRide",new RequestRideServlet(),
                "/ride/accept",new AcceptRideServlet(),

                //user servlets
                "/user/register",new RegisterServlet(),
                "/user/logout",new LogoutServlet(),
                "/user/login",new LoginServlet()

                );
    }
}
