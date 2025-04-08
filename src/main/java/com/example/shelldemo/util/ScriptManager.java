package com.example.shelldemo.util;

import picocli.CommandLine.Command;
import java.io.File;
import java.util.List;

public class ScriptManager {
    private final ScriptGenerator scriptGenerator;

    public ScriptManager(ScriptGenerator scriptGenerator) {
        this.scriptGenerator = scriptGenerator;
    }

    public void initializeScripts(List<Class<?>> commandClasses, String scriptDir, boolean overwrite) {
        File scriptsDirectory = new File(scriptDir);
        
        if (!scriptsDirectory.exists()) {
            if (!scriptsDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create scripts directory: " + scriptDir);
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
                        throw new RuntimeException("Script files already exist. Use --overwrite-scripts to overwrite them.");
                    }
                }
            }
        }
        
        scriptGenerator.generateScripts(commandClasses, scriptsDirectory, overwrite);
    }
} 