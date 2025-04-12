package com.example.shelldemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    public static <T extends BaseException> T handleException(String context, Exception e) {
        String errorMessage = String.format("Error in %s: %s", context, e.getMessage());
        log.error(errorMessage, e);
        return (T) new BaseException(errorMessage, context, "", e) {};
    }
    
    public static <T extends BaseException> T handleException(String context, String additionalInfo, Exception e) {
        String errorMessage = String.format("Error in %s - %s: %s", context, additionalInfo, e.getMessage());
        log.error(errorMessage, e);
        return (T) new BaseException(errorMessage, context, additionalInfo, e) {};
    }
    
    public static <T extends BaseException> T handleException(Class<T> exceptionClass, String context, String additionalInfo, Exception e) {
        String errorMessage = String.format("Error in %s - %s: %s", context, additionalInfo, e.getMessage());
        log.error(errorMessage, e);
        try {
            return exceptionClass.getConstructor(String.class, String.class, String.class, Throwable.class)
                    .newInstance(errorMessage, context, additionalInfo, e);
        } catch (Exception ex) {
            log.error("Failed to create exception instance", ex);
            return (T) new BaseException(errorMessage, context, additionalInfo, e) {};
        }
    }
} 