package filter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthFilter {

    void doFilter(HttpServletRequest request, HttpServletResponse response, AuthFilterChain chain , Servlet servlet)
            throws IOException, ServletException;
}
