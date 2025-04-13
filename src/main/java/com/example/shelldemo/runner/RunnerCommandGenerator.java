package com.example.shelldemo.runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.cli.exception.CommandGenerationException;
import com.example.shelldemo.cmdsrpgen.ScriptManager;

@Command(name = "command-generator", mixinStandardHelpOptions = true)
public class RunnerCommandGenerator {
    private static final Logger log = LoggerFactory.getLogger(RunnerCommandGenerator.class);

    @Option(names = {"--update-scripts"}, description = "Update run.sh and run.bat scripts")
    private boolean updateScripts;

    @Option(names = {"--script-dir"}, description = "Directory containing run scripts")
    private File scriptDir = new File(".");

    @Option(names = {"--command-classes"}, description = "Comma-separated list of command classes to generate")
    private String commandClasses;

    public void generateCommands() {
        try {
            validateScriptDir();
            List<Class<?>> classes = parseCommandClasses();
            if (updateScripts) {
                // Use ScriptManager to generate individual command scripts
                ScriptManager.initializeScripts(classes, scriptDir.getAbsolutePath(), true);
                // Generate wrapper scripts
                updateRunScripts(classes);
            }
        } catch (ClassNotFoundException e) {
            throw new CommandGenerationException("Failed to load command class: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new CommandGenerationException("Failed to write script files: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CommandGenerationException("Unexpected error during command generation: " + e.getMessage(), e);
        }
    }

    private void validateScriptDir() {
        if (!scriptDir.exists() && !scriptDir.mkdirs()) {
            throw new CommandGenerationException("Failed to create script directory: " + scriptDir);
        }
        if (!scriptDir.isDirectory()) {
            throw new CommandGenerationException("Script directory is not a directory: " + scriptDir);
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
                script.append("    ./").append(cmd.name()).append(".sh \"$@\"\n");
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
                script.append("    call ").append(cmd.name()).append(".bat %*\n");
                script.append("    goto :eof\n\n");
            }
        }
        
        return script.toString();
    }
} 