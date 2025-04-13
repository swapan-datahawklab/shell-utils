package com.example.shelldemo.analysis;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.shelldemo.cli.exception.CommandExecutionException;

import java.util.Optional;

public final class UtilCommandRegistry {
    private static final Map<String, CommandData> commands = new ConcurrentHashMap<>();

    private UtilCommandRegistry() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initialize(List<Class<?>> commandClasses) {
        if (commandClasses == null) {
            throw new IllegalArgumentException("Command classes list cannot be null");
        }
        
        commands.clear();
        for (Class<?> clazz : commandClasses) {
            Command cmd = clazz.getAnnotation(Command.class);
            if (cmd == null) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @Command");
            }
            
            String name = cmd.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : cmd.name();
            String description = cmd.description();
            String usage = cmd.usage();
            
            if (commands.containsKey(name)) {
                throw new IllegalStateException("Duplicate command name: " + name);
            }
            
            commands.put(name, new CommandData(name, description, usage, clazz, cmd));
        }
    }

    public static List<CommandData> getCommands() {
        return List.copyOf(commands.values());
    }

    public static Optional<CommandData> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public static class CommandData {
        private final String name;
        private final String description;
        private final String usage;
        private final Class<?> commandClass;
        private final Command commandAnnotation;

        public CommandData(String name, String description, String usage, Class<?> commandClass, Command commandAnnotation) {
            this.name = name;
            this.description = description;
            this.usage = usage;
            this.commandClass = commandClass;
            this.commandAnnotation = commandAnnotation;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getUsage() {
            return usage;
        }

        public Optional<Class<?>> getCommandClass() {
            return Optional.ofNullable(commandClass);
        }

        public Optional<Command> getCommandAnnotation() {
            return Optional.ofNullable(commandAnnotation);
        }

        public String execute() throws CommandExecutionException {
            try {
                Object instance = commandClass.getDeclaredConstructor().newInstance();
                return (String) commandClass.getMethod("execute").invoke(instance);
            } catch (NoSuchMethodException e) {
                throw new CommandExecutionException("Command class does not have a no-args constructor or execute method: " + name, e);
            } catch (IllegalAccessException e) {
                throw new CommandExecutionException("Cannot access command constructor or execute method: " + name, e);
            } catch (InvocationTargetException e) {
                throw new CommandExecutionException("Command execution failed: " + name, e.getCause());
            } catch (InstantiationException e) {
                throw new CommandExecutionException("Cannot instantiate command class: " + name, e);
            }
        }
    }
} 