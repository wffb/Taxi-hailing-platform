package filter;

import DAO.DriverDAO;
import DAO.RideDAO;
import DAO.RiderDAO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.helper.PasswordEncryptHelper;
import common.helper.ResponseHelper;
import common.util.CacheUtil;
import common.util.JwtUtil;
import model.LoginUser;
import model.User;
import service.UserService;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

public class UsernamePasswordAuthFilter implements AuthFilter {
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, AuthFilterChain chain, Servlet servlet) throws IOException, ServletException {

        //try to get username and password from request
        if ( "/user/login".equals(request.getRequestURI()) && "POST".equalsIgnoreCase(request.getMethod())) {

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {

                // user buffer to get request body
                BufferedReader reader = request.getReader();
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                // transform request body to JSON object
                String requestBody = stringBuilder.toString();
                JSONObject jsonObject = JSON.parseObject(requestBody);

                //get username and password from JSON object
                String username = jsonObject.getString("username");
                String password = jsonObject.getString("password");



                if (username != null && password != null) {
                    System.out.println("get Username: " + username);
                    System.out.println("get Password: " + password);

                    //do username and password authentication
                        //TODO: encrypt password before authentication
                    User user = UserService.getInstance().getUserByUsername(username);
                    if (user == null  ) {
                        ResponseHelper.sendErrorMessage(response, "username or password error");
                        return;
                    }

                    try {
                        boolean isPasswordMatch = PasswordEncryptHelper.verifyPassword(password, user.getPassword());
                        if(!isPasswordMatch){
                            ResponseHelper.sendErrorMessage(response, "username or password error");
                            return;
                        }
                    }catch (Exception e){
                        System.err.println(e.getMessage());
                        ResponseHelper.sendErrorMessage(response, "error occurred while verifying password");
                        return;

                    }



                    String id = String.valueOf(user.getUser_id());
                    String role = user.getIdentity() == 1 ? "Driver" : "Rider";


                    String roleId = "";
                    try {
                        //get specific role ID
                        if (role.equals("Driver"))
                            roleId = String.valueOf(DriverDAO.getInstance().getDriverIDByUserID(user.getUser_id()));
                        else
                            roleId = String.valueOf(RiderDAO.getInstance().getRiderIDByUserID(user.getUser_id()));

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }


                    //generate token
                    Date now = new Date();
                    LoginUser loginUser = new LoginUser(
                            id,
                            roleId,
                            now,
                            role
                    );
                    String token = JwtUtil.getInstance().generateToken(loginUser);


                    String redisKey = "login:" + id;
                    CacheUtil.LoginUserCache.put(redisKey,loginUser);


                    JSONObject data = new JSONObject();
                    data.put("id",id);
                    data.put("token",token);



                    ResponseHelper.sendSuccessMessage(response, "login success",data);
                    return;

                }

            }

        }

        chain.doFilter(request, response, servlet);
    }
}
