package com.jobhub.service;

import com.jobhub.dao.ApplicationDao;
import com.jobhub.dao.JobDao;
import com.jobhub.exception.ConflictException;
import com.jobhub.exception.ForbiddenException;
import com.jobhub.exception.NotFoundException;
import com.jobhub.exception.ValidationException;
import com.jobhub.model.Application;
import com.jobhub.model.ApplicationStatus;
import com.jobhub.model.ApplicationView;
import com.jobhub.model.Job;
import com.jobhub.model.JobStatus;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.util.Validation;
import java.util.List;

public class ApplicationService {
    private final ApplicationDao apps;
    private final JobDao jobs;

    public ApplicationService(ApplicationDao apps, JobDao jobs) { this.apps = apps; this.jobs = jobs; }

    public Application apply(User actor, Long jobId, String resumeUrl) {
        if (!actor.hasRole(Role.JOB_SEEKER)) throw new ForbiddenException("Only job seekers can apply");
        if (jobId == null) throw new ValidationException("jobId is required");
        String resume = Validation.optionalUrl(resumeUrl, "Resume URL");
        Job job = jobs.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found"));
        if (job.getStatus() != JobStatus.APPROVED) throw new ValidationException("This job is not open for applications");
        if (apps.exists(jobId, actor.getId())) throw new ConflictException("You have already applied to this job");
        return apps.save(new Application(jobId, actor.getId(), resume));
    }

    public List<ApplicationView> forSeeker(User actor, Long seekerId) {
        if (!actor.isAdmin() && !(actor.hasRole(Role.JOB_SEEKER) && actor.getId().equals(seekerId)))
            throw new ForbiddenException("You can only view your own applications");
        return apps.findViewsBySeeker(seekerId);
    }

    public List<ApplicationView> forJob(User actor, Long jobId) {
        requireJobOwnerOrAdmin(actor, jobs.findById(jobId).orElseThrow(() -> new NotFoundException("Job not found")));
        return apps.findViewsByJob(jobId);
    }

    public Application setStatus(User actor, Long applicationId, String value) {
        ApplicationStatus status = ApplicationStatus.parse(value);
        Application a = apps.findById(applicationId).orElseThrow(() -> new NotFoundException("Application not found"));
        Job job = jobs.findById(a.getJobId()).orElseThrow(() -> new NotFoundException("Job not found"));
        requireJobOwnerOrAdmin(actor, job);
        a.setStatus(status);
        return apps.save(a);
    }

    private void requireJobOwnerOrAdmin(User actor, Job job) {
        boolean owner = actor.hasRole(Role.EMPLOYER) && actor.getId().equals(job.getEmployerId());
        if (!actor.isAdmin() && !owner) throw new ForbiddenException("Only the employer of this job or an admin can do that");
    }
}
