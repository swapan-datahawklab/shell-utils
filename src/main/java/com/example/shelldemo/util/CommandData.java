package com.example.shelldemo.util;

import picocli.CommandLine.Command;

public class CommandData {
    private final Class<?> commandClass;
    private final Command commandAnnotation;
    
    public CommandData(Class<?> commandClass) {
        this.commandClass = commandClass;
        this.commandAnnotation = commandClass.getAnnotation(Command.class);
    }
    
    public Class<?> getCommandClass() {
        return commandClass;
    }
    
    public Command getCommandAnnotation() {
        return commandAnnotation;
    }
    
    public String getName() {
        return commandAnnotation != null ? commandAnnotation.name() : commandClass.getSimpleName();
    }
    
    public String getDescription() {
        return commandAnnotation != null ? commandAnnotation.description()[0] : "";
    }
} 