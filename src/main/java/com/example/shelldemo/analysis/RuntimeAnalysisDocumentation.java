package com.example.shelldemo.analysis;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Map;



public class RuntimeAnalysisDocumentation {
    private static final Logger log = LoggerFactory.getLogger(RuntimeAnalysisDocumentation.class);
    private final Configuration freemarkerConfig;

    public RuntimeAnalysisDocumentation() {
        freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
        freemarkerConfig.setDefaultEncoding("UTF-8");
        freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public void generateDocumentation(Map<String, Object> templateVars, Path outputPath) throws DocumentationGenerationException {
        try {
            Template template = freemarkerConfig.getTemplate("runtime-analysis.ftl");
            String output = processTemplate(template, templateVars);
            writeOutput(output, outputPath.toFile());
            log.info("Generated runtime analysis documentation at: {}", outputPath);
        } catch (IOException e) {
            throw new TemplateLoadException("Failed to load template file: " + e.getMessage());
        }
    }

    private String processTemplate(Template template, Map<String, Object> model) throws TemplateProcessException {
        try (StringWriter writer = new StringWriter()) {
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new TemplateProcessException("Failed to process template: " + e.getMessage());
        }
    }

    private void writeOutput(String content, File outputFile) throws OutputWriteException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(content);
        } catch (IOException e) {
            throw new OutputWriteException("Failed to write documentation to file: " + e.getMessage());
        }
    }
} 