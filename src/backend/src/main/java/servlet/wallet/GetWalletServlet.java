package servlet.wallet;

import DAO.DriverDAO;
import DTO.WalletDTO;
import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;

import common.util.JwtUtil;
import DAO.UserDAO;
import DAO.RiderDAO;
import model.Driver;
import model.Payment;
import model.User;
import service.WalletService;

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

@WebServlet(urlPatterns = "/wallet")
public class GetWalletServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get user_id from token
        String token = req.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Token is required");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        String user_id = JwtUtil.getInstance().getUserIDFromToken(token);
        if (user_id == null || user_id.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("Invalid token");
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
                    CommonResult.failed("Invalid user_id format");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Get wallet information from service
        WalletDTO walletDTO;
        try {
            walletDTO = WalletService.getInstance().getWallet(userId);
        } catch (Exception e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet info get failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Check if wallet info retrieved successfully
        if (walletDTO == null) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet info get failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build payment records with user names
        List<Map<String, Object>> paymentList = new ArrayList<>();
        UserDAO userDAO = UserDAO.getInstance();
        RiderDAO riderDAO = RiderDAO.getInstance();
        DriverDAO driverDAO = DriverDAO.getInstance();

        // use cache to avoid multiple db transactions
        Map<Integer, String> riderNameCache = new HashMap<>();
        Map<Integer, String> driverNameCache = new HashMap<>();

        for (Payment payment : walletDTO.getRecords()) {
            Map<String, Object> paymentRecord = new HashMap<>();

            paymentRecord.put("id", payment.getPayment_id());

            // Get rider name
            String riderName = "";
            if (payment.getRider_id() > 0) {
                int riderID = payment.getRider_id();

                if (riderNameCache.containsKey(riderID)) {
                    riderName = riderNameCache.get(riderID);
                } else {
                    try {
                        int userID = riderDAO.getRiderById(riderID).getUser_id();
                        User rider = userDAO.getUserById(userID);
                        if (rider != null) {
                            riderName = rider.getUser_name();
                            riderNameCache.put(riderID, riderName);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            paymentRecord.put("rider_name", riderName);

            // Get driver name
            String driverName = "";
            if (payment.getDriver_id() > 0) {
                int driverID = payment.getDriver_id();

                if (driverNameCache.containsKey(driverID)) {
                    driverName = driverNameCache.get(driverID);
                } else {
                    try {
                        int userID = driverDAO.getUserIdByDriverId(driverID);
                        if (userID == -1)
                            System.out.println("driver ID does not exist");
                        User driver = userDAO.getUserById(userID);
                        if (driver != null) {
                            driverName = driver.getUser_name();
                            driverNameCache.put(driverID, driverName);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            paymentRecord.put("driver_name", driverName);

            paymentRecord.put("amount", payment.getAmount());
            paymentRecord.put("date", payment.getPayment_time() != null ?
                    payment.getPayment_time().toString() : null);

            paymentList.add(paymentRecord);
        }


        // Build response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("balance", walletDTO.getBalance());
        responseData.put("payments", paymentList);

        CommonResult<Map<String, Object>> result =
                CommonResult.success("wallet info get successfully", responseData);
        String response = JSON.toJSONString(result);
        resp.getWriter().write(response);
    }
}