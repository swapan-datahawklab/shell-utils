package com.example.shelldemo.monitoring;

public class Alert {
    private final AlertRule rule;
    private final MetricEvent event;

    public Alert(AlertRule rule, MetricEvent event) {
        this.rule = rule;
        this.event = event;
    }

    public AlertRule getRule() {
        return rule;
    }

    public MetricEvent getEvent() {
        return event;
    }
} 