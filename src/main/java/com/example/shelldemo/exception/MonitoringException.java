package com.example.shelldemo.exception;

public class MonitoringException extends BaseException {
    private static final long serialVersionUID = 1L;

    public MonitoringException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, context, additionalInfo, cause);
    }
} 