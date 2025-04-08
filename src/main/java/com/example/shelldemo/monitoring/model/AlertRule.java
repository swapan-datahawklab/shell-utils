package com.example.shelldemo.monitoring.model;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Represents a rule for triggering alerts based on metric events.
 * This class is immutable and thread-safe.
 */
public class AlertRule {
    private static final Pattern CONDITION_PATTERN = Pattern.compile("^([<>]=?|==)\\s*([0-9.]+)$");
    
    private final String name;
    private final String metric;
    private final String condition;
    private final Duration duration;
    private final String severity;
    private final List<String> channels;
    private final String message;
    private final Map<String, String> tags;

    /**
     * Creates a new alert rule.
     *
     * @param name The name of the alert rule
     * @param metric The metric name to monitor
     * @param condition The condition to evaluate (e.g., ">0.9", "<0.05", "==1.0")
     * @param duration The duration for which the condition must be met
     * @param severity The severity level of the alert
     * @param channels The notification channels to use
     * @param message The alert message template
     * @param tags Optional tags to match against metric events
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public AlertRule(String name, String metric, String condition, Duration duration, 
                    String severity, List<String> channels, String message,
                    Map<String, String> tags) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.metric = Objects.requireNonNull(metric, "metric cannot be null");
        this.condition = validateCondition(condition);
        this.duration = Objects.requireNonNull(duration, "duration cannot be null");
        this.severity = Objects.requireNonNull(severity, "severity cannot be null");
        this.channels = List.copyOf(Objects.requireNonNull(channels, "channels cannot be null"));
        this.message = Objects.requireNonNull(message, "message cannot be null");
        this.tags = Map.copyOf(Objects.requireNonNull(tags, "tags cannot be null"));
    }

    private String validateCondition(String condition) {
        Objects.requireNonNull(condition, "condition cannot be null");
        if (!CONDITION_PATTERN.matcher(condition).matches()) {
            throw new IllegalArgumentException("Invalid condition format: " + condition + 
                ". Must match pattern: " + CONDITION_PATTERN.pattern());
        }
        return condition;
    }

    public String getName() { return name; }
    public String getMetric() { return metric; }
    public String getCondition() { return condition; }
    public Duration getDuration() { return duration; }
    public String getSeverity() { return severity; }
    public List<String> getChannels() { return channels; }
    public String getMessage() { return message; }
    public Map<String, String> getTags() { return tags; }

    /**
     * Checks if this rule matches the given metric event.
     * A match occurs if the metric name matches and all tags match.
     *
     * @param event The metric event to check
     * @return true if the event matches this rule
     */
    public boolean matches(MetricEvent event) {
        if (!event.getName().equals(metric)) {
            return false;
        }
        
        // Check if all required tags are present and match
        return tags.entrySet().stream()
            .allMatch(entry -> event.getTags().getOrDefault(entry.getKey(), "")
                .equals(entry.getValue()));
    }

    /**
     * Evaluates if the metric event triggers this alert rule.
     * The event must first match the rule (see {@link #matches(MetricEvent)}).
     *
     * @param event The metric event to evaluate
     * @return true if the event triggers the alert
     */
    public boolean isTriggered(MetricEvent event) {
        if (!matches(event)) {
            return false;
        }

        double value = event.getValue();
        String[] parts = condition.split("\\s+");
        String operator = parts[0];
        double threshold = Double.parseDouble(parts[1]);

        return switch (operator) {
            case ">" -> value > threshold;
            case "<" -> value < threshold;
            case ">=" -> value >= threshold;
            case "<=" -> value <= threshold;
            case "==" -> value == threshold;
            default -> false;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertRule alertRule = (AlertRule) o;
        return name.equals(alertRule.name) &&
               metric.equals(alertRule.metric) &&
               condition.equals(alertRule.condition) &&
               duration.equals(alertRule.duration) &&
               severity.equals(alertRule.severity) &&
               channels.equals(alertRule.channels) &&
               message.equals(alertRule.message) &&
               tags.equals(alertRule.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, metric, condition, duration, severity, channels, message, tags);
    }

    @Override
    public String toString() {
        return "AlertRule{" +
            "name='" + name + '\'' +
            ", metric='" + metric + '\'' +
            ", condition='" + condition + '\'' +
            ", duration=" + duration +
            ", severity='" + severity + '\'' +
            ", channels=" + channels +
            ", message='" + message + '\'' +
            ", tags=" + tags +
            '}';
    }
} 