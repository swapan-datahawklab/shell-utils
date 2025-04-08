package com.example.shelldemo.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertManager {
    private static final Logger log = LoggerFactory.getLogger(AlertManager.class);
    private final Map<String, AlertThreshold> thresholds = new ConcurrentHashMap<>();

    public void setThreshold(String metric, double value, AlertType type) {
        thresholds.put(metric, new AlertThreshold(value, type));
        log.info("Set {} threshold for {} to {}", type, metric, value);
    }

    public void checkMetrics(Map<String, Object> metrics) {
        for (Map.Entry<String, Object> entry : metrics.entrySet()) {
            AlertThreshold threshold = thresholds.get(entry.getKey());
            if (threshold != null) {
                double value = ((Number) entry.getValue()).doubleValue();
                if (threshold.shouldAlert(value)) {
                    log.warn("Alert: {} {} threshold exceeded. Value: {}", 
                            entry.getKey(), threshold.getType(), value);
                }
            }
        }
    }

    private static class AlertThreshold {
        private final double value;
        private final AlertType type;

        public AlertThreshold(double value, AlertType type) {
            this.value = value;
            this.type = type;
        }

        public boolean shouldAlert(double currentValue) {
            return type == AlertType.ABOVE ? currentValue > value : currentValue < value;
        }

        public AlertType getType() {
            return type;
        }
    }

    public enum AlertType {
        ABOVE, BELOW
    }
} 