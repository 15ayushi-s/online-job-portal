package com.jobhub.util;

import com.jobhub.exception.ValidationException;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Validation {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern URL = Pattern.compile("^https?://\\S+$", Pattern.CASE_INSENSITIVE);

    private Validation() {}

    /** Trimmed, non-blank, at most max characters. */
    public static String required(String v, String label, int max) {
        String t = v == null ? "" : v.trim();
        if (t.isEmpty()) throw new ValidationException(label + " is required");
        if (t.length() > max) throw new ValidationException(label + " must be at most " + max + " characters");
        return t;
    }

    /** Trimmed; null if blank; at most max characters. */
    public static String optional(String v, String label, int max) {
        String t = v == null ? "" : v.trim();
        if (t.length() > max) throw new ValidationException(label + " must be at most " + max + " characters");
        return t.isEmpty() ? null : t;
    }

    public static String email(String raw) {
        String e = required(raw, "Email", 255).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(e).matches()) throw new ValidationException("Enter a valid email address");
        return e;
    }

    /** Only http(s) links are accepted (blocks javascript: URLs). */
    public static String optionalUrl(String raw, String label) {
        String u = optional(raw, label, 255);
        if (u != null && !URL.matcher(u).matches()) throw new ValidationException(label + " must start with http:// or https://");
        return u;
    }
}
