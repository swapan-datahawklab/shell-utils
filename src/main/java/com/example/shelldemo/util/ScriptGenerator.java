package com.example.shelldemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

@Component
public class ScriptGenerator {
    private static final Logger log = LoggerFactory.getLogger(ScriptGenerator.class);
    
    public void generateScripts(List<Class<?>> commandClasses, File scriptDir, boolean includeHidden) {
        for (Class<?> commandClass : commandClasses) {
            File scriptFile = new File(scriptDir, commandClass.getSimpleName().toLowerCase() + ".bat");
            generateBatchScript(scriptFile, commandClass);
        }
    }
    
    private static void generateBatchScript(File scriptFile, Class<?> clazz) {
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
            log.error("Error generating script {}: {}", file.getName(), e.getMessage());
        }
    }
} 