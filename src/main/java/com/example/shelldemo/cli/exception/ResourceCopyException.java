package com.example.shelldemo.cli.exception;

/**
 * Exception thrown when there is an error copying static resources.
 */
public class ResourceCopyException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new ResourceCopyException with the specified detail message.
     *
     * @param message the detail message
     */
    public ResourceCopyException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceCopyException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ResourceCopyException(String message, Throwable cause) {
        super(message, cause);
    }
} 