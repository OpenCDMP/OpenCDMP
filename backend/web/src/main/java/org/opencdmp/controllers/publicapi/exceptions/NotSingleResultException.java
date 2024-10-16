package org.opencdmp.controllers.publicapi.exceptions;


public class NotSingleResultException extends RuntimeException {
    public NotSingleResultException() {
        super();
    }

    public NotSingleResultException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSingleResultException(String message) {
        super(message);
    }

    public NotSingleResultException(Throwable cause) {
        super(cause);
    }
}