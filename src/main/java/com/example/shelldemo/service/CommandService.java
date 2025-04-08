package com.example.shelldemo.service;

import com.example.shelldemo.util.CommandRegistry;
import com.example.shelldemo.util.ScriptGenerator;
import com.example.shelldemo.util.CommandData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CommandService {
    private static final Logger log = LoggerFactory.getLogger(CommandService.class);
    private final Configuration freemarkerConfig;
    private final CommandRegistry commandRegistry;
    private final ScriptGenerator scriptGenerator;

    public CommandService(CommandRegistry commandRegistry, ScriptGenerator scriptGenerator) {
        this.commandRegistry = commandRegistry;
        this.scriptGenerator = scriptGenerator;
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void generateScripts(File scriptDir) {
        scriptGenerator.generateScripts(commandRegistry.getCommandClasses(), scriptDir, false);
    }

    public void generateDocumentation(String format, boolean includeHidden, File outputFile) throws Exception {
        switch (format.toLowerCase()) {
            case "json" -> generateJsonOutput(outputFile);
            case "markdown" -> generateMarkdownOutput(outputFile);
            default -> generateTextOutput(outputFile);
        }
    }

    private void generateJsonOutput(File outputFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ArrayNode commands = root.putArray("commands");
        
        for (CommandData commandData : commandRegistry.getCommandData()) {
            ObjectNode commandNode = commands.addObject();
            commandNode.put("name", commandData.getName());
            commandNode.put("description", String.join(" ", commandData.getDescription()));
        }
        
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, root);
    }

    private void generateMarkdownOutput(File outputFile) throws Exception {
        StringBuilder md = new StringBuilder("# Command Documentation\n\n");
        
        for (CommandData commandData : commandRegistry.getCommandData()) {
            md.append("## ").append(commandData.getName()).append("\n\n");
            md.append(String.join(" ", commandData.getDescription())).append("\n\n");
        }
        
        Files.writeString(outputFile.toPath(), md.toString());
    }

    private void generateTextOutput(File outputFile) throws Exception {
        StringBuilder text = new StringBuilder("Command Documentation\n");
        text.append("==================\n\n");
        
        for (CommandData commandData : commandRegistry.getCommandData()) {
            text.append(commandData.getName()).append("\n");
            text.append(String.join(" ", commandData.getDescription())).append("\n\n");
        }
        
        Files.writeString(outputFile.toPath(), text.toString());
    }

    public String processTemplate(String templateName, Map<String, Object> model) throws Exception {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    public void writeOutput(String content, String filename) throws Exception {
        Files.writeString(Path.of(filename), content);
        log.info("Generated: {}", filename);
    }

    public Map<String, Object> collectCommandData() {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> commands = new ArrayList<>();
        
        for (CommandData commandData : commandRegistry.getCommandData()) {
            Map<String, Object> commandInfo = new HashMap<>();
            commandInfo.put("name", commandData.getName());
            commandInfo.put("description", String.join(" ", commandData.getDescription()));
            commands.add(commandInfo);
        }
        
        data.put("commands", commands);
        return data;
    }

    public void generateRuntimeAnalysisDocumentation(Map<String, Object> templateVars, Path outputPath) throws Exception {
        Template template = freemarkerConfig.getTemplate("runtime-analysis.ftl");
        StringWriter writer = new StringWriter();
        template.process(templateVars, writer);
        String output = writer.toString();
        Files.writeString(outputPath, output);
        log.info("Generated runtime analysis documentation at: {}", outputPath);
    }
} 