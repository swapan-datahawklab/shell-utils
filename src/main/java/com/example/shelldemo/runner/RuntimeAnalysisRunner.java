package com.example.shelldemo.runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;

import com.example.shelldemo.analysis.AgentAttacher;
import com.example.shelldemo.analysis.RuntimeAnalysisDocumentation;
import com.example.shelldemo.analysis.RuntimeAnalyzer;
import com.example.shelldemo.exception.BaseException;

@Command(name = "runtime-analysis", 
         description = "Run runtime analysis and generate documentation")
public class RuntimeAnalysisRunner implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAnalysisRunner.class);
    
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
        this.runtimeAnalyzer = new RuntimeAnalyzer();
        this.runtimeAnalysisDocumentation = new RuntimeAnalysisDocumentation();
    }
    
    private void attachAgent() throws BaseException {
        log.info("Attaching agent to process {} with JAR {}", processId, agentJarPath);
        AgentAttacher.attachToProcess(processId, agentJarPath);
        log.info("Agent successfully attached");
    }
    
    public void runAnalysis(int durationSeconds) throws InterruptedException, RuntimeException {
        runtimeAnalyzer.start();
        log.info("Runtime analysis started. Duration: {} seconds", durationSeconds);
        Thread.sleep(durationSeconds * 1000L);
        runtimeAnalyzer.stop();
    }
    
    public void generateDocumentation(Map<String, Object> templateVars, Path outputPath) throws Exception {
        Map<String, Object> metrics = runtimeAnalyzer.getMetrics();
        templateVars.putAll(metrics);
        runtimeAnalysisDocumentation.generateDocumentation(templateVars, outputPath);
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
        } catch (Exception e) {
            log.error("Error during runtime analysis: {}", e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
        System.exit(new CommandLine(new RuntimeAnalysisRunner()).execute(args));
    }
} 