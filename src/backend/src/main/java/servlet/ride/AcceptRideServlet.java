package servlet.ride;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import common.util.JwtUtil;
import common.util.TransUtil;
import model.Ride;
import service.RideService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//@WebServlet(urlPatterns = "/ride/accept")
public class AcceptRideServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)   // <- 改为 doPost
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get parameters from URL
        JSONObject jsonRequest = null;
        try {
            jsonRequest = TransUtil.getJsonFromRequest(req);
        }catch (IOException e){
            e.printStackTrace();
            ResponseHelper.sendErrorMessage(resp, "Failed to parse request body: " + e.getMessage());
        }

        String ride_id = jsonRequest.getString("ride_id");


        //identify user and driver
        String user_id = JwtUtil.getInstance().getUserIDFromToken(req.getHeader("token"));
        String driver_id = TransUtil.getRoleIDFromUserID(user_id);

        // Validate input parameters
        if (ride_id == null || ride_id.trim().isEmpty() ||
                driver_id == null || driver_id.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("ride_id or driver_id is null or empty");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Parse parameters to integers
        int rideId;
        int driverId;
        try {
            rideId = Integer.parseInt(ride_id);
            driverId = Integer.parseInt(driver_id);
        } catch (NumberFormatException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Invalid parameter format: ride_id and driver_id must be numbers");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Handle ride acceptance
        boolean success;
        try {
            success = RideService.getInstance().acceptRide(rideId, driverId);
        } catch (SQLException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to accept ride: " + e.getMessage());
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build response
        if (success) {
            // Get ride details to include start_time in response
            try {
                Ride acceptedRide = RideService.getInstance().getRideById(rideId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("ride_id", rideId);
                responseData.put("driver_id", driverId);
                responseData.put("ride_state", "ACCEPTED");
                responseData.put("start_time", acceptedRide != null && acceptedRide.getStart_time() != null ?
                        acceptedRide.getStart_time().toString() : null);

                CommonResult<Map<String, Object>> result =
                        CommonResult.success("Ride accepted successfully", responseData);
                String response = JSON.toJSONString(result);
                resp.getWriter().write(response);
            } catch (SQLException e) {
                // If we can't get ride details, return basic response
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("ride_id", rideId);
                responseData.put("driver_id", driverId);
                responseData.put("ride_state", "ACCEPTED");

                CommonResult<Map<String, Object>> result =
                        CommonResult.success("Ride accepted successfully", responseData);
                String response = JSON.toJSONString(result);
                resp.getWriter().write(response);
            }
        } else {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Ride not found, already accepted or beyond schedule");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
        }
    }
}
