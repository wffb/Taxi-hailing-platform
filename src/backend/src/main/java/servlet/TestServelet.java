package servlet;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import model.Driver;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


public class TestServelet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        CommonResult<String> cm = CommonResult.success("Hi!");
        String res = JSON.toJSONString(cm);

        resp.getWriter().write(res);
    }

}
