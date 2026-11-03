package com.jobhub.model;

import com.jobhub.exception.ValidationException;
import java.util.Locale;

public enum JobStatus {
    PENDING, APPROVED, REJECTED;

    public static JobStatus parse(String raw) {
        try { return valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException e) { throw new ValidationException("Job status must be PENDING, APPROVED or REJECTED"); }
    }
}
