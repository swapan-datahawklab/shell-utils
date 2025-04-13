package com.example.shelldemo;

import com.example.shelldemo.analysis.CommandService;
import com.example.shelldemo.analysis.UtilCommandRegistry;
import com.example.shelldemo.cli.exception.CommandExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.ArrayList;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final CommandService commandService;

    public App() {
        this.commandService = new CommandService("com.example.shelldemo.commands");
    }

    public void run(String[] args) {
        try {
            List<Class<?>> commandClasses = discoverCommands();
            UtilCommandRegistry.initialize(commandClasses);

            if (args.length > 0) {
                handleCommand(args);
            } else {
                startInteractiveMode();
            }
        } catch (CommandExecutionException e) {
            log.error("Command execution failed", e);
            System.exit(1);
        } catch (Exception e) {
            log.error("Application error", e);
            System.exit(1);
        }
    }

    private List<Class<?>> discoverCommands() {
        // Implement command discovery logic
        return new ArrayList<>();
    }

    private void handleCommand(String[] args) throws CommandExecutionException {
        String command = args[0].toLowerCase();
        try {
            switch (command) {
                case "help" -> {
                    if (args.length > 1) {
                        commandService.executeCommand("help", new String[]{args[1]});
                    } else {
                        commandService.executeCommand("help", new String[]{});
                    }
                }
                case "doc" -> generateDocumentation(args);
                default -> commandService.executeCommand(command, new String[]{});
            }
        } catch (CommandExecutionException e) {
            log.error("Failed to execute command: " + command, e);
            throw e;
        }
    }

    private void generateDocumentation(String[] args) throws CommandExecutionException {
        if (args.length < 2) {
            try {
                commandService.executeCommand("help", new String[]{"doc"});
            } catch (CommandExecutionException e) {
                log.error("Failed to show documentation help", e);
                throw e;
            }
            return;
        }
        try {
            commandService.generateDocumentation(args[1]);
        } catch (Exception e) {
            log.error("Documentation generation failed", e);
            try {
                commandService.executeCommand("help", new String[]{"Error generating documentation: " + e.getMessage()});
            } catch (CommandExecutionException ex) {
                log.error("Failed to show error help", ex);
                throw ex;
            }
        }
    }

    private void startInteractiveMode() throws CommandExecutionException {
        try {
            commandService.executeCommand("help", new String[]{});
        } catch (CommandExecutionException e) {
            log.error("Failed to start interactive mode", e);
            throw e;
        }
    }

    public static void main(String[] args) {
        log.info("Starting application with args: {}", (Object) args);
        new App().run(args);
    }
} 