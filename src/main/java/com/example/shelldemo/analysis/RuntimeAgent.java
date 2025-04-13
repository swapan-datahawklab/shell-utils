package com.example.shelldemo.analysis;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.analysis.exception.RuntimeAnalysisException;
import com.example.shelldemo.monitoring.exception.MonitoringException;

public final class RuntimeAgent {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAgent.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    private RuntimeAgent() {
        throw new IllegalStateException("Utility class");
    }
    
    @SuppressWarnings({"java:S1172", "unused"})
    public static void premain(String agentArgs, Instrumentation inst) throws MonitoringException {
        startMonitoring();
    }
    
    @SuppressWarnings({"java:S1172", "unused"})
    public static void agentmain(String agentArgs, Instrumentation inst) throws MonitoringException {
        startMonitoring();
    }
    
    public static void initialize() throws MonitoringException {
        startMonitoring();
    }
    
    private static void startMonitoring() throws MonitoringException {
        try {
            scheduler.scheduleAtFixedRate(
                () -> {
                    try {
                        analyzeRuntime();
                    } catch (RuntimeAnalysisException e) {
                        log.error("Runtime analysis failed: {}", e.getMessage(), e);
                    }
                },
                0, 5, TimeUnit.SECONDS
            );
            log.info("Runtime monitoring started");
        } catch (Exception e) {
            throw new MonitoringException(
                "Failed to start runtime monitoring",
                "Runtime monitoring initialization",
                "Check system resources and permissions",
                e
            );
        }
    }
    
    private static void analyzeRuntime() throws RuntimeAnalysisException {
        try {
            // Memory analysis
            long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryBean.getHeapMemoryUsage().getMax();
            double heapUsagePercent = (double) heapUsed / heapMax * 100;
            
            // Thread analysis
            int threadCount = threadBean.getThreadCount();
            
            if (log.isInfoEnabled()) {
                log.info("Runtime Analysis - Heap Usage: {}%, Threads: {}", 
                    String.format("%.2f", heapUsagePercent), threadCount);
            }
        } catch (Exception e) {
            throw new RuntimeAnalysisException(
                "Runtime analysis failed",
                "Runtime metrics collection",
                "Check system state and resource availability",
                e
            );
        }
    }
    
    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Shutdown interrupted - Final state: Heap: {}%, Threads: {} - {}: {}", 
                String.format("%.2f", memoryBean.getHeapMemoryUsage().getUsed() * 100.0 / memoryBean.getHeapMemoryUsage().getMax()),
                threadBean.getThreadCount(),
                e.getClass().getSimpleName(),
                e.getMessage(),
                e);
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 