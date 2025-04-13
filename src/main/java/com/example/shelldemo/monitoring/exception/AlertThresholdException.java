package com.example.shelldemo.monitoring.exception;

import com.example.shelldemo.exception.BaseException;

public class AlertThresholdException extends BaseException {
    private static final long serialVersionUID = 1L;
    
    public AlertThresholdException(String message, String metric, double value, double threshold) {
        super(message, 
            "Alert threshold violation for metric: " + metric,
            String.format("Current value: %.2f, Threshold: %.2f", value, threshold),
            null);
    }
} 