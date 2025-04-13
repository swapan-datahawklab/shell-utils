package com.example.shelldemo.cli.exception;

/**
 * Exception thrown when there is an error during command generation.
 */
public class CommandGenerationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new CommandGenerationException with the specified detail message.
     *
     * @param message the detail message
     */
    public CommandGenerationException(String message) {
        super(message);
    }

    /**
     * Constructs a new CommandGenerationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public CommandGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
} 