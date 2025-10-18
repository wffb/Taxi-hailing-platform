package filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class TestFilter implements AuthFilter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, AuthFilterChain chain, Servlet servlet) throws IOException, ServletException {
        //before
        System.out.println("TestFilter is running");

        chain.doFilter(request, response, servlet);

        //after
        System.out.println("TestFilter is done");
    }
}
