package filter;

import lombok.Setter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AuthFilterChain {

    private List<AuthFilter> filters;
    private int index = 0;



    public AuthFilterChain() {
    }
    public void setFilters( List<AuthFilter> filters) {
        this.filters = filters;
    }


    public void init(){
        //set pointer to the first filter
        index = 0;
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response,Servlet servlet)
            throws IOException, ServletException {

        if (index < filters.size()) {
            AuthFilter filter = filters.get(index++);
            filter.doFilter(request, response,this,servlet);
        } else {
            servlet.service(request, response);
        }
    }
}
