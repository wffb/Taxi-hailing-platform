package servlet;

import authentication.AuthenticationEnforcer;
import authentication.AuthenticationProvider;
import config.ServeletConfig;
import config.filters.AllFilterConfig;
import filter.AuthFilterChain;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/*")  // 全局拦截
public class FrontController extends HttpServlet {

    private Map<String, HttpServlet> servletMapping;

    @Override
    public void init() throws ServletException {
        /** can be changed to
         *      use Annotation to capture the servlets
         *      use configuration center dynamically change
         */

        try {
            //load config
            AllFilterConfig allFilterConfig = AllFilterConfig.loadConfig();
            //load filters
            AuthenticationProvider.getInstance().loadFilters(allFilterConfig.getFilters());
            //load chains
            for(Map.Entry<String, List<String>> entry : allFilterConfig.getFilterChains().entrySet()){
                AuthFilterChain chain = AuthenticationProvider.getInstance().buildChain(entry.getValue());
                AuthenticationEnforcer.getInstance().registerChain(entry.getKey(), chain);
            }
            //load chain mapping
            for(Map.Entry<String, String> entry : allFilterConfig.getPaths().entrySet()){
                AuthenticationEnforcer.getInstance().registerPath(entry.getKey(), entry.getValue());
            }

        }catch (Exception e){
            System.err.println("System initialization failed: " + e.getMessage());
            return;
        }

        // read servlet mapping from config file
        servletMapping = ServeletConfig.loadServlets();

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String path = req.getRequestURI().substring(req.getContextPath().length());
        HttpServlet targetServlet = servletMapping.get(path);

        if (targetServlet == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
            return;
        }

        AuthenticationEnforcer.getInstance().enforce(path, req, resp, targetServlet);
    }
}