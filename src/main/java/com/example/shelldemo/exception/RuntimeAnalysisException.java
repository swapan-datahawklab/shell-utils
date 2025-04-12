package com.example.shelldemo.exception;

public class RuntimeAnalysisException extends BaseException {
    private static final long serialVersionUID = 1L;

    public RuntimeAnalysisException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, context, additionalInfo, cause);
    }
} 