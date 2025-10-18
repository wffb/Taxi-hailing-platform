package authentication;

import DAO.DriverDAO;
import filter.AuthFilter;
import filter.AuthFilterChain;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthenticationEnforcer {
    private static Map<String, AuthFilterChain> chainMapping = new ConcurrentHashMap<>();
    private static Map<String, String> pathMapping = new ConcurrentHashMap<>();

    private static volatile AuthenticationEnforcer INSTANCE;
    public AuthenticationEnforcer() {}
    public static AuthenticationEnforcer getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthenticationEnforcer();
                }
            }
        }
        return INSTANCE;
    }

    public void enforce(String path, HttpServletRequest request, HttpServletResponse response, Servlet servlet) {

        //TODO: Add regular expression OR prefix  matching

        //specific rule for path
        AuthFilterChain chain = null;

        String chainName = pathMapping.get(path);

        if(chainName != null){
            chain = chainMapping.get(chainName);
        }

        //if specific rule not found, use basic rule
        if(chain == null){
            chain = chainMapping.get(pathMapping.get("/*"));
        }

        //do filter chain
        chain.init();
        try {
            chain.doFilter(request, response, servlet);
        } catch (Exception e ) {
            System.out.println("[ERROR] something went wrong while enforcing authentication for path"+path +": "+e.getMessage());
        }

    }

    public void registerPath(String path, String filterName) {
        pathMapping.put(path, filterName);
    }
    public void registerChain(String name, AuthFilterChain chain) {
        chainMapping.put(name, chain);
    }

}
