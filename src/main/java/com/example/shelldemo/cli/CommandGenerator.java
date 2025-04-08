package com.example.shelldemo.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.lang.reflect.Field;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "command-generator", mixinStandardHelpOptions = true)
public class CommandGenerator {
    private static final Logger log = LoggerFactory.getLogger(CommandGenerator.class);

    @Option(names = {"--update-scripts"}, description = "Update run.sh and run.bat scripts")
    private boolean updateScripts;

    @Option(names = {"--script-dir"}, description = "Directory containing run scripts")
    private File scriptDir = new File(".");

    @Option(names = {"--command-classes"}, description = "Comma-separated list of command classes to generate")
    private String commandClasses;

    public void generateCommands() {
        try {
            List<Class<?>> classes = parseCommandClasses();
            if (updateScripts) {
                updateRunScripts(classes);
            }
        } catch (Exception e) {
            log.error("Error generating commands", e);
        }
    }

    private List<Class<?>> parseCommandClasses() throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (commandClasses != null) {
            for (String className : commandClasses.split(",")) {
                classes.add(Class.forName(className.trim()));
            }
        }
        return classes;
    }

    private void updateRunScripts(List<Class<?>> commandClasses) throws IOException {
        // Generate run.sh
        String shScript = generateShellScript(commandClasses);
        Path shPath = scriptDir.toPath().resolve("run.sh");
        Files.writeString(shPath, shScript, StandardCharsets.UTF_8);
        log.info("Updated run.sh at {}", shPath);

        // Generate run.bat
        String batScript = generateBatchScript(commandClasses);
        Path batPath = scriptDir.toPath().resolve("run.bat");
        Files.writeString(batPath, batScript, StandardCharsets.UTF_8);
        log.info("Updated run.bat at {}", batPath);
    }

    private String generateShellScript(List<Class<?>> commandClasses) {
        StringBuilder script = new StringBuilder("#!/bin/bash\n\n");
        script.append("# Auto-generated script\n\n");
        
        for (Class<?> commandClass : commandClasses) {
            Command cmd = commandClass.getAnnotation(Command.class);
            if (cmd != null) {
                script.append("# Command: ").append(cmd.name()).append("\n");
                script.append("function ").append(cmd.name()).append("() {\n");
                script.append("    java -jar $JAR_FILE ");
                script.append(generateCommandOptions(commandClass));
                script.append(" \"$@\"\n");
                script.append("}\n\n");
            }
        }
        
        return script.toString();
    }

    private String generateBatchScript(List<Class<?>> commandClasses) {
        StringBuilder script = new StringBuilder("@echo off\n\n");
        script.append("REM Auto-generated script\n\n");
        
        for (Class<?> commandClass : commandClasses) {
            Command cmd = commandClass.getAnnotation(Command.class);
            if (cmd != null) {
                script.append("REM Command: ").append(cmd.name()).append("\n");
                script.append(":").append(cmd.name()).append("\n");
                script.append("    java -jar %JAR_FILE% ");
                script.append(generateCommandOptions(commandClass));
                script.append(" %*\n");
                script.append("    goto :eof\n\n");
            }
        }
        
        return script.toString();
    }

    private String generateCommandOptions(Class<?> commandClass) {
        StringBuilder options = new StringBuilder();
        for (Field field : commandClass.getDeclaredFields()) {
            Option option = field.getAnnotation(Option.class);
            if (option != null) {
                options.append("--").append(field.getName()).append(" ");
            }
        }
        return options.toString().trim();
    }
} 