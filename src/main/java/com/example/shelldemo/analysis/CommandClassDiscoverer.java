package com.example.shelldemo.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.google.common.reflect.ClassPath;
import java.lang.reflect.Modifier;

/**
 * Custom exception for command discovery failures.
 */
class CommandDiscoveryException extends RuntimeException {
    public CommandDiscoveryException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Utility class for discovering command classes annotated with @Command.
 * This class is thread-safe and caches discovered commands for performance.
 */
public final class CommandClassDiscoverer {
    private static final Logger logger = LoggerFactory.getLogger(CommandClassDiscoverer.class);
    private static final Map<String, List<CommandData>> CACHE = new ConcurrentHashMap<>();

    private CommandClassDiscoverer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Discovers all command classes in the specified base package.
     * Results are cached for performance.
     *
     * @param basePackage the base package to scan for command classes
     * @return a list of discovered command data
     * @throws IllegalArgumentException if basePackage is null or empty
     * @throws IllegalStateException if command discovery fails
     */
    public static List<CommandData> discoverCommands(String basePackage) {
        if (basePackage == null || basePackage.trim().isEmpty()) {
            throw new IllegalArgumentException("Base package cannot be null or empty");
        }
        
        return CACHE.computeIfAbsent(basePackage, pkg -> {
            try {
                logger.debug("Discovering commands in package: {}", pkg);
                return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getTopLevelClasses(pkg)
                    .stream()
                    .map(ClassPath.ClassInfo::load)
                    .filter(clazz -> clazz.isAnnotationPresent(Command.class))
                    .filter(clazz -> !clazz.isInterface())
                    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .map(CommandData::new)
                    .toList();
            } catch (IOException e) {
                throw new CommandDiscoveryException(
                    String.format("Failed to discover commands in package '%s'. This may be due to missing classes, invalid package structure, or class loading issues.", 
                    basePackage), e);
            }
        });
    }

    /**
     * Finds a specific command by name.
     *
     * @param basePackage the base package to search in
     * @param commandName the name of the command to find
     * @return the command data if found
     * @throws IllegalArgumentException if the command is not found
     */
    public static CommandData findCommand(String basePackage, String commandName) {
        return discoverCommands(basePackage).stream()
            .filter(cmd -> cmd.getName().equals(commandName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Command not found: " + commandName));
    }
} 