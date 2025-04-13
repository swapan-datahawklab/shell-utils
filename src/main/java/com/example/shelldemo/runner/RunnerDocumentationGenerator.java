package com.example.shelldemo.runner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ArgGroup;
import java.lang.reflect.Field;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.cli.exception.ResourceCopyException;
import com.example.shelldemo.analysis.RuntimeAnalysisDocumentation;

import java.io.IOException;

@Command(name = "doc-generator", mixinStandardHelpOptions = true)
public class RunnerDocumentationGenerator {
    private static final Logger log = LoggerFactory.getLogger(RunnerDocumentationGenerator.class);
    private static final String COMMANDS_KEY = "commands";
    private static final String DESCRIPTION_KEY = "description";
    private static final String REQUIRED_KEY = "required";

    @Option(names = {"--output-format"}, description = "Output format (markdown, json, text)")
    private String outputFormat = "text";

    @Option(names = {"--include-hidden"}, description = "Include hidden options in output")
    private boolean includeHidden = false;

    @Option(names = {"--generate-html"}, description = "Generate interactive HTML documentation")
    private boolean generateHtml;

    @Option(names = {"--generate-config"}, 
            description = "Generate configuration templates (properties, yaml, json, xml)")
    private String generateConfig;

    @Option(names = {"--generate-api-docs"}, 
            description = "Generate API documentation (openapi, swagger, raml)")
    private String generateApiDocs;

    @Option(names = {"--generate-tests"}, 
            description = "Generate test cases (junit, testng)")
    private String generateTests;

    @Option(names = {"--template-dir"}, 
            description = "Directory containing custom templates")
    private File templateDir;

    @Option(names = {"--output-file"}, description = "Output file path")
    private File outputFile;

    @Option(names = {"--template-vars"}, 
            description = "Custom template variables in key=value format",
            split = ",")
    private Map<String, String> templateVars = new HashMap<>();

    private final RuntimeAnalysisDocumentation documentation;

    public RunnerDocumentationGenerator() {
        try {
            this.documentation = new RuntimeAnalysisDocumentation();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize documentation generator", e);
        }
    }

