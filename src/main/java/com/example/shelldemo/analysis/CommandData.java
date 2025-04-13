package com.example.shelldemo.analysis;

import picocli.CommandLine.Command;

/**
 * Data class representing a command's metadata and implementation.
 * This class is immutable and thread-safe.
 */
public final class CommandData {
    private final String name;
    private final String description;
    private final Class<?> commandClass;
    private final Command commandAnnotation;

    /**
     * Creates a new CommandData instance.
     * @param commandClass the command implementation class
     * @throws IllegalArgumentException if the command class is invalid
     */
    public CommandData(Class<?> commandClass) {
        if (commandClass == null) {
            throw new IllegalArgumentException("Command class cannot be null");
        }
        
        this.commandAnnotation = commandClass.getAnnotation(Command.class);
        if (this.commandAnnotation == null) {
            throw new IllegalArgumentException("Class " + commandClass.getName() + " is not annotated with @Command");
        }
        
        this.name = commandAnnotation.name();
        this.description = String.join(" ", commandAnnotation.description());
        this.commandClass = commandClass;
        
        validateCommandClass();
    }
    
    private void validateCommandClass() {
        try {
            // Verify the class implements Runnable
            if (!Runnable.class.isAssignableFrom(commandClass)) {
                throw new IllegalArgumentException("Command class must implement Runnable");
            }
            
            // Verify no-args constructor exists
            commandClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Command class must have a no-args constructor", e);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Class<?> getCommandClass() {
        return commandClass;
    }
    
    public Command getCommandAnnotation() {
        return commandAnnotation;
    }
    
    /**
     * Creates a new instance of the command.
     * @return a new command instance
     * @throws IllegalStateException if the command cannot be instantiated
     */
    public Runnable newInstance() {
        try {
            return (Runnable) commandClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create command instance: " + name, e);
        }
    }
} 