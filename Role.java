package com.jobhub.model;

import com.jobhub.exception.ValidationException;
import java.util.Locale;

public enum Role {
    ADMIN, EMPLOYER, JOB_SEEKER;

    public static Role parse(String raw) {
        if (raw == null) throw new ValidationException("Role is required");
        try { return valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new ValidationException("Unknown role: " + raw); }
    }
}
