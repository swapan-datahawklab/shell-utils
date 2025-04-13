package com.example.shelldemo.cli.exception;

public class ScriptFileExistsException extends ScriptException {
    private static final long serialVersionUID = 1L;

    public ScriptFileExistsException(String message) {
        super(message);
    }
} 