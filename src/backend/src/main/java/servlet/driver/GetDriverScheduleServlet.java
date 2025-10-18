package servlet.driver;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import common.util.CacheUtil;
import common.util.JwtUtil;
import model.Driver;
import model.LoginUser;
import service.DriverService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

//                                                                                       
public class GetDriverScheduleServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        //handle request
        String token = req.getHeader("token");
        String id = JwtUtil.getInstance().getUserIDFromToken(token);
        List<String> schedules = null;

        try {

            if(id == null || id.isEmpty() ){
                resp.getWriter().write(JSON.toJSONString(CommonResult.failed("parameter error")));
                return;
            }
            String cacheKey = "login:"+id;
            LoginUser driver = (LoginUser)CacheUtil.LoginUserCache.get(cacheKey);

            schedules = DriverService.getInstance().getDriverScheduleByDriverID(Integer.parseInt(driver.getRoleID()));

        } catch (SQLException e) {
            e.printStackTrace();
            resp.getWriter().write(JSON.toJSONString(CommonResult.failed("sql operation error")));
        }

        //build response
        CommonResult<String> result = CommonResult.success("operation success",JSON.toJSONString(schedules));
        String res = JSON.toJSONString(result);

        resp.getWriter().write(res);
    }

}
