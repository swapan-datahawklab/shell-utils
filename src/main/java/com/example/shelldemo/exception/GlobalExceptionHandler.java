package com.example.shelldemo.exception;

import java.util.function.Function;

public class GlobalExceptionHandler {
    private GlobalExceptionHandler() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    private static class DefaultException extends BaseException {
        private static final long serialVersionUID = 1L;

        public DefaultException(String message, Throwable cause) {
            super(message, cause);
        }

        public DefaultException(String message, String additionalInfo, Throwable cause) {
            super(message, additionalInfo, cause);
        }

        public DefaultException(String message, String additionalInfo, String collectorName, Throwable cause) {
            super(message, additionalInfo, collectorName, cause);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseException> T createException(String message, Throwable cause) {
        return (T) new DefaultException(message, cause);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseException> T createException(String message, String additionalInfo, Throwable cause) {
        return (T) new DefaultException(message, additionalInfo, cause);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BaseException> T createException(String message, String additionalInfo, String collectorName, Throwable cause) {
        return (T) new DefaultException(message, additionalInfo, collectorName, cause);
    }

    public static <T extends BaseException> Function<Throwable, T> handleException(String message) {
        return throwable -> createException(message, throwable);
    }
    
    public static <T extends BaseException> Function<Throwable, T> handleException(String message, String additionalInfo) {
        return throwable -> createException(message, additionalInfo, throwable);
    }
    
    public static <T extends BaseException> Function<Throwable, T> handleException(String message, String additionalInfo, String collectorName) {
        return throwable -> createException(message, additionalInfo, collectorName, throwable);
    }
} 