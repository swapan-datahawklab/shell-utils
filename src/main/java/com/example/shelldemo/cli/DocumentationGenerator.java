package com.example.shelldemo.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ArgGroup;
import java.lang.reflect.Field;
import java.util.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(name = "doc-generator", mixinStandardHelpOptions = true)
public class DocumentationGenerator {
    private static final Logger log = LoggerFactory.getLogger(DocumentationGenerator.class);

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

    private final Configuration freemarkerConfig;

    public DocumentationGenerator() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void generateDocumentation(List<Class<?>> commandClasses) {
        try {
            if (generateHtml) {
                generateInteractiveDoc(commandClasses);
            }
            if (generateConfig != null) {
                generateConfigTemplate(commandClasses);
            }
            if (generateApiDocs != null) {
                generateApiDocumentation(commandClasses);
            }
            if (generateTests != null) {
                generateTestCases(commandClasses);
            }
        } catch (Exception e) {
            log.error("Error generating documentation", e);
        }
    }

    private void generateInteractiveDoc(List<Class<?>> commandClasses) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("commands", collectCommandData(commandClasses));
        model.put("examples", collectExampleData());
        model.put("validation", collectValidationData());

        Template template = freemarkerConfig.getTemplate("interactive-docs.ftl");
        String output = processTemplate(template, model);
        writeOutput(output, "documentation.html");
        copyStaticResources();
    }

    private void generateConfigTemplate(List<Class<?>> commandClasses) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("commands", collectCommandData(commandClasses));

        String templateName = switch (generateConfig.toLowerCase()) {
            case "properties" -> "application.properties.ftl";
            case "yaml" -> "application.yaml.ftl";
            case "json" -> "config.json.ftl";
            case "xml" -> "config.xml.ftl";
            default -> throw new IllegalArgumentException("Unsupported config format: " + generateConfig);
        };

        Template template = freemarkerConfig.getTemplate(templateName);
        String output = processTemplate(template, model);
        writeOutput(output, "application." + generateConfig.toLowerCase());
    }

    private void generateApiDocumentation(List<Class<?>> commandClasses) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("commands", collectCommandData(commandClasses));
        model.put("info", collectApiInfo());

        String templateName = switch (generateApiDocs.toLowerCase()) {
            case "openapi" -> "openapi.yaml.ftl";
            case "swagger" -> "swagger.json.ftl";
            case "raml" -> "api.raml.ftl";
            default -> throw new IllegalArgumentException("Unsupported API doc format: " + generateApiDocs);
        };

        Template template = freemarkerConfig.getTemplate(templateName);
        String output = processTemplate(template, model);
        writeOutput(output, "api." + (generateApiDocs.equals("raml") ? "raml" : 
                                   generateApiDocs.equals("openapi") ? "yaml" : "json"));
    }

    private void generateTestCases(List<Class<?>> commandClasses) throws Exception {
        Map<String, Object> model = new HashMap<>();
        model.put("commands", collectCommandData(commandClasses));
        model.put("validation", collectValidationData());

        String templateName = generateTests.toLowerCase().equals("junit") ? 
                            "junit-tests.java.ftl" : "testng-tests.java.ftl";

        Template template = freemarkerConfig.getTemplate(templateName);
        String output = processTemplate(template, model);
        writeOutput(output, "CommandTest.java");
    }

    private String processTemplate(Template template, Map<String, Object> model) throws Exception {
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    private void writeOutput(String content, String filename) throws Exception {
        Path outputPath = outputFile != null ? outputFile.toPath() : Path.of(filename);
        Files.writeString(outputPath, content);
        log.info("Generated: {}", outputPath);
    }

    private void copyStaticResources() throws Exception {
        String[] resources = {
            "styles.css",
            "script.js",
            "highlight.pack.js",
            "search.js"
        };

        for (String resource : resources) {
            Path targetPath = Path.of("static", resource);
            Files.createDirectories(targetPath.getParent());
            Files.copy(getClass().getResourceAsStream("/static/" + resource), targetPath);
        }
    }

    private List<Map<String, Object>> collectCommandData(List<Class<?>> commandClasses) {
        List<Map<String, Object>> commands = new ArrayList<>();
        for (Class<?> commandClass : commandClasses) {
            Map<String, Object> commandData = new HashMap<>();
            Command cmd = commandClass.getAnnotation(Command.class);
            
            commandData.put("name", cmd != null ? cmd.name() : commandClass.getSimpleName());
            commandData.put("description", cmd != null ? cmd.description() : "");
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
        info.put("description", "API for generating documentation and configuration files");
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
                optionData.put("description", String.join(" ", option.description()));
                optionData.put("required", option.required());
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
                paramData.put("description", String.join(" ", params.description()));
                paramData.put("required", true);
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
                groupData.put("required", group.multiplicity() == "1");
                groupData.put("exclusive", group.exclusive());
                groups.add(groupData);
            }
        }
        return groups;
    }
} 