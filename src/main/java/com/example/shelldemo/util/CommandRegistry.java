package com.example.shelldemo.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CommandRegistry {
    private final Map<String, CommandData> commandMap = new ConcurrentHashMap<>();
    
    public CommandRegistry(List<Class<?>> commandClasses) {
        initialize(commandClasses);
    }
    
    public void initialize(List<Class<?>> commandClasses) {
        for (Class<?> commandClass : commandClasses) {
            CommandData commandData = new CommandData(commandClass);
            commandMap.put(commandData.getName(), commandData);
        }
    }
    
    public CommandData getCommand(String name) {
        return commandMap.get(name);
    }
    
    public List<CommandData> getCommandData() {
        return new ArrayList<>(commandMap.values());
    }
    
    public List<Class<?>> getCommandClasses() {
        return commandMap.values().stream()
                .map(CommandData::getCommandClass)
                .collect(Collectors.toList());
    }
} 