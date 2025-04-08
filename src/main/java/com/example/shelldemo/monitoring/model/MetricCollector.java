package com.example.shelldemo.monitoring.model;

/**
 * Interface for collecting metrics.
 */
public interface MetricCollector {
    /**
     * Returns the name of this collector.
     * @return the collector name
     */
    String getName();

    /**
     * Collects metrics and adds them to the provided builder.
     * @param builder the metric event builder
     */
    void collect(MetricEvent.Builder builder);
} 