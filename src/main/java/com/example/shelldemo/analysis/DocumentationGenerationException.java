package com.example.shelldemo.analysis;

/**
 * Exception thrown when documentation generation fails.
 */
public class DocumentationGenerationException extends Exception {
    public DocumentationGenerationException(String message) {
        super(message);
    }
    
    public DocumentationGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 