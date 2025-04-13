package com.example.shelldemo.exception;

public class BaseException extends Exception {
    private static final long serialVersionUID = 1L;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseException(String message, String additionalInfo, Throwable cause) {
        super(message + " - " + additionalInfo, cause);
    }

    public BaseException(String message, String additionalInfo, String collectorName, Throwable cause) {
        super(message + " - " + additionalInfo + " - " + collectorName, cause);
    }
} 