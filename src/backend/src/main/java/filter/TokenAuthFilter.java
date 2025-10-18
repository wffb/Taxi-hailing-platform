package filter;

import com.alibaba.fastjson2.JSON;
import common.api.CommonResult;
import common.helper.ResponseHelper;
import common.util.CacheUtil;
import common.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import model.LoginUser;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;

public class TokenAuthFilter implements AuthFilter{

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, AuthFilterChain chain, Servlet servlet) throws IOException, ServletException {
        // get token from header
        String token = request.getHeader("token");

        if (token == null || token.isEmpty()) {
            //build error response if not exist
            ResponseHelper.sendErrorMessage(response, "token is empty");
            return;
        }


        //judge token
        String userID;
        String role;
        Date iat;
        boolean isDriver;

        try {

            userID = JwtUtil.getInstance().getUserIDFromToken(token);
            isDriver= JwtUtil.getInstance().getIsDriverFromToken(token);
            iat=JwtUtil.getInstance().getIatFromToken(token);

        } catch (Exception e) {

            System.out.println("[INFO] token: "+token+" token is invalid");
            ResponseHelper.sendErrorMessage(response, "token is invalid");
            return;
        }

        String redisKey = "login:" + userID;
        LoginUser loginUser = (LoginUser) CacheUtil.LoginUserCache.get(redisKey);


        //check if user is logged in
        if(Objects.isNull(loginUser)){
            System.out.println("authenticated "+(isDriver?"driver":"user")+ userID+":" +"failed");
            ResponseHelper.sendErrorMessage(response, "user is not authenticated, please login again");
            return;
        }

        //check if token is expired
        LocalDateTime tokenTime= LocalDateTime.ofInstant(iat.toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime cachedTime= LocalDateTime.ofInstant(loginUser.getLastLogin().toInstant(), ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES);

        Boolean isExpired= JwtUtil.getInstance().validateToken(token,loginUser);
        Boolean isCreated = cachedTime.isBefore(tokenTime);

        if(!JwtUtil.getInstance().validateToken(token,loginUser) || tokenTime.isBefore(cachedTime)){
            System.out.println("authenticated "+(isDriver?"driver":"user")+ userID+":" +"failed");
            ResponseHelper.sendErrorMessage(response, "login token is expired, please login again");
            return;
        }

        System.out.println("authenticated "+(isDriver?"driver":"user")+ userID+":" +"success");


        chain.doFilter(request, response, servlet);
    }
}
