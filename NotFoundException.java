package com.jobhub.exception;

public class NotFoundException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public NotFoundException(String message) { super(404, message); }
}
