package com.jobhub.model;

import java.time.LocalDateTime;

public class Application {
    private Long id;
    private Long jobId;
    private Long seekerId;
    private ApplicationStatus status = ApplicationStatus.APPLIED;
    private String resumeUrl;
    private LocalDateTime appliedAt = LocalDateTime.now();

    public Application() {}
    public Application(Long jobId, Long seekerId, String resumeUrl) {
        this.jobId = jobId; this.seekerId = seekerId; this.resumeUrl = resumeUrl;
    }

    public Long getId() { return id; }
    public void setId(Long v) { id = v; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long v) { jobId = v; }
    public Long getSeekerId() { return seekerId; }
    public void setSeekerId(Long v) { seekerId = v; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus v) { status = v; }
    public String getResumeUrl() { return resumeUrl; }
    public void setResumeUrl(String v) { resumeUrl = v; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime v) { appliedAt = v; }
}
