package servlet.ride;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import common.api.CommonResult;
import common.util.CacheUtil;
import common.util.JwtUtil;
import model.LoginUser;
import model.Ride;
import service.RideService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

// cancel ride servlet
@WebServlet("/ride/cancel")
public class CancelRideServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Get token and validate
            String token = req.getHeader("token");
            String userid = JwtUtil.getInstance().getUserIDFromToken(token);
            if (userid == null || userid.isEmpty()) {
                resp.getWriter().write(JSON.toJSONString(CommonResult.failed("invalid or missing token")));
                return;
            }

            // Get LoginUser from cache
            String cacheKey = "login:" + userid;
            LoginUser user = (LoginUser) CacheUtil.LoginUserCache.get(cacheKey);
            if (user == null) {
                resp.getWriter().write(JSON.toJSONString(CommonResult.failed("user not logged in")));
                return;
            }

            // Read JSON request body
            BufferedReader reader = req.getReader();
            String jsonBody = reader.lines().collect(Collectors.joining());
            JSONObject jsonObject = JSONObject.parseObject(jsonBody);

            int rideId = jsonObject.getIntValue("ride_id");

            // Update ride state to CANCELLED
            boolean success = RideService.getInstance().updateRideState(rideId, 0, "CANCELLED");

            if (success) {
                Ride ride = RideService.getInstance().getRideById(rideId);

                if (ride == null) {
                    resp.getWriter().write(JSON.toJSONString(
                            CommonResult.failed("ride not found after update")
                    ));
                    return;
                }

                JSONObject rideData = convertRideToJson(ride);
                resp.getWriter().write(JSON.toJSONString(
                        CommonResult.success("ride cancelled successfully", rideData)
                ));
            } else {
                resp.getWriter().write(JSON.toJSONString(
                        CommonResult.failed("failed to cancel ride")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JSON.toJSONString(
                    CommonResult.failed("error cancelling ride: " + e.getMessage())
            ));
        }
    }

    // convert ride to json according to api document's requirement
    private JSONObject convertRideToJson(Ride ride) {
        JSONObject rideData = new JSONObject();
        rideData.put("ride_id", ride.getRide_id());
        rideData.put("driver_id", ride.getDriver_id());
        rideData.put("rider_id", ride.getRider_id());
        rideData.put("pickup_location", ride.getPickup_location());
        rideData.put("destination", ride.getDestination());
        rideData.put("estimate_fare", ride.getEstimate_fare());
        rideData.put("actual_fare", ride.getActual_fare());
        rideData.put("ride_state", ride.getRide_state());

        // convert Duration to seconds
        rideData.put("required_time",
                ride.getRequired_time() != null ? ride.getRequired_time().getSeconds() : 0);

        // formalize LocalDateTime
        if (ride.getStart_time() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            rideData.put("start_time", ride.getStart_time().format(formatter));
        }

        return rideData;
    }
}