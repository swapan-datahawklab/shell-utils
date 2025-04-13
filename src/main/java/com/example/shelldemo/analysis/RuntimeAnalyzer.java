package com.example.shelldemo.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import javax.management.ObjectName;
import java.util.stream.Collectors;

@Aspect
public class RuntimeAnalyzer {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAnalyzer.class);
    private final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
    private final CommandService commandService;
    private boolean isRunning = false;
    private final Map<String, CommandMetrics> commandMetrics = new ConcurrentHashMap<>();
    private final Map<String, CommandUsageStats> usageStats = new ConcurrentHashMap<>();

    public RuntimeAnalyzer(CommandService commandService) {
        this.commandService = commandService;
    }

    public void start() {
        if (!isRunning) {
            try {
                // Attach agent to current process
                String pid = String.valueOf(ProcessHandle.current().pid());
                String agentJarPath = System.getProperty("agent.jar.path", "runtime-agent.jar");
                
                AgentAttacher.attachToProcess(pid, agentJarPath);
                RuntimeAgent.initialize();
                
                isRunning = true;
                log.info("Runtime analysis started for process {}", pid);
            } catch (Exception e) {
                log.error("Failed to start runtime analysis", e);
            }
        }
    }

    public void stop() {
        if (isRunning) {
            RuntimeAgent.shutdown();
            isRunning = false;
            log.info("Runtime analysis stopped");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    @Around("execution(* com.example.shelldemo.cli.*.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!isRunning) {
            return point.proceed();
        }

        String commandName = point.getSignature().getDeclaringType().getSimpleName();
        CommandData commandData = commandService.getCommandData(commandName);
        
        if (commandData == null) {
            return point.proceed();
        }

        CommandMetrics metrics = commandMetrics.computeIfAbsent(commandName, 
            k -> new CommandMetrics());
        CommandUsageStats stats = usageStats.computeIfAbsent(commandName, 
            k -> new CommandUsageStats());

        long startTime = System.nanoTime();
        try {
            Object result = point.proceed();
            metrics.recordSuccess();
            stats.recordUsage();
            return result;
        } catch (Exception e) {
            metrics.recordError(e);
            stats.recordError();
            throw e;
        } finally {
            metrics.recordExecutionTime(System.nanoTime() - startTime);
            stats.recordExecutionTime(System.nanoTime() - startTime);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Command-specific metrics
        metrics.put("commandMetrics", commandMetrics.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStats())));
        metrics.put("usageStats", usageStats.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStats())));
        
        // JVM metrics
        try {
            metrics.put("heapMemoryUsage", 
                mBeanServer.getAttribute(new ObjectName("java.lang:type=Memory"), 
                "HeapMemoryUsage"));
            metrics.put("threadCount", 
                mBeanServer.getAttribute(new ObjectName("java.lang:type=Threading"), 
                "ThreadCount"));
        } catch (Exception e) {
            log.error("Error collecting JVM metrics", e);
        }
        
        return metrics;
    }

    private static class CommandMetrics {
        private final AtomicLong executionCount = new AtomicLong();
        private final AtomicLong errorCount = new AtomicLong();
        private final AtomicLong totalExecutionTime = new AtomicLong();
        private final Map<String, AtomicLong> errorTypes = new ConcurrentHashMap<>();
        private final List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());

        public void recordSuccess() {
            executionCount.incrementAndGet();
        }

        public void recordError(Exception e) {
            errorCount.incrementAndGet();
            errorTypes.computeIfAbsent(e.getClass().getSimpleName(), 
                k -> new AtomicLong()).incrementAndGet();
        }

        public void recordExecutionTime(long nanos) {
            totalExecutionTime.addAndGet(nanos);
            executionTimes.add(nanos);
        }

        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("executionCount", executionCount.get());
            stats.put("errorCount", errorCount.get());
            stats.put("averageExecutionTime", 
                executionCount.get() > 0 ? 
                totalExecutionTime.get() / executionCount.get() / 1_000_000.0 : 0);
            stats.put("errorTypes", errorTypes);
            
            // Calculate percentiles
            if (!executionTimes.isEmpty()) {
                List<Long> sorted = new ArrayList<>(executionTimes);
                Collections.sort(sorted);
                stats.put("p50", calculatePercentile(sorted, 50));
                stats.put("p90", calculatePercentile(sorted, 90));
                stats.put("p99", calculatePercentile(sorted, 99));
            }
            
            return stats;
        }

        private double calculatePercentile(List<Long> sorted, int percentile) {
            int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
            return sorted.get(index) / 1_000_000.0;
        }
    }

    private static class CommandUsageStats {
        private final AtomicLong usageCount = new AtomicLong();
        private final AtomicLong errorCount = new AtomicLong();
        private final List<Long> executionTimes = Collections.synchronizedList(new ArrayList<>());

        public void recordUsage() {
            usageCount.incrementAndGet();
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public void recordExecutionTime(long nanos) {
            executionTimes.add(nanos);
        }

        public Map<String, Object> getStats() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("usageCount", usageCount.get());
            stats.put("errorCount", errorCount.get());
            if (!executionTimes.isEmpty()) {
                stats.put("averageExecutionTime", 
                    executionTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0);
            }
            return stats;
        }
    }
} 