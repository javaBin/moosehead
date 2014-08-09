package no.java.moosehead.security;

import no.java.moosehead.web.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminFilter implements Filter {

    private SecurityHandler securityHandler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.securityHandler = new SecurityHandler();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!Configuration.secureAdmin()) {
            chain.doFilter(request,response);
            return;
        }
        HttpServletRequest req = (HttpServletRequest) request;
        if (securityHandler.isLoggedIn(req.getSession())) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse resp = (HttpServletResponse) response;
            resp.sendRedirect("/login");
        }
    }

    @Override
    public void destroy() {
    }

}
