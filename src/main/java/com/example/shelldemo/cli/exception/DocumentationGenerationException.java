package com.example.shelldemo.cli.exception;

public class DocumentationGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DocumentationGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 