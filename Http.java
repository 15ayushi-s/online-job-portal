package com.jobhub.util;

import com.jobhub.exception.JobHubException;
import com.jobhub.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

/** Small helpers shared by the servlets and the filter. */
public final class Http {
    private static final int MAX_BODY_CHARS = 100_000;

    private Http() {}

    /** Reads the JSON request body. Requires Content-Type: application/json (blocks cross-site form posts). */
    public static Map<String, Object> readJson(HttpServletRequest req) throws IOException {
        String ct = req.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("application/json"))
            throw new JobHubException(415, "Content-Type must be application/json");
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = req.getReader()) {
            char[] buf = new char[2048];
            int n;
            while ((n = r.read(buf)) != -1) {
                sb.append(buf, 0, n);
                if (sb.length() > MAX_BODY_CHARS) throw new JobHubException(413, "Request body too large");
            }
        }
        if (sb.toString().isBlank()) throw new ValidationException("Request body is required");
        return Json.parseObject(sb.toString());
    }

    /** Reads a field as text; numbers/booleans are converted, objects/arrays are rejected. */
    public static String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof String || v instanceof Number || v instanceof Boolean) return v.toString();
        throw new ValidationException("Field '" + key + "' must be text");
    }

    public static Long id(String segment) {
        try { return Long.valueOf(segment); }
        catch (NumberFormatException e) { throw new ValidationException("Invalid id: " + segment); }
    }

    public static void write(HttpServletResponse res, int status, Object body) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(Json.write(body));
    }

    public static void error(HttpServletResponse res, int status, String message) throws IOException {
        write(res, status, Map.of("message", message));
    }

    public static void noContent(HttpServletResponse res) {
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
