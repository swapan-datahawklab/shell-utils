package com.example.shelldemo.monitoring.model;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

public class MetricEvent {
    private final String collector;
    private final String name;
    private final double value;
    private final Map<String, String> tags;
    private final Instant timestamp;

    public MetricEvent(String collector, String name, double value, Map<String, String> tags, Instant timestamp) {
        this.collector = collector;
        this.name = name;
        this.value = value;
        this.tags = tags;
        this.timestamp = timestamp;
    }

    public String getCollector() { return collector; }
    public String getName() { return name; }
    public double getValue() { return value; }
    public Map<String, String> getTags() { return tags; }
    public Instant getTimestamp() { return timestamp; }

    public static class Builder {
        private String collector;
        private String name;
        private double value;
        private Map<String, String> tags = new HashMap<>();
        private Instant timestamp = Instant.now();

        public Builder collector(String collector) { this.collector = collector; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder value(double value) { this.value = value; return this; }
        public Builder tag(String key, String value) { tags.put(key, value); return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public MetricEvent build() { return new MetricEvent(collector, name, value, tags, timestamp); }
    }
} 