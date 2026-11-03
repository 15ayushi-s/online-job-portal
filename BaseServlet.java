package com.jobhub.web;

import com.jobhub.exception.AuthException;
import com.jobhub.exception.ForbiddenException;
import com.jobhub.exception.JobHubException;
import com.jobhub.exception.NotFoundException;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.util.Http;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;

/**
 * Parent of every API servlet. Overrides service() so that any JobHubException thrown
 * by a subclass becomes a clean JSON error ({"message": ...}) with the right HTTP status.
 */
public abstract class BaseServlet extends HttpServlet {
    public static final String SESSION_USER = "jobhub.user";

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        try {
            super.service(req, res);           // dispatches to doGet / doPost / doPut / doDelete
        } catch (JobHubException e) {
            if (e.getCause() != null) log(getClass().getSimpleName() + ": " + e.getMessage(), e.getCause());
            // never leak SQL details to the client
            String msg = e.getStatus() >= 500 ? "Server error. Please try again." : e.getMessage();
            Http.error(res, e.getStatus(), msg);
        } catch (RuntimeException e) {
            log("Unexpected error in " + getClass().getSimpleName(), e);
            Http.error(res, 500, "Server error. Please try again.");
        }
    }

    /** Path after the servlet mapping, split into segments: "/employer/5" -> ["employer","5"]. */
    protected String[] segments(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null) return new String[0];
        return Arrays.stream(info.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute(SESSION_USER);
    }

    protected User requireLogin(HttpServletRequest req) {
        User u = currentUser(req);
        if (u == null) throw new AuthException("Please log in");
        return u;
    }

    protected User requireRole(HttpServletRequest req, Role role) {
        User u = requireLogin(req);
        if (!u.hasRole(role)) throw new ForbiddenException("This action requires the " + role + " role");
        return u;
    }

    protected NotFoundException notFound() { return new NotFoundException("Not found"); }
}
