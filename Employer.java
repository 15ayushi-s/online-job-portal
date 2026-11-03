package com.jobhub.model;

import java.util.List;

public class Employer extends User {
    private static final long serialVersionUID = 1L;
    public Employer(Long id, String name, String email, String hash) { super(id, name, email, hash); }
    @Override public Role getRole() { return Role.EMPLOYER; }
    @Override public List<String> permissions() { return List.of("POST_JOB", "DELETE_OWN_JOB", "MANAGE_APPLICANTS"); }
}
