package com.example.shelldemo.exception;

public class DatabaseException extends BaseException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
} 