package com.example.shelldemo.exception;

public class DocumentationGenerationException extends Exception {
    public DocumentationGenerationException(String message) {
        super(message);
    }

    public DocumentationGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 