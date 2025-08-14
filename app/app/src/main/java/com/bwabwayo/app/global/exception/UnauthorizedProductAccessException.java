package com.bwabwayo.app.global.exception;

public class UnauthorizedProductAccessException extends RuntimeException {
    public UnauthorizedProductAccessException(String message) {
        super(message);
    }
}
