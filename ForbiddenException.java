package com.jobhub.exception;

public class ForbiddenException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public ForbiddenException(String message) { super(403, message); }
}
