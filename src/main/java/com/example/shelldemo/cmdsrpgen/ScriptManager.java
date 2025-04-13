package com.example.shelldemo.cmdsrpgen;

import java.io.File;
import java.util.List;

import com.example.shelldemo.analysis.Command;
import com.example.shelldemo.analysis.UtilCommandRegistry;
import com.example.shelldemo.analysis.UtilCommandRegistry.CommandData;
import com.example.shelldemo.cli.exception.ScriptDirectoryException;
import com.example.shelldemo.cli.exception.ScriptFileExistsException;

@SuppressWarnings("unused")
public final class ScriptManager {
    private ScriptManager() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void initializeScripts(List<Class<?>> commandClasses, String scriptDir, boolean overwrite) {
        File scriptsDirectory = new File(scriptDir);
        
        if (!scriptsDirectory.exists()) {
            if (!scriptsDirectory.mkdirs()) {
                throw new ScriptDirectoryException("Failed to create scripts directory: " + scriptDir);
            }
        } else if (!overwrite) {
            // Check if any scripts with the same names exist
            for (Class<?> commandClass : commandClasses) {
                Command cmd = commandClass.getAnnotation(Command.class);
                if (cmd != null) {
                    String scriptName = cmd.name();
                    File shScript = new File(scriptsDirectory, scriptName + ".sh");
                    File batScript = new File(scriptsDirectory, scriptName + ".bat");
                    
                    if (shScript.exists() || batScript.exists()) {
                        throw new ScriptFileExistsException("Script files already exist. Use --overwrite-scripts to overwrite them.");
                    }
                }
            }
        }
        
        generateScripts(commandClasses, scriptsDirectory);
    }

    public static void generateScripts(List<Class<?>> commandClasses, File outputDir) {
        List<UtilCommandRegistry.CommandData> commandDataList = commandClasses.stream()
            .map(clazz -> {
                Command cmd = clazz.getAnnotation(Command.class);
                String name = cmd != null ? cmd.name() : clazz.getSimpleName().toLowerCase();
                String description = cmd != null ? cmd.description() : "Command " + name;
                String usage = cmd != null ? cmd.usage() : "";
                return new UtilCommandRegistry.CommandData(name, description, usage, clazz, cmd);
            })
            .toList();
            
        ScriptGenerator.generateScripts(commandDataList, outputDir);
    }
} 