package com.example.shelldemo.monitoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.example.shelldemo.monitoring.model.MetricEvent;

@Component
public class MetricsPersistence {
    private static final Logger log = LoggerFactory.getLogger(MetricsPersistence.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public MetricsPersistence(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
        initializeSchema();
    }

    private void initializeSchema() {
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS metrics (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                collector_name VARCHAR(255),
                metric_name VARCHAR(255),
                value DOUBLE,
                tags JSON,
                timestamp TIMESTAMP,
                INDEX idx_metric_time (metric_name, timestamp)
            )
        """);
    }

    public void saveMetric(MetricEvent event) {
        try {
            jdbc.update("""
                INSERT INTO metrics (collector_name, metric_name, value, tags, timestamp)
                VALUES (?, ?, ?, ?, ?)
            """, event.getCollector(), event.getName(), event.getValue(),
                mapper.writeValueAsString(event.getTags()), event.getTimestamp());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metric tags", e);
        }
    }

    public List<MetricEvent> queryMetrics(String metricName, Instant start, Instant end) {
        return jdbc.query("""
            SELECT * FROM metrics 
            WHERE metric_name = ? AND timestamp BETWEEN ? AND ?
            ORDER BY timestamp
        """, (rs, rowNum) -> {
            try {
                return new MetricEvent(
                    rs.getString("collector_name"),
                    rs.getString("metric_name"),
                    rs.getDouble("value"),
                    mapper.readValue(rs.getString("tags"), new TypeReference<Map<String, String>>() {}),
                    rs.getTimestamp("timestamp").toInstant()
                );
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize metric tags", e);
                return null;
            }
        }, metricName, start, end);
    }
} 