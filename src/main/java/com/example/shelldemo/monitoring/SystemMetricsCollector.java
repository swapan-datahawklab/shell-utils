package com.example.shelldemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemMetricsCollector implements MetricCollector {
    private static final Logger log = LoggerFactory.getLogger(SystemMetricsCollector.class);
    private static final String COLLECTOR_NAME = "system";
    private final MeterRegistry registry;

    public SystemMetricsCollector(MeterRegistry registry) {
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
                   .name("system.cpu.usage")
                   .value(registry.get("system.cpu.usage").gauge().value());
            
            builder.collector(COLLECTOR_NAME)
                   .name("system.memory.used")
                   .value(registry.get("system.memory.used").gauge().value());
            
            builder.collector(COLLECTOR_NAME)
                   .name("system.disk.usage")
                   .value(registry.get("system.disk.usage").gauge().value());
        } catch (Exception e) {
            log.error("Failed to collect system metrics for collector {}: {}", COLLECTOR_NAME, e.getMessage(), e);
        }
    }
} 