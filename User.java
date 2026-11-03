package com.jobhub.model;

import com.jobhub.util.JsonSerializable;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Abstract parent of the three account types. Each subclass supplies its own
 * role and permission list (polymorphism); callers only deal with User.
 */
public abstract class User implements Serializable, JsonSerializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String email;
    private String passwordHash;

    protected User(Long id, String name, String email, String passwordHash) {
        this.id = id; this.name = name; this.email = email; this.passwordHash = passwordHash;
    }

    /** Factory: returns the right subclass for a role. */
    public static User create(Long id, String name, String email, String passwordHash, Role role) {
        return switch (role) {
            case ADMIN      -> new Admin(id, name, email, passwordHash);
            case EMPLOYER   -> new Employer(id, name, email, passwordHash);
            case JOB_SEEKER -> new JobSeeker(id, name, email, passwordHash);
        };
    }

    public abstract Role getRole();
    public abstract List<String> permissions();

    public boolean hasRole(Role r) { return getRole() == r; }
    public boolean isAdmin() { return getRole() == Role.ADMIN; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /** The password hash is deliberately never part of the JSON. */
    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", name);
        m.put("email", email);
        m.put("role", getRole().name());
        m.put("permissions", permissions());
        return m;
    }

    @Override public boolean equals(Object o) {
        return o instanceof User u && id != null && Objects.equals(id, u.id);
    }
    @Override public int hashCode() { return Objects.hashCode(id); }
    @Override public String toString() { return getRole() + "#" + id + "(" + email + ")"; }
}
