package filter;

import common.helper.ResponseHelper;
import common.util.CacheUtil;
import common.util.JwtUtil;
import model.LoginUser;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RiderAuthFilter implements AuthFilter{
    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, AuthFilterChain chain, Servlet servlet) throws IOException, ServletException {

        String token = request.getHeader("token");
        String id = JwtUtil.getInstance().getUserIDFromToken(token);
        LoginUser loginUser = (LoginUser) CacheUtil.LoginUserCache.get("login:"+id);

        if(loginUser!= null && loginUser.getRole().equals("Rider")){
            chain.doFilter(request, response, servlet);
            return;
        }

        ResponseHelper.sendErrorMessage(response, "Role Authentication Failed");

    }
}
