package servlet.ride;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import common.util.JwtUtil;
import common.util.ScheduleUtil;
import model.Ride;
import service.DriverService;
import service.RideService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//@WebServlet(urlPatterns = "/ride/getAllRides")
public class GetAllRidesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Handle request
        List<Ride> rides;
        try {

            //judge in schedule
            if(!isDriverAvailable(req,resp)){
                ResponseHelper.sendErrorMessage(resp,"Driver is not available now");
                return;
            }

            rides = RideService.getInstance().getAllRides();
        } catch (SQLException e) {
            // Handle database error
            CommonResult<List<Ride>> errorResult =
                    CommonResult.failed("Failed to retrieve rides: " + e.getMessage());
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build success response
        CommonResult<List<Ride>> result = CommonResult.success("Rides retrieved successfully", rides);
        String response = JSON.toJSONString(result);

        resp.getWriter().write(response);
    }


    private boolean isDriverAvailable(HttpServletRequest req, HttpServletResponse resp) throws SQLException {
        String token = req.getHeader("token");
        int driverId = Integer.parseInt(JwtUtil.getInstance().getUserIDFromToken(token));

        List<String>schedules;

        //load from db
        schedules =  DriverService.getInstance().getDriverScheduleByUserID(driverId);

        //check schedule
        return ScheduleUtil.judgeNowFromScheduleInWeek(schedules);
    }
}
