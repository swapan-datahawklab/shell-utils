package com.example.shelldemo.monitoring.collector;

import java.nio.file.Path;
import java.net.URLClassLoader;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.shelldemo.monitoring.model.MetricCollector;
public class CustomCollectorLoader {
    private static final Logger log = LoggerFactory.getLogger(CustomCollectorLoader.class);
    private final Path collectorDir;

    public CustomCollectorLoader(Path collectorDir) {
        this.collectorDir = collectorDir;
    }

    public Map<String, MetricCollector> loadCollectors() {
        Map<String, MetricCollector> collectors = new HashMap<>();
        try {
            if (collectorDir != null && Files.exists(collectorDir)) {
                URL[] urls = new URL[]{collectorDir.toUri().toURL()};
                try (URLClassLoader loader = new URLClassLoader(urls)) {
                    Files.list(collectorDir)
                         .filter(p -> p.toString().endsWith(".jar"))
                         .forEach(jar -> {
                             try {
                                 String className = jar.getFileName().toString()
                                     .replace(".jar", "")
                                     .replace("-", ".");
                                 Class<?> collectorClass = loader.loadClass(className);
                                 if (MetricCollector.class.isAssignableFrom(collectorClass)) {
                                     MetricCollector collector = (MetricCollector) collectorClass.getDeclaredConstructor().newInstance();
                                     collectors.put(collector.getName(), collector);
                                 }
                             } catch (Exception e) {
                                 log.error("Failed to load collector from " + jar, e);
                             }
                         });
                }
            }
        } catch (Exception e) {
            log.error("Error loading custom collectors", e);
        }
        return collectors;
    }
} 