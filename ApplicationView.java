package com.jobhub.model;

import com.jobhub.util.JsonSerializable;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only application row joined with job and seeker details (result of a SQL JOIN). */
public record ApplicationView(Long id, Long jobId, String jobTitle, String company,
                              Long seekerId, String seekerName, String seekerEmail,
                              ApplicationStatus status, String resumeUrl, LocalDateTime appliedAt)
        implements JsonSerializable {

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id); m.put("jobId", jobId); m.put("jobTitle", jobTitle); m.put("company", company);
        m.put("seekerId", seekerId); m.put("seekerName", seekerName); m.put("seekerEmail", seekerEmail);
        m.put("status", status.name()); m.put("resumeUrl", resumeUrl); m.put("appliedAt", appliedAt);
        return m;
    }
}
