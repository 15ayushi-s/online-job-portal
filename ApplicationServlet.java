package com.jobhub.web;

import com.jobhub.model.Application;
import com.jobhub.model.User;
import com.jobhub.service.AppContext;
import com.jobhub.service.ApplicationService;
import com.jobhub.util.Http;
import com.jobhub.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Mapped to /api/applications/* */
public class ApplicationServlet extends BaseServlet {
    private final ApplicationService apps = AppContext.applications();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if (segments(req).length != 0) throw notFound();
        User seeker = requireLogin(req);                      // seeker identity comes from the session, not the body
        Map<String, Object> b = Http.readJson(req);
        String jobId = Http.str(b, "jobId");
        if (jobId == null) throw new ValidationException("jobId is required");
        Application a = apps.apply(seeker, Http.id(jobId), Http.str(b, "resumeUrl"));
        Http.write(res, HttpServletResponse.SC_CREATED, Map.of("id", a.getId(), "jobId", a.getJobId(), "status", a.getStatus()));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length != 2) throw notFound();
        User actor = requireLogin(req);
        Long id = Http.id(p[1]);
        switch (p[0]) {
            case "seeker" -> Http.write(res, HttpServletResponse.SC_OK, apps.forSeeker(actor, id));   // /seeker/{id}
            case "job" -> Http.write(res, HttpServletResponse.SC_OK, apps.forJob(actor, id));         // /job/{id}
            default -> throw notFound();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String[] p = segments(req);
        if (p.length != 2 || !p[1].equals("status")) throw notFound();   // PUT /{id}/status?value=
        Application a = apps.setStatus(requireLogin(req), Http.id(p[0]), req.getParameter("value"));
        Http.write(res, HttpServletResponse.SC_OK, Map.of("id", a.getId(), "status", a.getStatus()));
    }
}
