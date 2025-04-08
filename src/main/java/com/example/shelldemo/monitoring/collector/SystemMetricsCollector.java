package com.example.shelldemo.monitoring.collector;

import com.example.shelldemo.monitoring.model.MetricCollector;
import com.example.shelldemo.monitoring.model.MetricEvent;
import io.micrometer.core.instrument.MeterRegistry;

public class SystemMetricsCollector implements MetricCollector {
    private final MeterRegistry registry;

    public SystemMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "system";
    }

    @Override
    public void collect(MetricEvent.Builder builder) {
        // Add system-specific metrics collection logic here
        builder.collector("system")
               .name("system.cpu.usage")
               .value(registry.get("system.cpu.usage").gauge().value());
    }
} 