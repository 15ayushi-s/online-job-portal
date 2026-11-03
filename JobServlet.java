package com.jobhub.web;

import com.jobhub.model.Job;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.service.AppContext;
import com.jobhub.service.JobService;
import com.jobhub.util.Http;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Mapped to /api/jobs/* */
public class JobServlet extends BaseServlet {
    private final JobService jobs = AppContext.jobs();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length == 0) {                                          // GET /api/jobs?q=   (public)
            Http.write(res, HttpServletResponse.SC_OK, jobs.searchApproved(req.getParameter("q")));
        } else if (p.length == 1 && p[0].equals("all")) {             // GET /api/jobs/all  (admin)
            Http.write(res, HttpServletResponse.SC_OK, jobs.listAll(requireLogin(req)));
        } else if (p.length == 2 && p[0].equals("employer")) {        // GET /api/jobs/employer/{id}
            Http.write(res, HttpServletResponse.SC_OK, jobs.listForEmployer(requireLogin(req), Http.id(p[1])));
        } else throw notFound();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (segments(req).length != 0) throw notFound();
        User employer = requireRole(req, Role.EMPLOYER);
        Map<String, Object> b = Http.readJson(req);
        Job input = new Job();
        input.setTitle(Http.str(b, "title"));
        input.setCompany(Http.str(b, "company"));
        input.setLocation(Http.str(b, "location"));
        input.setType(Http.str(b, "type"));
        input.setSalary(Http.str(b, "salary"));
        input.setSkills(Http.str(b, "skills"));
        input.setDescription(Http.str(b, "description"));
        Http.write(res, HttpServletResponse.SC_CREATED, jobs.create(employer, input));
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length != 2 || !p[1].equals("status")) throw notFound();   // PUT /api/jobs/{id}/status?value=
        Http.write(res, HttpServletResponse.SC_OK,
            jobs.setStatus(requireLogin(req), Http.id(p[0]), req.getParameter("value")));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) {
        String[] p = segments(req);
        if (p.length != 1) throw notFound();
        jobs.delete(requireLogin(req), Http.id(p[0]));
        Http.noContent(res);
    }
}
