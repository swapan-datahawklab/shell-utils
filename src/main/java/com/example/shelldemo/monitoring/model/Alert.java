package com.example.shelldemo.monitoring.model;

public class Alert {
    private final AlertRule rule;
    private final MetricEvent event;
    private final String message;

    public Alert(AlertRule rule, MetricEvent event) {
        this.rule = rule;
        this.event = event;
        this.message = String.format(rule.getMessage(), event.getValue());
    }

    public AlertRule getRule() { return rule; }
    public MetricEvent getEvent() { return event; }
    public String getMessage() { return message; }
} 