package com.example.shelldemo.cli.exception;

public class ScriptDirectoryException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptDirectoryException(String message) {
        super(message);
    }

    public ScriptDirectoryException(String message, Throwable cause) {
        super(message, cause);
    }
} 