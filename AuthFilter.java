package com.jobhub.web;

import com.jobhub.util.Http;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Mapped to /api/*. Everything except login, register and the public job search
 * needs a logged-in HttpSession. Role/ownership checks happen in the services.
 */
public class AuthFilter implements Filter {

    private static boolean isPublic(String method, String path) {
        if (method.equals("POST") && (path.equals("/api/users/login") || path.equals("/api/users/register"))) return true;
        return method.equals("GET") && (path.equals("/api/jobs") || path.equals("/api/jobs/"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        res.setHeader("Cache-Control", "no-store");
        res.setHeader("X-Content-Type-Options", "nosniff");

        String path = req.getServletPath() + (req.getPathInfo() == null ? "" : req.getPathInfo());
        if (!isPublic(req.getMethod(), path)) {
            HttpSession s = req.getSession(false);
            if (s == null || s.getAttribute(BaseServlet.SESSION_USER) == null) {
                Http.error(res, HttpServletResponse.SC_UNAUTHORIZED, "Please log in");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
