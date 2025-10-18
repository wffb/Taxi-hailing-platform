package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

//        String origin = req.getHeader("Origin");
//        if (origin != null &&
//                (origin.equals("http://localhost:5174") || origin.equals("http://127.0.0.1:5174"))) {
//
//            resp.setHeader("Access-Control-Allow-Origin", origin);
//            resp.setHeader("Vary", "Origin");
//            resp.setHeader("Access-Control-Allow-Credentials", "true");
//            resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
//            resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
//            resp.setHeader("Access-Control-Max-Age", "86400");
//        }

        // Allow all origins
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, token, Authorization");
        resp.setHeader("Access-Control-Max-Age", "86400");

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(request, response);
    }
}
