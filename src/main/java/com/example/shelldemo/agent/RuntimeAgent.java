package com.example.shelldemo.agent;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.shelldemo.exception.RuntimeAnalysisException;
import com.example.shelldemo.exception.GlobalExceptionHandler;
import com.example.shelldemo.exception.MonitoringException;

public final class RuntimeAgent {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAgent.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    
    private RuntimeAgent() {
        throw new IllegalStateException("Utility class");
    }
    
    @SuppressWarnings({"java:S1172", "unused"})
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        startMonitoring();
    }
    
    @SuppressWarnings({"java:S1172", "unused"})
    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        startMonitoring();
    }
    
    private static void startMonitoring() throws MonitoringException {
        try {
            // Schedule periodic analysis
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    analyzeRuntime();
                } catch (Exception e) {
                    log.error("Error during runtime analysis - Heap: {}%, Threads: {} - {}: {}", 
                        String.format("%.2f", memoryBean.getHeapMemoryUsage().getUsed() * 100.0 / memoryBean.getHeapMemoryUsage().getMax()),
                        threadBean.getThreadCount(),
                        e.getClass().getSimpleName(),
                        e.getMessage(),
                        e);
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw GlobalExceptionHandler.handleException(
                MonitoringException.class,
                "Runtime Monitoring",
                "Failed to start monitoring service",
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
            throw GlobalExceptionHandler.handleException(
                RuntimeAnalysisException.class,
                "Runtime Analysis",
                "Failed to analyze runtime environment",
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