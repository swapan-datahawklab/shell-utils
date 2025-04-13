package com.example.shelldemo.cli.exception;

public class ScriptException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ScriptException(String message) {
        super(message);
    }

    public ScriptException(String message, Throwable cause) {
        super(message, cause);
    }
} 