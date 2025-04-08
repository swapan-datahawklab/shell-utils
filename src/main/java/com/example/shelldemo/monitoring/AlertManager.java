package com.example.shelldemo.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.example.shelldemo.monitoring.model.MetricEvent;
import com.example.shelldemo.monitoring.model.AlertRule;
import com.example.shelldemo.monitoring.model.AlertConfig;
import com.example.shelldemo.monitoring.model.Alert;

@Component
public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);
    private final Map<String, AlertRule> rules = new ConcurrentHashMap<>();
    private final NotificationService notificationService;

    public AlertManager(Path configPath, NotificationService notificationService) {
        loadAlertRules(configPath);
        this.notificationService = notificationService;
    }

    private void loadAlertRules(Path configPath) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AlertConfig config = mapper.readValue(configPath.toFile(), AlertConfig.class);
            config.getRules().forEach(rule -> rules.put(rule.getName(), rule));
        } catch (Exception e) {
            log.error("Failed to load alert rules", e);
        }
    }

    public void checkMetric(MetricEvent event) {
        rules.values().stream()
            .filter(rule -> rule.matches(event))
            .forEach(rule -> {
                if (rule.isTriggered(event)) {
                    Alert alert = new Alert(rule, event);
                    notificationService.sendAlert(alert);
                }
            });
    }
} 