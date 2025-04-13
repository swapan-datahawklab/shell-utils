package com.example.shelldemo.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.example.shelldemo.NotificationService;

public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);
    private final Map<String, AlertRule> rules = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    private boolean isRunning;

    public AlertManager(Path configPath, NotificationService notificationService) {
        this.notificationService = notificationService;
        this.isRunning = false;
        loadAlertRules(configPath);
    }

    public void start() {
        if (isRunning) {
            return;
        }
        isRunning = true;
        log.info("Alert monitoring started");
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        log.info("Alert monitoring stopped");
    }

    private void loadAlertRules(Path configPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AlertConfig config = mapper.readValue(configPath.toFile(), AlertConfig.class);
            config.getRules().forEach(rule -> rules.put(rule.getName(), rule));
            log.info("Loaded {} alert rules from {}", rules.size(), configPath);
        } catch (Exception e) {
            log.error("Failed to load alert rules from {}", configPath, e);
        }
    }

    public void checkMetric(MetricEvent event) {
        if (!isRunning) {
            return;
        }
        
        rules.values().stream()
            .filter(rule -> rule.matches(event))
            .forEach(rule -> {
                if (rule.isTriggered(event)) {
                    Alert alert = new Alert(rule, event);
                    notificationService.sendAlert(alert);
                }
            });
    }

    public void addRule(AlertRule rule) {
        rules.put(rule.getName(), rule);
        log.info("Added alert rule: {}", rule.getName());
    }

    public void removeRule(String ruleName) {
        rules.remove(ruleName);
        log.info("Removed alert rule: {}", ruleName);
    }

    public boolean isRunning() {
        return isRunning;
    }
} 