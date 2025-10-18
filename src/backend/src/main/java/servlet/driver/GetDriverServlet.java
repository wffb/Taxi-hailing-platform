package servlet.driver;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import model.Driver;
import service.DriverService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class GetDriverServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        //handle request
        List<Driver> drivers = null;
        try {
            drivers = DriverService.getInstance().getAllDrivers();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        //build response
        CommonResult<List<Driver>> result = CommonResult.success(drivers);
        String res = JSON.toJSONString(result);

        resp.getWriter().write(res);
    }
}
