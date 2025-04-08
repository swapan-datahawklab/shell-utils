package com.example.shelldemo.monitoring;

import com.example.shelldemo.monitoring.collector.CommandMetricsCollector;
import com.example.shelldemo.monitoring.collector.JvmMetricsCollector;
import com.example.shelldemo.monitoring.collector.SystemMetricsCollector;
import com.example.shelldemo.monitoring.model.MetricCollector;
import com.example.shelldemo.monitoring.model.MetricEvent;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsManager {
    private static final Logger log = LoggerFactory.getLogger(MetricsManager.class);
    private final MeterRegistry registry;
    private final List<MetricCollector> collectors;
    private final ScheduledExecutorService scheduler;
    private boolean isRunning;

    public MetricsManager(MeterRegistry registry) {
        this.registry = registry;
        this.collectors = new ArrayList<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.isRunning = false;
    }

    public void start() {
        if (isRunning) {
            return;
        }

        // Add default collectors
        collectors.add(new SystemMetricsCollector(registry));
        collectors.add(new CommandMetricsCollector(registry));
        collectors.add(new JvmMetricsCollector(registry));

        // Schedule metric collection
        scheduler.scheduleAtFixedRate(this::collectMetrics, 0, 1, TimeUnit.SECONDS);
        isRunning = true;
        log.info("Metrics collection started");
    }

    public void stop() {
        if (!isRunning) {
            return;
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        isRunning = false;
        log.info("Metrics collection stopped");
    }

    private void collectMetrics() {
        try {
            for (MetricCollector collector : collectors) {
                MetricEvent.Builder builder = new MetricEvent.Builder();
                collector.collect(builder);
                MetricEvent event = builder.build();
                processEvent(event);
            }
        } catch (Exception e) {
            log.error("Error collecting metrics", e);
        }
    }

    private void processEvent(MetricEvent event) {
        // Process the metric event (e.g., store in database, send to monitoring system)
        log.debug("Processing metric event: {}", event);
    }

    public void addCollector(MetricCollector collector) {
        collectors.add(collector);
    }

    public void loadCustomCollectors() {
        // Load custom collectors from configuration or classpath
        // This is a placeholder for actual implementation
    }
}