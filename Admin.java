package com.jobhub.model;

import java.util.List;

public class Admin extends User {
    private static final long serialVersionUID = 1L;
    public Admin(Long id, String name, String email, String hash) { super(id, name, email, hash); }
    @Override public Role getRole() { return Role.ADMIN; }
    @Override public List<String> permissions() { return List.of("MANAGE_USERS", "APPROVE_JOBS", "DELETE_ANY_JOB", "MANAGE_ANY_APPLICATION"); }
}
