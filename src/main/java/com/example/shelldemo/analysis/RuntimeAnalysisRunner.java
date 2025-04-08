package com.example.shelldemo.analysis;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.Map;

@Command(name = "runtime-analysis", 
         description = "Run runtime analysis and generate documentation")
public class RuntimeAnalysisRunner implements Runnable {
    
    private final RuntimeAnalyzer runtimeAnalyzer;
    private final RuntimeAnalysisDocumentation runtimeAnalysisDocumentation;
    
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
    
    public void runAnalysis(int durationSeconds) throws Exception {
        runtimeAnalyzer.start();
        System.out.println("Runtime analysis started. Duration: " + durationSeconds + " seconds");
        Thread.sleep(durationSeconds * 1000L);
        runtimeAnalyzer.stop();
    }
    
    public void generateDocumentation(Map<String, Object> templateVars, Path outputPath) throws Exception {
        runtimeAnalysisDocumentation.generateDocumentation(templateVars, outputPath);
    }
    
    @Override
    public void run() {
        try {
            // Run the analysis
            runAnalysis(duration);
            
            // Generate documentation
            Map<String, Object> templateVariables = Map.copyOf(templateVars);
            generateDocumentation(
                templateVariables, 
                Path.of(outputPath)
            );
            
            System.out.println("Runtime analysis completed. Documentation generated at: " + outputPath);
        } catch (Exception e) {
            System.err.println("Error during runtime analysis: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        System.exit(new CommandLine(new RuntimeAnalysisRunner()).execute(args));
    }
} 