package com.jobhub.exception;

/** Base class of all application errors; carries the HTTP status the web layer should answer with. */
public class JobHubException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final int status;

    public JobHubException(int status, String message) { super(message); this.status = status; }
    public JobHubException(int status, String message, Throwable cause) { super(message, cause); this.status = status; }

    public int getStatus() { return status; }
}
