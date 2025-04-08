package com.example.shelldemo.monitoring.collector;

import com.example.shelldemo.monitoring.model.MetricCollector;
import com.example.shelldemo.monitoring.model.MetricEvent;
import io.micrometer.core.instrument.MeterRegistry;

public class JvmMetricsCollector implements MetricCollector {
    private final MeterRegistry registry;

    public JvmMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "jvm";
    }

    @Override
    public void collect(MetricEvent.Builder builder) {
        builder.collector("jvm")
               .name("memory.used")
               .value(registry.get("jvm.memory.used").gauge().value())
               .tag("area", "heap");
    }
} 