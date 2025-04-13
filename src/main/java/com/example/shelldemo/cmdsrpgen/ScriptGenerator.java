package com.example.shelldemo.cmdsrpgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.analysis.UtilCommandRegistry;
import com.example.shelldemo.cli.exception.ScriptGenerationException;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public final class ScriptGenerator {
    private static final Logger log = LoggerFactory.getLogger(ScriptGenerator.class);
    
    private ScriptGenerator() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void generateScripts(List<UtilCommandRegistry.CommandData> commands, File scriptDir) {
        if (!scriptDir.exists() && !scriptDir.mkdirs()) {
            log.error("Failed to create script directory: {}", scriptDir);
            throw new ScriptGenerationException("Failed to create script directory: " + scriptDir);
        }

        for (UtilCommandRegistry.CommandData command : commands) {
            File scriptFile = new File(scriptDir, command.getName() + ".sh");
            try (FileWriter writer = new FileWriter(scriptFile)) {
                writer.write(generateScript(command));
                log.info("Generated script for command: {}", command.getName());
            } catch (IOException e) {
                throw new ScriptGenerationException("Script generation failed for command: " + command.getName(), e);
            }
        }
    }
    
    private static String generateScript(UtilCommandRegistry.CommandData command) {
        return String.format("#!/bin/bash%n" +
            "# Description: %s%n" +
            "# Usage: %s%n" +
            "%n" +
            "echo \"Executing command: %s\"%n" +
            "# Add your command implementation here%n", 
            command.getDescription(), 
            command.getUsage(),
            command.getName());
    }
    
    public String generateDocumentation(List<UtilCommandRegistry.CommandData> commands) {
        StringBuilder doc = new StringBuilder("# Command Documentation%n%n");
        for (UtilCommandRegistry.CommandData command : commands) {
            doc.append(String.format("## %s%n%n" +
                "**Description:** %s%n%n" +
                "**Usage:** %s%n%n",
                command.getName(),
                command.getDescription(),
                command.getUsage()));
        }
        return doc.toString();
    }
    
    public static void generateBatchScript(File scriptFile, Class<?> clazz) {
        StringBuilder script = new StringBuilder();
        script.append("@echo off\n\n");
        script.append("REM Auto-generated script for ").append(scriptFile.getName()).append("\n\n");
        
        // Add usage function
        script.append(":usage\n");
        script.append("echo Usage: ").append(scriptFile.getName()).append(" [options]\n");
        script.append("echo Options:\n");
        
        // Add options
        for (Field field : clazz.getDeclaredFields()) {
            Option option = field.getAnnotation(Option.class);
            if (option != null) {
                String names = String.join(", ", option.names());
                String description = String.join(" ", option.description());
                script.append("echo   ").append(names).append(" - ").append(description).append("\n");
            }
        }
        
        // Add parameters
        for (Field field : clazz.getDeclaredFields()) {
            Parameters params = field.getAnnotation(Parameters.class);
            if (params != null) {
                String description = String.join(" ", params.description());
                script.append("echo   ").append(field.getName()).append(" - ").append(description).append("\n");
            }
        }
        
        script.append("exit /b 0\n\n");
        
        // Add main script body
        script.append("set CMD=java -cp %%CLASSPATH%% com.example.shelldemo.App ").append(scriptFile.getName()).append("\n\n");
        script.append(":parse_args\n");
        script.append("if \"%~1\"==\"\" goto :execute\n\n");
        
        // Add option handling
        for (Field field : clazz.getDeclaredFields()) {
            Option option = field.getAnnotation(Option.class);
            if (option != null) {
                for (String name : option.names()) {
                    script.append("if \"%~1\"==\"").append(name).append("\" (\n");
                    script.append("    set CMD=%%CMD%% ").append(name).append(" %~2\n");
                    script.append("    shift\n");
                    script.append("    shift\n");
                    script.append("    goto :parse_args\n");
                    script.append(")\n");
                }
            }
        }
        
        script.append("if \"%~1\"==\"--help\" (\n");
        script.append("    call :usage\n");
        script.append("    exit /b 0\n");
        script.append(")\n\n");
        
        script.append("set CMD=%%CMD%% %~1\n");
        script.append("shift\n");
        script.append("goto :parse_args\n\n");
        
        script.append(":execute\n");
        script.append("%%CMD%%\n");
        
        writeScript(scriptFile, script.toString());
    }
    
    private static void writeScript(File file, String content) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
            log.info("Generated script: {}", file.getAbsolutePath());
        } catch (IOException e) {
            throw new ScriptGenerationException("Failed to write script: " + file.getName(), e);
        }
    }
    
    public static String generateScript(String command) {
        // Basic script generation logic
        return String.format("#!/bin/bash%n%s", command);
    }
    
    public static String generateDocumentation(String command) {
        // Basic documentation generation logic
        return String.format("# %s%n%nUsage: %s", command, command);
    }
} 