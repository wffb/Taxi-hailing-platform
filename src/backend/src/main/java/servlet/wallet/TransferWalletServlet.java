package servlet.wallet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import common.api.CommonResult;
import common.util.JwtUtil;
import DAO.UserDAO;
import model.User;
import service.WalletService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/wallet/transfer")
public class TransferWalletServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Get user_id from token (fromId)
        String token = req.getHeader("token");
        if (token == null || token.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        String user_id = JwtUtil.getInstance().getUserIDFromToken(token);
        if (user_id == null || user_id.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Parse fromId to integer
        int fromId;
        try {
            fromId = Integer.parseInt(user_id);
        } catch (NumberFormatException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Read request body
        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }

        // Parse JSON body
        String requestBody = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(requestBody);

        // Get toId from request body
        String toIdStr = jsonObject.getString("toId");
        if (toIdStr == null || toIdStr.trim().isEmpty()) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        int toId;
        try {
            toId = Integer.parseInt(toIdStr);
        } catch (NumberFormatException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Verify toId user exists
        UserDAO userDAO = UserDAO.getInstance();
        try {
            User toUser = userDAO.getUserById(toId);
            if (toUser == null) {
                CommonResult<Object> errorResult =
                        CommonResult.failed("wallet transfer failed");
                String errorResponse = JSON.toJSONString(errorResult);
                resp.getWriter().write(errorResponse);
                return;
            }
        } catch (SQLException e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Get amount from request body
        BigDecimal amount;
        try {
            String amountStr = jsonObject.getString("amount");
            if (amountStr == null || amountStr.trim().isEmpty()) {
                CommonResult<Object> errorResult =
                        CommonResult.failed("wallet transfer failed");
                String errorResponse = JSON.toJSONString(errorResult);
                resp.getWriter().write(errorResponse);
                return;
            }
            amount = new BigDecimal(amountStr);
        } catch (Exception e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Validate amount: must be > 0, <= 10000, and have at most 2 decimal places
        if (amount.compareTo(BigDecimal.ZERO) <= 0 ||
                amount.compareTo(new BigDecimal("10000")) > 0) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Check decimal places (scale <= 2)
        if (amount.scale() > 2) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Call service to transfer wallet
        boolean success;
        try {
            success = WalletService.getInstance().transferWallet(fromId, toId, amount);
        } catch (Exception e) {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
            return;
        }

        // Build response
        if (success) {
            CommonResult<Object> result =
                    CommonResult.success("wallet transfer successfully", null);
            String response = JSON.toJSONString(result);
            resp.getWriter().write(response);
        } else {
            CommonResult<Object> errorResult =
                    CommonResult.failed("wallet transfer failed");
            String errorResponse = JSON.toJSONString(errorResult);
            resp.getWriter().write(errorResponse);
        }
    }
}