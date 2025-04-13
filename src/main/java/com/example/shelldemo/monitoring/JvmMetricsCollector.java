package com.example.shelldemo.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

public class JvmMetricsCollector implements MetricCollector {
    private static final Logger log = LoggerFactory.getLogger(JvmMetricsCollector.class);
    private final MeterRegistry registry;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;

    public JvmMetricsCollector(MeterRegistry registry) {
        this.registry = registry;
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public String getName() {
        return "jvm";
    }

    @Override
    public void collect(MetricEvent.Builder builder) {
        try {
            builder.collector("jvm")
                   .name("jvm.memory.used")
                   .value(memoryBean.getHeapMemoryUsage().getUsed());
            
            builder.collector("jvm")
                   .name("jvm.threads.live")
                   .value(threadBean.getThreadCount());
            
            builder.collector("jvm")
                   .name("jvm.gc.time")
                   .value(registry.get("jvm.gc.time").timer().totalTime(TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            log.error("Failed to collect JVM metrics: {}", e.getMessage());
            // Don't throw, just log as this is a monitoring operation
        }
    }
} 