package com.jobhub.exception;

public class ValidationException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public ValidationException(String message) { super(400, message); }
}