    public void generateDocumentation(List<Class<?>> commandClasses) {
        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put(COMMANDS_KEY, collectCommandData(commandClasses));
            vars.put("examples", collectExampleData());
            vars.put("validation", collectValidationData());
            vars.put("info", collectApiInfo());

            if (generateHtml) {
                documentation.generateDocumentation(vars, Path.of("documentation.html"));
                copyStaticResources();
            }
            if (generateConfig != null) {
                String configFile = "application." + generateConfig.toLowerCase();
                documentation.generateDocumentation(vars, Path.of(configFile));
            }
            if (generateApiDocs != null) {
                String extension = getApiDocExtension(generateApiDocs);
                documentation.generateDocumentation(vars, Path.of("api." + extension));
            }
            if (generateTests != null) {
                documentation.generateDocumentation(vars, Path.of("CommandTest.java"));
            }
        } catch (Exception e) {
            log.error("Error generating documentation", e);
        }
    }

    private String getApiDocExtension(String format) {
        if (format.equals("raml")) {
            return "raml";
        }
        return format.equals("openapi") ? "yaml" : "json";
    }

    private void copyStaticResources() {
        String[] resources = {
            "styles.css",
            "script.js",
            "highlight.pack.js",
            "search.js"
        };

        for (String resource : resources) {
            try {
                Path targetPath = Path.of("static", resource);
                Files.createDirectories(targetPath.getParent());
                Files.copy(getClass().getResourceAsStream("/static/" + resource), targetPath);
            } catch (IOException e) {
                throw new ResourceCopyException("Failed to copy static resource: " + resource, e);
            }
        }
    }

    private List<Map<String, Object>> collectCommandData(List<Class<?>> commandClasses) {
        List<Map<String, Object>> commands = new ArrayList<>();
        for (Class<?> commandClass : commandClasses) {
            Map<String, Object> commandData = new HashMap<>();
            Command cmd = commandClass.getAnnotation(Command.class);
            
            commandData.put("name", cmd != null ? cmd.name() : commandClass.getSimpleName());
            commandData.put(DESCRIPTION_KEY, cmd != null ? cmd.description() : "");
            commandData.put("options", collectOptions(commandClass));
            commandData.put("parameters", collectParameters(commandClass));
            commandData.put("groups", collectOptionGroups(commandClass));
            
            commands.add(commandData);
        }
        return commands;
    }

    private Map<String, Object> collectExampleData() {
        Map<String, Object> examples = new HashMap<>();
        examples.put("generateHtml", "java -jar target/shdemmo-1.0-SNAPSHOT.jar --generate-html");
        examples.put("generateConfig", "java -jar target/shdemmo-1.0-SNAPSHOT.jar --generate-config properties");
        examples.put("generateApiDocs", "java -jar target/shdemmo-1.0-SNAPSHOT.jar --generate-api-docs openapi");
        examples.put("generateTests", "java -jar target/shdemmo-1.0-SNAPSHOT.jar --generate-tests junit");
        return examples;
    }

    private Map<String, Object> collectValidationData() {
        Map<String, Object> validation = new HashMap<>();
        validation.put("requiredOptions", Arrays.asList("--generate-html", "--generate-config", "--generate-api-docs", "--generate-tests"));
        validation.put("mutuallyExclusive", Arrays.asList(
            Arrays.asList("--generate-html", "--generate-config", "--generate-api-docs", "--generate-tests")
        ));
        validation.put("fileExtensions", Map.of(
            "properties", ".properties",
            "yaml", ".yaml",
            "json", ".json",
            "xml", ".xml"
        ));
        return validation;
    }

    private Map<String, Object> collectApiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("title", "Command Line Interface API");
        info.put(DESCRIPTION_KEY, "API for generating documentation and configuration files");
        info.put("version", "1.0.0");
        info.put("contact", Map.of(
            "name", "ShellDemo Team",
            "email", "support@example.com"
        ));
        return info;
    }

    private List<Map<String, Object>> collectOptions(Class<?> commandClass) {
        List<Map<String, Object>> options = new ArrayList<>();
        for (Field field : commandClass.getDeclaredFields()) {
            Option option = field.getAnnotation(Option.class);
            if (option != null && (!option.hidden() || includeHidden)) {
                Map<String, Object> optionData = new HashMap<>();
                optionData.put("name", field.getName());
                optionData.put("type", field.getType().getSimpleName());
                optionData.put("names", Arrays.asList(option.names()));
                optionData.put(DESCRIPTION_KEY, String.join(" ", option.description()));
                optionData.put(REQUIRED_KEY, option.required());
                optionData.put("hidden", option.hidden());
                optionData.put("defaultValue", option.defaultValue().equals("__no_default__") ? null : option.defaultValue());
                options.add(optionData);
            }
        }
        return options;
    }

    private List<Map<String, Object>> collectParameters(Class<?> commandClass) {
        List<Map<String, Object>> parameters = new ArrayList<>();
        for (Field field : commandClass.getDeclaredFields()) {
            Parameters params = field.getAnnotation(Parameters.class);
            if (params != null) {
                Map<String, Object> paramData = new HashMap<>();
                paramData.put("name", field.getName());
                paramData.put("index", params.index());
                paramData.put(DESCRIPTION_KEY, String.join(" ", params.description()));
                paramData.put(REQUIRED_KEY, true);
                parameters.add(paramData);
            }
        }
        return parameters;
    }

    private List<Map<String, Object>> collectOptionGroups(Class<?> commandClass) {
        List<Map<String, Object>> groups = new ArrayList<>();
        for (Field field : commandClass.getDeclaredFields()) {
            ArgGroup group = field.getAnnotation(ArgGroup.class);
            if (group != null) {
                Map<String, Object> groupData = new HashMap<>();
                groupData.put("name", field.getName());
                groupData.put(REQUIRED_KEY, group.multiplicity() == "1");
                groupData.put("exclusive", group.exclusive());
                groups.add(groupData);
            }
        }
        return groups;
    }
} 