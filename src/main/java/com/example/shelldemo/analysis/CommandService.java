package com.example.shelldemo.analysis;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.cli.exception.CommandExecutionException;
import com.example.shelldemo.cli.exception.DocumentationGenerationException;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.io.IOException;
import freemarker.template.TemplateException;

/**
 * Service for managing command execution and documentation.
 * This class is thread-safe.
 */
public class CommandService {
    private static final Logger log = LoggerFactory.getLogger(CommandService.class);
    private final Configuration freemarkerConfig;
    private final String basePackage;

    public CommandService(String basePackage) {
        this.basePackage = basePackage;
        this.freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    /**
     * Executes a command with the given arguments.
     *
     * @param commandName the name of the command to execute
     * @param args the command arguments
     * @return the command's exit code
     * @throws CommandExecutionException if command execution fails
     */
    public int executeCommand(String commandName, String[] args) throws CommandExecutionException {
        try {
            CommandData commandData = CommandClassDiscoverer.findCommand(basePackage, commandName);
            Runnable command = commandData.newInstance();
            command.run();
            return 0;
        } catch (IllegalArgumentException e) {
            throw new CommandExecutionException("Invalid command or arguments: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CommandExecutionException("Failed to execute command: " + commandName, e);
        }
    }

    /**
     * Generates documentation for all commands.
     *
     * @param outputPath the output file path
     * @throws DocumentationGenerationException if documentation generation fails
     */
    public void generateDocumentation(String outputPath) throws DocumentationGenerationException {
        try {
            List<CommandData> commands = CommandClassDiscoverer.discoverCommands(basePackage);
            Map<String, Object> model = new HashMap<>();
            model.put("commands", commands);
            
            String content = processTemplate("commands.ftl", model);
            writeOutput(content, outputPath);
            log.info("Generated documentation at {}", outputPath);
        } catch (Exception e) {
            throw new DocumentationGenerationException("Documentation generation failed for path: " + outputPath, e);
        }
    }

    /**
     * Gets help information for a specific command.
     *
     * @param commandName the name of the command
     * @return the help information
     */
    public String getCommandHelp(String commandName) {
        try {
            CommandData command = CommandClassDiscoverer.findCommand(basePackage, commandName);
            return String.format("%s - %s", 
                command.getName(), command.getDescription());
        } catch (IllegalArgumentException e) {
            return "Unknown command: " + commandName;
        }
    }

    /**
     * Gets general help information for all commands.
     *
     * @return the help information
     */
    public String getGeneralHelp() {
        StringBuilder help = new StringBuilder("Available commands:\n");
        CommandClassDiscoverer.discoverCommands(basePackage).stream()
            .filter(cmd -> !cmd.getCommandAnnotation().hidden())
            .forEach(cmd -> help.append(String.format("  %-15s - %s%n", 
                cmd.getName(), cmd.getDescription())));
        return help.toString();
    }

    private String processTemplate(String templateName, Map<String, Object> model) 
            throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private void writeOutput(String content, String filename) throws IOException {
        Files.writeString(Path.of(filename), content);
        log.info("Generated: {}", filename);
    }

    public List<CommandData> getCommands() {
        return CommandClassDiscoverer.discoverCommands(basePackage);
    }

    public CommandData getCommandData(String commandName) {
        return CommandClassDiscoverer.findCommand(basePackage, commandName);
    }
} 