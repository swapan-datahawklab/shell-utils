package com.example.shelldemo.runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.io.IOException;

import com.example.shelldemo.analysis.AgentAttacher;
import com.example.shelldemo.analysis.RuntimeAnalysisDocumentation;
import com.example.shelldemo.analysis.RuntimeAnalyzer;
import com.example.shelldemo.exception.BaseException;
import com.example.shelldemo.analysis.CommandService;
import com.example.shelldemo.analysis.DocumentationInitializationException;
import com.example.shelldemo.exception.AnalysisExecutionException;
import com.example.shelldemo.exception.DocumentationGenerationException;
@Command(name = "runtime-analysis", 
         description = "Run runtime analysis and generate documentation")
public class RuntimeAnalysisRunner implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAnalysisRunner.class);
    
    private final CommandService commandService;
    private final RuntimeAnalyzer runtimeAnalyzer;
    private final RuntimeAnalysisDocumentation runtimeAnalysisDocumentation;
    
    @Option(names = {"--pid"}, 
            description = "Process ID of the target Java application",
            required = true)
    private String processId;
    
    @Option(names = {"--agent-jar"}, 
            description = "Path to the agent JAR file",
            required = true)
    private String agentJarPath;
    
    @Option(names = {"--duration"}, 
            description = "Duration for runtime analysis in seconds",
            defaultValue = "60")
    private int duration;
    
    @Option(names = {"--output"}, 
            description = "Output file path for documentation",
            defaultValue = "runtime-analysis.html")
    private String outputPath;
    
    @Option(names = {"--template-vars"}, 
            description = "Custom template variables in key=value format",
            split = ",")
    private Map<String, String> templateVars;
    
    public RuntimeAnalysisRunner() {
        this.commandService = new CommandService("com.example.shelldemo.commands");
        this.runtimeAnalyzer = new RuntimeAnalyzer(commandService);
        try {
            this.runtimeAnalysisDocumentation = new RuntimeAnalysisDocumentation();
        } catch (IOException e) {
            throw new DocumentationInitializationException("Failed to initialize documentation generator", e);
        }
    }
    
    private void attachAgent() throws BaseException {
        log.info("Attaching agent to process {} with JAR {}", processId, agentJarPath);
        AgentAttacher.attachToProcess(processId, agentJarPath);
        log.info("Agent successfully attached");
    }
    
    public void runAnalysis(int durationSeconds) throws InterruptedException, AnalysisExecutionException {
        try {
            runtimeAnalyzer.start();
            log.info("Runtime analysis started. Duration: {} seconds", durationSeconds);
            Thread.sleep(durationSeconds * 1000L);
            runtimeAnalyzer.stop();
        } catch (RuntimeException e) {
            throw new AnalysisExecutionException("Failed to execute runtime analysis: " + e.getMessage(), e);
        }
    }
    
    public void generateDocumentation(Map<String, Object> templateVars, Path outputPath) throws DocumentationGenerationException {
        Map<String, Object> metrics = runtimeAnalyzer.getMetrics();
        templateVars.putAll(metrics);
        try {
            runtimeAnalysisDocumentation.generateDocumentation(templateVars, outputPath);
        } catch (Exception e) {
            throw new DocumentationGenerationException("Failed to generate documentation: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void run() {
        try {
            // Step 1: Attach the agent
            attachAgent();
            
            // Step 2: Run the analysis
            runAnalysis(duration);
            
            // Step 3: Generate documentation
            Map<String, Object> templateVariables = Map.copyOf(templateVars);
            generateDocumentation(
                templateVariables, 
                Path.of(outputPath)
            );
            
            log.info("Runtime analysis completed. Documentation generated at: {}", outputPath);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Runtime analysis interrupted: {}", e.getMessage());
        } catch (BaseException e) {
            log.error("Failed to attach agent: {}", e.getMessage());
        } catch (AnalysisExecutionException | DocumentationGenerationException e) {
            log.error("Error during runtime analysis: {}", e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        System.exit(new CommandLine(new RuntimeAnalysisRunner()).execute(args));
    }
} 