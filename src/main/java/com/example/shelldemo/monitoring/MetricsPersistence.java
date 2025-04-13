package com.example.shelldemo.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class MetricsPersistence {
    private static final Logger log = LoggerFactory.getLogger(MetricsPersistence.class);
    private final Connection connection;

    public MetricsPersistence(Connection connection) {
        this.connection = connection;
    }

    public void saveMetric(MetricEvent event) {
        String sql = "INSERT INTO metrics (name, value, timestamp) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, event.getName());
            stmt.setDouble(2, event.getValue());
            stmt.setTimestamp(3, Timestamp.from(Instant.now()));
            stmt.executeUpdate();
            log.debug("Saved metric: {}", event);
        } catch (SQLException e) {
            log.error("Failed to save metric: {}", event, e);
        }
    }
} 