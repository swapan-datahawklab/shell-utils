package com.example.shelldemo;

import com.example.shelldemo.util.CommandRegistry;
import com.example.shelldemo.util.ScriptGenerator;
import com.example.shelldemo.util.CommandClassDiscoverer;
import com.example.shelldemo.service.CommandService;
import com.example.shelldemo.analysis.RuntimeAnalysisDocumentation;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import java.util.ArrayList;

public class App {
    private final CommandRegistry commandRegistry;
    private final CommandService commandService;
    private final RuntimeAnalysisDocumentation runtimeAnalysisDocumentation;

    public App() {
        this.commandRegistry = new CommandRegistry(new ArrayList<>());
        this.commandService = new CommandService(commandRegistry, new ScriptGenerator());
        this.runtimeAnalysisDocumentation = new RuntimeAnalysisDocumentation();
    }

    public static void main(String[] args) {
        App app = new App();
        app.run(args);
    }

    public void run(String... args) {
        try {
            // Initialize command registry
            List<Class<?>> commandClasses = CommandClassDiscoverer.discoverCommandClasses("com.example");
            commandRegistry.initialize(commandClasses);

            // Generate scripts
            File scriptDir = new File("scripts");
            if (!scriptDir.exists()) {
                scriptDir.mkdirs();
            }
            commandService.generateScripts(scriptDir);

            // Generate documentation
            File docDir = new File("docs");
            if (!docDir.exists()) {
                docDir.mkdirs();
            }
            commandService.generateDocumentation("json", false, new File(docDir, "commands.json"));
            commandService.generateDocumentation("markdown", false, new File(docDir, "commands.md"));
            commandService.generateDocumentation("text", false, new File(docDir, "commands.txt"));

            // Generate runtime analysis documentation
            Map<String, Object> templateVars = Map.of("timestamp", System.currentTimeMillis());
            Path outputPath = Path.of("docs", "runtime-analysis.html");
            runtimeAnalysisDocumentation.generateDocumentation(templateVars, outputPath);

            // Parse and execute commands
            CommandLine commandLine = new CommandLine(new Object());
            for (Class<?> commandClass : commandClasses) {
                commandLine.addSubcommand(commandClass);
            }
            int exitCode = commandLine.execute(args);
            System.exit(exitCode);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 