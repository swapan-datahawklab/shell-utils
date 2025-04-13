package com.example.shelldemo.monitoring.exception;

import com.example.shelldemo.exception.BaseException;

public class MonitoringCollectorException extends BaseException {
    private static final long serialVersionUID = 1L;

    public MonitoringCollectorException(String message, String collectorName, String additionalInfo, Throwable cause) {
        super(message, "Monitoring Collector: " + collectorName, additionalInfo, cause);
    }

    public MonitoringCollectorException(String message, String collectorName, String additionalInfo) {
        this(message, collectorName, additionalInfo, null);
    }
} 