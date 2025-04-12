package com.example.shelldemo.exception;

public class AgentException extends BaseException {
    private static final long serialVersionUID = 1L;

    public AgentException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, context, additionalInfo, cause);
    }
} 