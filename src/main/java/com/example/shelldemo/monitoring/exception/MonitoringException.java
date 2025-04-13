package com.example.shelldemo.monitoring.exception;
import com.example.shelldemo.exception.BaseException;

public class MonitoringException extends BaseException {
    private static final long serialVersionUID = 1L;

    public MonitoringException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, context, additionalInfo, cause);
    }
} 