package com.example.shelldemo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ${className} {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @BeforeEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    <#list commands as command>
    @Test
    public void test${command.name?cap_first}Command() {
        // Test with required parameters
        String[] args = {
            <#list command.options as option>
            <#if option.required>
            "${option.names[0]}", "test-value",
            </#if>
            </#list>
        };
        
        App.main(args);
        assertTrue(outContent.toString().contains("success"), 
            "Command ${command.name} should execute successfully");
    }

    @Test
    public void test${command.name?cap_first}CommandWithInvalidParameters() {
        // Test with invalid parameters
        String[] args = {
            "${command.options[0].names[0]}", "invalid-value"
        };
        
        App.main(args);
        assertTrue(errContent.toString().contains("error"), 
            "Command ${command.name} should fail with invalid parameters");
    }
    </#list>

    @Test
    public void testHelpOption() {
        String[] args = {"--help"};
        App.main(args);
        assertTrue(outContent.toString().contains("Usage:"), 
            "Help message should be displayed");
    }

    @Test
    public void testVersionOption() {
        String[] args = {"--version"};
        App.main(args);
        assertTrue(outContent.toString().contains("1.0-SNAPSHOT"), 
            "Version information should be displayed");
    }
}