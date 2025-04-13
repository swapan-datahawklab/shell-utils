package com.example.shelldemo.cli.exception;
import com.example.shelldemo.exception.BaseException;

public class CommandExecutionException extends BaseException  {
    private static final long serialVersionUID = 1L;

    public CommandExecutionException(String message) {
        super(message);
    }

    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandExecutionException(String message, String additionalInfo, Throwable cause) {
        super(message, additionalInfo, cause);
    }

    public CommandExecutionException(String message, String additionalInfo, String collectorName, Throwable cause) {
        super(message, additionalInfo, collectorName, cause);
    }
} 