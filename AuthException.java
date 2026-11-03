package com.jobhub.exception;

public class AuthException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public AuthException(String message) { super(401, message); }
}
