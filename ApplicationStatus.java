package com.jobhub.model;

import com.jobhub.exception.ValidationException;
import java.util.Locale;

public enum ApplicationStatus {
    APPLIED, SHORTLISTED, REJECTED, HIRED;

    public static ApplicationStatus parse(String raw) {
        try { return valueOf(raw.trim().toUpperCase(Locale.ROOT)); }
        catch (RuntimeException e) { throw new ValidationException("Application status must be APPLIED, SHORTLISTED, REJECTED or HIRED"); }
    }
}
