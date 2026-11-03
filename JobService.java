package com.jobhub.service;

import com.jobhub.dao.JobDao;
import com.jobhub.exception.ForbiddenException;
import com.jobhub.exception.NotFoundException;
import com.jobhub.model.Job;
import com.jobhub.model.JobStatus;
import com.jobhub.model.Role;
import com.jobhub.model.User;
import com.jobhub.util.Validation;
import java.util.List;

public class JobService {
    private final JobDao jobs;

    public JobService(JobDao jobs) { this.jobs = jobs; }

    public List<Job> searchApproved(String q) { return jobs.searchApproved(q); }

    public List<Job> listAll(User actor) {
        if (!actor.isAdmin()) throw new ForbiddenException("Admins only");
        return jobs.findAll();
    }

    public List<Job> listForEmployer(User actor, Long employerId) {
        if (!actor.isAdmin() && !(actor.hasRole(Role.EMPLOYER) && actor.getId().equals(employerId)))
            throw new ForbiddenException("You can only view your own listings");
        return jobs.findByEmployerId(employerId);
    }

    /** The employer is always the logged-in user; new jobs always start PENDING. */
    public Job create(User actor, Job input) {
        if (!actor.hasRole(Role.EMPLOYER)) throw new ForbiddenException("Only employers can post jobs");
        Job j = new Job();
        j.setTitle(Validation.required(input.getTitle(), "Title", 255));
        j.setCompany(Validation.required(input.getCompany(), "Company", 255));
        j.setLocation(Validation.required(input.getLocation(), "Location", 255));
        j.setType(Validation.optional(input.getType(), "Type", 50));
        j.setSalary(Validation.optional(input.getSalary(), "Salary", 255));
        j.setSkills(Validation.optional(input.getSkills(), "Skills", 255));
        j.setDescription(Validation.optional(input.getDescription(), "Description", 2000));
        j.setEmployerId(actor.getId());
        j.setStatus(JobStatus.PENDING);
        return jobs.save(j);
    }

    public Job setStatus(User actor, Long id, String value) {
        if (!actor.isAdmin()) throw new ForbiddenException("Only admins can approve or reject jobs");
        JobStatus status = JobStatus.parse(value);
        Job j = jobs.findById(id).orElseThrow(() -> new NotFoundException("Job not found"));
        j.setStatus(status);
        return jobs.save(j);
    }

    public void delete(User actor, Long id) {
        Job j = jobs.findById(id).orElseThrow(() -> new NotFoundException("Job not found"));
        boolean owner = actor.hasRole(Role.EMPLOYER) && actor.getId().equals(j.getEmployerId());
        if (!actor.isAdmin() && !owner) throw new ForbiddenException("You can only delete your own jobs");
        jobs.deleteById(id);
    }
}
