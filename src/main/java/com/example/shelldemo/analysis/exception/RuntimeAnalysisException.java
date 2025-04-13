package com.example.shelldemo.analysis.exception;
import com.example.shelldemo.exception.BaseException;

public class RuntimeAnalysisException extends BaseException {
    private static final long serialVersionUID = 1L;

    public RuntimeAnalysisException(String message, String context, String additionalInfo, Throwable cause) {
        super(message, context, additionalInfo, cause);
    }
} 