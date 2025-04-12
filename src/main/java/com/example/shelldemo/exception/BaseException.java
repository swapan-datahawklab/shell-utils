package com.example.shelldemo.exception;

public abstract class BaseException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String context;
    private final String additionalInfo;

    protected BaseException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, cause);
        this.context = context;
        this.additionalInfo = additionalInfo;
    }

    public String getContext() {
        return context;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
} 