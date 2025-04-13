package com.example.shelldemo.analysis.exception;

import com.example.shelldemo.exception.BaseException;

public class AgentException extends BaseException {
    private static final long serialVersionUID = 1L;

    public AgentException(String message) {
        super(message);
    }

    public AgentException(String message, Throwable cause) {
        super(message, cause);
    }

    public AgentException(String message, String additionalInfo, Throwable cause) {
        super(message, additionalInfo, cause);
    }

    public AgentException(String message, String additionalInfo, String collectorName, Throwable cause) {
        super(message, additionalInfo, collectorName, cause);
    }
} 