package com.bwabwayo.app.domain.user.exception;

public class InsufficientPointException extends RuntimeException {
    public InsufficientPointException(String message) {
        super(message);
    }
}

