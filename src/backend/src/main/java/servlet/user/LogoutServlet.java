package servlet.user;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import common.util.CacheUtil;
import common.util.JwtUtil;
import model.Driver;
import service.DriverService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LogoutServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        //handle request
        String token = req.getHeader("token");
        String userID = JwtUtil.getInstance().getUserIDFromToken(token);

        //logout
        String redisKey = "login:" + userID;
        CacheUtil.LoginUserCache.remove(redisKey);

        ResponseHelper.sendSuccessMessage(resp, "Logout success");
    }
}
