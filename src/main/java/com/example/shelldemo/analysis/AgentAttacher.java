package com.example.shelldemo.analysis;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.shelldemo.exception.BaseException;
import com.example.shelldemo.exception.GlobalExceptionHandler;
import com.example.shelldemo.cli.exception.CommandExecutionException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class AgentAttacher {
    private static final Logger log = LoggerFactory.getLogger(AgentAttacher.class);
    private static final String JAVA_VERSION_PROPERTY = "java.version";
    
    private AgentAttacher() {
        // Prevent instantiation
    }
    
    public static class AgentAttachmentException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public AgentAttachmentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static void attachToProcess(String processId, String agentJarPath) throws BaseException {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(processId);
            vm.loadAgent(agentJarPath);
        } catch (com.sun.tools.attach.AttachNotSupportedException | IOException | 
                 com.sun.tools.attach.AgentLoadException | com.sun.tools.attach.AgentInitializationException e) {
            String context = "Agent attachment";
            String additionalInfo = String.format("Process: %s, JAR: %s, Java version: %s", 
                processId, agentJarPath, System.getProperty(JAVA_VERSION_PROPERTY));
            throw GlobalExceptionHandler.handleException(context, additionalInfo).apply(e);
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) {
                    log.error("Failed to detach from process {}: {}", processId, e.getMessage());
                }
            }
        }
    }
    
    public static void listJavaProcesses() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        log.info("Available Java processes:");
        for (VirtualMachineDescriptor vm : vms) {
            log.info("PID: {} - {}", vm.id(), vm.displayName());
        }
    }

    @Command(name = "attach-agent", 
             description = "Attaches the monitoring agent to a running Java process",
             usage = "attach-agent <pid> <agent-jar-path>")
    public static class AttachCommand {
        public String execute() throws CommandExecutionException {
            String pid = System.getProperty("pid");
            String agentJarPath = System.getProperty("agent-jar-path");

            if (pid == null || agentJarPath == null) {
                return "Error: Missing required arguments. Usage: attach-agent <pid> <agent-jar-path>";
            }

            if (!new File(agentJarPath).exists()) {
                return "Error: Agent JAR file not found: " + agentJarPath;
            }

            try {
                attachToProcess(pid, agentJarPath);
                return "Successfully attached agent to process " + pid;
            } catch (BaseException e) {
                throw new CommandExecutionException("Failed to attach agent: " + e.getMessage());
            }
        }
    }

    @Command(name = "list-java-processes", 
             description = "Lists all running Java processes",
             usage = "list-java-processes")
    public static class ListProcessesCommand {
        public String execute() {
            StringBuilder output = new StringBuilder("Available Java processes:\n");
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            for (VirtualMachineDescriptor vm : vms) {
                output.append(String.format("PID: %s - %s%n", vm.id(), vm.displayName()));
            }
            return output.toString();
        }
    }
} 