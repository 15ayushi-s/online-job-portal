package com.jobhub.exception;

public class ConflictException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public ConflictException(String message) { super(409, message); }
}
