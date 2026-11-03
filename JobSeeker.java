package com.jobhub.model;

import java.util.List;

public class JobSeeker extends User {
    private static final long serialVersionUID = 1L;
    public JobSeeker(Long id, String name, String email, String hash) { super(id, name, email, hash); }
    @Override public Role getRole() { return Role.JOB_SEEKER; }
    @Override public List<String> permissions() { return List.of("SEARCH_JOBS", "APPLY_TO_JOB", "TRACK_APPLICATIONS"); }
}
