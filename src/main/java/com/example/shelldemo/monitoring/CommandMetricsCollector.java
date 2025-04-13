package com.example.shelldemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

public class CommandMetricsCollector implements MetricCollector {
    private static final Logger log = LoggerFactory.getLogger(CommandMetricsCollector.class);
    private static final String COLLECTOR_NAME = "command";
    private final MeterRegistry registry;

    public CommandMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return COLLECTOR_NAME;
    }

    @Override
    public void collect(MetricEvent.Builder builder) {
        try {
            builder.collector(COLLECTOR_NAME)
                   .name("command.execution.time")
                   .value(registry.get("command.execution.time").timer().totalTime(TimeUnit.MILLISECONDS));
            
            builder.collector(COLLECTOR_NAME)
                   .name("command.execution.count")
                   .value(registry.get("command.execution.count").counter().count());
                   
            builder.collector(COLLECTOR_NAME)
                   .name("command.error.count")
                   .value(registry.get("command.error.count").counter().count());
        } catch (Exception e) {
            log.error("Failed to collect command metrics: {}", e.getMessage());
            // Don't throw, just log as this is a monitoring operation
        }
    }
} 