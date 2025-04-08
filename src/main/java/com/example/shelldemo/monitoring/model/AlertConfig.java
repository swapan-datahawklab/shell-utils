package com.example.shelldemo.monitoring.model;

import java.util.List;

public class AlertConfig {
    private List<AlertRule> rules;

    public List<AlertRule> getRules() {
        return rules;
    }

    public void setRules(List<AlertRule> rules) {
        this.rules = rules;
    }
} 