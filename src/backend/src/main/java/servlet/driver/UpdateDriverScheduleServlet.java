package servlet.driver;

import DAO.DriverDAO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import common.util.CacheUtil;
import common.util.JwtUtil;
import common.util.TransUtil;
import model.LoginUser;
import service.DriverService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//@WebServlet(urlPatterns = "/driver/updateDriversSchedule") // Confirm consistency with front end
public class UpdateDriverScheduleServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private final DriverDAO driverDAO = new DriverDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        // set the response content type and encoding
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // user buffer to get request body
        BufferedReader reader = req.getReader();
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }


        //transform JSON to object
        String requestBody = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(requestBody);

        //handle the request
        String id = "";
        List<String> schedule = null;
        try {
            //get the token from the request header
            String token = req.getHeader("token");
            String userid = JwtUtil.getInstance().getUserIDFromToken(token);

            String cacheKey = "login:"+userid;
            LoginUser driver = (LoginUser) CacheUtil.LoginUserCache.get(cacheKey);
            id = driver.getRoleID();

            schedule = JSON.parseArray(jsonObject.get("schedules").toString(), String.class) ;

            if(schedule == null || schedule.isEmpty()){
                resp.getWriter().write(CommonResult.failed("parameter error").toString());
                return;
            }

            if(!DriverService.getInstance().updateDriverSchedule(Integer.parseInt(id), schedule)){
                resp.getWriter().write(JSON.toJSONString(CommonResult.failed("operation failed")));
                return;
            }


        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JSON.toJSONString(CommonResult.failed("sql operation error")));
            return;
        }

        //build the response
        CommonResult<String> result = CommonResult.success("operation success", id);
        String res = JSON.toJSONString(result);


        // write the response back to the client
        resp.getWriter().write(res);

    }
}
