package servlet.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import common.api.CommonResult;
import common.helper.PasswordEncryptHelper;
import model.User;
import service.DriverService;
import service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


//user registration servlet
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        // set the response content type and encoding
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        //Read the JSON data in the request body
        BufferedReader reader = req.getReader();
        String jsonRequest = reader.lines().collect(Collectors.joining());

        //transform JSON to object
        JSONObject jsonObject = JSONObject.parseObject(jsonRequest);

        //handle the request
        String password = "";
        String name = "";
        String email = "";
        String role = "";
        try {

            password = jsonObject.getString("password");
            name = jsonObject.getString("username");
            email = jsonObject.getString("email");
            role = jsonObject.getString("role");

            if(password.isEmpty() || name.isEmpty() || email.isEmpty() || role.isEmpty()){
                resp.getWriter().write(JSON.toJSONString(CommonResult.failed("register information is incomplete")));
                return;
            }

            User user = new User();
            user.setUser_name(name);
            //TODO: encrypt password
            user.setPassword(PasswordEncryptHelper.encryptPassword(password));
            //TODO: validate email format
            user.setEmail(email);
            user.setIdentity(role.equals("Driver")? 1 : 0);

            resp.getWriter().write(JSON.toJSONString(UserService.getInstance().registerUser(user)));
            return;


        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write(JSON.toJSONString(CommonResult.failed("sql operation error")));

        }

    }

}
