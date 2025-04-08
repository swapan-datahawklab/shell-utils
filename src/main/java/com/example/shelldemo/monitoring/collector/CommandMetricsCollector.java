package com.example.shelldemo.monitoring.collector;

import com.example.shelldemo.monitoring.model.MetricCollector;
import com.example.shelldemo.monitoring.model.MetricEvent;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.TimeUnit;

public class CommandMetricsCollector implements MetricCollector {
    private final MeterRegistry registry;

    public CommandMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getName() {
        return "command";
    }

    @Override
    public void collect(MetricEvent.Builder builder) {
        // Add command-specific metrics collection logic here
        builder.collector("command")
               .name("command.execution.time")
               .value(registry.get("command.execution.time").timer().totalTime(TimeUnit.MILLISECONDS));
    }
} 