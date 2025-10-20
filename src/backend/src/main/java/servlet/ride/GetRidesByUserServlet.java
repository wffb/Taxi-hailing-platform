package servlet.ride;

import DAO.DriverDAO;
import DAO.RiderDAO;
import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import common.util.JwtUtil;
import DAO.UserDAO;
import model.Driver;
import model.Ride;
import model.User;
import service.RideService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@WebServlet(urlPatterns = "/ride/getRidesByUser")
public class GetRidesByUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get user_id from token
        String token = req.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to retrieve rides: Token is required");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        String user_id = JwtUtil.getInstance().getUserIDFromToken(token);
        if (user_id == null || user_id.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to retrieve rides: Invalid token");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Parse user_id to integer
        int userId;
        try {
            userId = Integer.parseInt(user_id);
        } catch (NumberFormatException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to retrieve rides: Invalid user ID format");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Get user to determine identity (0 = rider, 1 = driver)
        User user;
        try {
            user = UserDAO.getInstance().getUserById(userId);
            if (user == null) {
                CommonResult<Object> errorResult =
                        CommonResult.failed("Failed to retrieve rides: User not found");
                String errorResponse = JSON.toJSONString(errorResult);
                resp.getWriter().write(errorResponse);
                return;
            }
        } catch (SQLException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to retrieve rides: " + e.getMessage());
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        int identity = user.getIdentity();

        // Get rides based on user identity
        List<Ride> rides;
        try {
            RideService rideService = RideService.getInstance();

            if (identity == 0) {
                // User is a rider
                RiderDAO riderDAO = RiderDAO.getInstance();
                int riderId = riderDAO.getRiderIDByUserID(userId);
                rides = rideService.getRidesByRiderId(riderId);
            } else if (identity == 1) {
                // User is a driver
                DriverDAO driverDAO = DriverDAO.getInstance();
                int driverId = driverDAO.getDriverIDByUserID(userId);
                rides = rideService.getRidesByDriverId(driverId);
            } else {
                // Invalid identity
                CommonResult<Object> errorResult =
                        CommonResult.failed("Failed to retrieve rides: Invalid user identity");
                String errorResponse = JSON.toJSONString(errorResult);
                resp.getWriter().write(errorResponse);
                return;
            }
        } catch (SQLException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to retrieve rides: " + e.getMessage());
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build response data
        List<Map<String, Object>> rideList = new ArrayList<>();

        for (Ride ride : rides) {
            Map<String, Object> rideData = new HashMap<>();

            rideData.put("ride_id", ride.getRide_id());
            rideData.put("driver_id", ride.getDriver_id());
            rideData.put("rider_id", ride.getRider_id());
            rideData.put("pickup_location", ride.getPickup_location());
            rideData.put("destination", ride.getDestination());

            // Format fares to 2 decimal places
            rideData.put("estimate_fare", String.format("%.2f", ride.getEstimate_fare()));
            rideData.put("actual_fare", String.format("%.2f", ride.getActual_fare()));

            rideData.put("ride_state", ride.getRide_state());

            // Convert Duration to seconds
            rideData.put("required_time", ride.getRequired_time() != null ?
                    ride.getRequired_time().getSeconds() : null);

            // Format start_time
            rideData.put("start_time", ride.getStart_time() != null ?
                    ride.getStart_time().toString() : null);

            rideList.add(rideData);
        }

        // Build success response
        CommonResult<List<Map<String, Object>>> result =
                CommonResult.success("Rides retrieved successfully", rideList);
        String response = JSON.toJSONString(result);
        resp.getWriter().write(response);
    }
}