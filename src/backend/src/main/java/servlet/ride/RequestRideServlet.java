package servlet.ride;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import common.api.CommonResult;
import common.util.CacheUtil;
import common.util.JwtUtil;
import common.util.TransUtil;
import model.LoginUser;
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

//@WebServlet(urlPatterns = "/ride/requestRide")
public class RequestRideServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // user buffer to get request body
        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // transform request body to JSON object
        String requestBody = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(requestBody);

        // Get parameters from URL
            //get from token
        String user_id = JwtUtil.getInstance().getUserIDFromToken(req.getHeader("token"));
        String rider_id = TransUtil.getRoleIDFromUserID(user_id);

        String pickup_location = jsonObject.getString("pickup_location");
        String destination = jsonObject.getString("destination");



        if (rider_id == null || rider_id.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("rider_id parameter is null or empty");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        if (pickup_location == null || pickup_location.trim().isEmpty() ||
                destination == null || destination.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("pickup_location and destination parameters are required");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Parse rider_id to integer

        int riderId;
        try {
            riderId = Integer.parseInt(rider_id);
        } catch (NumberFormatException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Invalid parameter format: rider_id must be a number");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Handle ride request
        Ride createdRide;
        try {
            createdRide = RideService.getInstance().requestRide(riderId, pickup_location, destination);

        } catch (SQLException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to request ride: " + e.getMessage());
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build response
        if (createdRide != null) {
            if (createdRide.getRide_id() == -1){
                // if ride_id == -1, it means Insufficient balance
                CommonResult<Object> errorResult =
                        CommonResult.failed("Failed to request ride: Insufficient balance");
                String errorResponse = JSON.toJSONString(errorResult);
                resp.getWriter().write(errorResponse);
            } else {
                // Create response data matching API specification
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("ride_id", createdRide.getRide_id());
                responseData.put("rider_id", createdRide.getRider_id());
                responseData.put("estimate_fare", createdRide.getEstimate_fare());
                responseData.put("ride_state", createdRide.getRide_state());
                responseData.put("start_time", createdRide.getStart_time() != null ?
                        createdRide.getStart_time().toString() : null);

                CommonResult<Map<String, Object>> result =
                        CommonResult.success("Ride request successful", responseData);
                String response = JSON.toJSONString(result);
                resp.getWriter().write(response);
            }
        } else {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Failed to request ride: Invalid location or parameters");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
        }
    }
}