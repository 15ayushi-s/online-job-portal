package com.jobhub.web;

import com.jobhub.model.User;
import com.jobhub.service.AppContext;
import com.jobhub.service.UserService;
import com.jobhub.util.Http;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

/** Mapped to /api/users/* */
public class UserServlet extends BaseServlet {
    private final UserService users = AppContext.users();

    /** New session on every login/registration (prevents session fixation). */
    private void startSession(HttpServletRequest req, User u) {
        HttpSession old = req.getSession(false);
        if (old != null) old.invalidate();
        HttpSession s = req.getSession(true);
        s.setMaxInactiveInterval(30 * 60);
        s.setAttribute(SESSION_USER, u);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length != 1) throw notFound();
        switch (p[0]) {
            case "register" -> {
                Map<String, Object> b = Http.readJson(req);
                User u = users.register(Http.str(b, "name"), Http.str(b, "email"), Http.str(b, "password"), Http.str(b, "role"));
                startSession(req, u);
                Http.write(res, HttpServletResponse.SC_CREATED, u);
            }
            case "login" -> {
                Map<String, Object> b = Http.readJson(req);
                User u = users.login(Http.str(b, "email"), Http.str(b, "password"));
                startSession(req, u);
                Http.write(res, HttpServletResponse.SC_OK, u);
            }
            case "logout" -> {
                HttpSession s = req.getSession(false);
                if (s != null) s.invalidate();
                Http.write(res, HttpServletResponse.SC_OK, Map.of("ok", true));
            }
            default -> throw notFound();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length == 0) {                                   // GET /api/users  (admin)
            Http.write(res, HttpServletResponse.SC_OK, users.listAll(requireLogin(req)));
        } else if (p.length == 1 && p[0].equals("me")) {       // GET /api/users/me
            Http.write(res, HttpServletResponse.SC_OK, requireLogin(req));
        } else throw notFound();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length != 1) throw notFound();
        User actor = requireLogin(req);
        Map<String, Object> b = Http.readJson(req);
        User updated = users.update(actor, Http.id(p[0]), Http.str(b, "name"), Http.str(b, "email"));
        if (updated.getId().equals(actor.getId())) req.getSession().setAttribute(SESSION_USER, updated);
        Http.write(res, HttpServletResponse.SC_OK, updated);
    }
}
