package com.example.shelldemo.util;

import com.example.shelldemo.annotation.Command;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for discovering command classes annotated with @Command.
 */
public class CommandClassDiscoverer {
    private static final Logger logger = LoggerFactory.getLogger(CommandClassDiscoverer.class);

    /**
     * Discovers all classes annotated with @Command in the specified base package.
     *
     * @param basePackage the base package to scan for command classes
     * @return a list of discovered command classes
     */
    public static List<Class<?>> discoverCommandClasses(String basePackage) {
        logger.debug("Discovering command classes in package: {}", basePackage);
        
        Reflections reflections = new Reflections(basePackage);
        List<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(Command.class)
                .stream()
                .collect(Collectors.toList());
                
        logger.info("Discovered {} command classes", commandClasses.size());
        return commandClasses;
    }
} 