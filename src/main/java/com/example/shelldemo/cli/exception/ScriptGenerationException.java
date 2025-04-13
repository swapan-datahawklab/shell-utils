package com.example.shelldemo.cli.exception;

public class ScriptGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ScriptGenerationException(String message) {
        super(message);
    }

    public ScriptGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 