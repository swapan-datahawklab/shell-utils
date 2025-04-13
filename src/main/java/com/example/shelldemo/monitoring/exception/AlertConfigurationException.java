package com.example.shelldemo.monitoring.exception;

import com.example.shelldemo.exception.BaseException;

public class AlertConfigurationException extends BaseException {
    private static final long serialVersionUID = 1L;
    
    public AlertConfigurationException(String message, String metric) {
        super(message, 
            "Alert configuration error for metric: " + metric,
            null,
            null);
    }
} 