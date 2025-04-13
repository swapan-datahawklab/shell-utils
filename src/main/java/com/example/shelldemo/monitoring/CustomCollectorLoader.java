package com.example.shelldemo.monitoring;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.monitoring.exception.MonitoringCollectorException;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class CustomCollectorLoader {
    private static final Logger log = LoggerFactory.getLogger(CustomCollectorLoader.class);

    public List<MetricCollector> loadCollectors(File jarFile) throws MonitoringCollectorException {
        List<MetricCollector> collectors = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            URL[] urls = {jarFile.toURI().toURL()};
            try (URLClassLoader loader = new URLClassLoader(urls)) {
                jar.entries().asIterator().forEachRemaining(entry -> {
                    if (entry.getName().endsWith(".class")) {
                        try {
                            String className = entry.getName()
                                .replace("/", ".")
                                .replace(".class", "");
                            Class<?> clazz = loader.loadClass(className);
                            if (MetricCollector.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                                collectors.add((MetricCollector) clazz.getDeclaredConstructor().newInstance());
                            }
                        } catch (Exception e) {
                            log.warn("Failed to load collector from class: {}", entry.getName(), e);
                        }
                    }
                });
            }
        } catch (Exception e) {
            throw new MonitoringCollectorException("Failed to load collectors from jar: " + jarFile.getName(), 
                "CustomCollectorLoader", e.getMessage(), e);
        }
        return collectors;
    }
} 