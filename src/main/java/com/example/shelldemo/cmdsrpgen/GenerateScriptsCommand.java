package com.example.shelldemo.cmdsrpgen;

import com.example.shelldemo.analysis.Command;
import com.example.shelldemo.analysis.CommandClassDiscoverer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

@Command(
    name = "generate-scripts",
    description = "Generates shell scripts for all discovered commands",
    usage = "generate-scripts <base-package> <output-dir>"
)
public class GenerateScriptsCommand implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GenerateScriptsCommand.class);
    private final String basePackage;
    private final String outputDir;

    public GenerateScriptsCommand(String basePackage, String outputDir) {
        this.basePackage = basePackage;
        this.outputDir = outputDir;
    }

    @Override
    public void run() {
        try {
            log.info("Generating scripts in directory: {}", outputDir);
            
            // Get all command classes
            List<Class<?>> commandClasses = new ArrayList<>();
            CommandClassDiscoverer.discoverCommands(basePackage)
                .forEach(cmd -> commandClasses.add(cmd.getCommandClass()));
            
            // Generate scripts
            ScriptManager.initializeScripts(commandClasses, outputDir, false);
            
            log.info("Script generation completed successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate scripts: " + e.getMessage(), e);
        }
    }
} 