package com.example.shelldemo.exception;

public class AnalysisExecutionException extends Exception {
    public AnalysisExecutionException(String message) {
        super(message);
    }

    public AnalysisExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
} 