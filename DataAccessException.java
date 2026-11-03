package com.jobhub.exception;

/** Wraps a checked java.sql.SQLException so DAO callers don't have to handle it. */
public class DataAccessException extends JobHubException {
    private static final long serialVersionUID = 1L;
    public DataAccessException(String message, Throwable cause) { super(500, message, cause); }
}
