package com.example.shelldemo.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.example.shelldemo.exception.AgentException;
import com.example.shelldemo.exception.GlobalExceptionHandler;

public class AgentAttacher {
    private static final Logger log = LoggerFactory.getLogger(AgentAttacher.class);
    private static final String JAVA_VERSION_PROPERTY = "java.version";
    
    public static class AgentAttachmentException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public AgentAttachmentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static void attachToProcess(String processId, String agentJarPath) throws AgentException {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(processId);
            vm.loadAgent(agentJarPath);
        } catch (com.sun.tools.attach.AttachNotSupportedException | IOException | 
                 com.sun.tools.attach.AgentLoadException | com.sun.tools.attach.AgentInitializationException e) {
            String context = "Agent attachment";
            String additionalInfo = String.format("Process: %s, JAR: %s, Java version: %s", 
                processId, agentJarPath, System.getProperty(JAVA_VERSION_PROPERTY));
            throw GlobalExceptionHandler.handleException(AgentException.class, context, additionalInfo, e);
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (IOException e) {
                    String context = "Process detachment";
                    String additionalInfo = String.format("Process: %s, Java version: %s", 
                        processId, System.getProperty(JAVA_VERSION_PROPERTY));
                    GlobalExceptionHandler.handleException(AgentException.class, context, additionalInfo, e);
                }
            }
        }
    }
    
    private static String getProcessDetails(String processId) {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        return vms.stream()
                .filter(vm -> vm.id().equals(processId))
                .findFirst()
                .map(vm -> String.format("Display name: %s", vm.displayName()))
                .orElse("Process not found");
    }
    
    public static void listJavaProcesses() {
        List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        log.info("Available Java processes:");
        for (VirtualMachineDescriptor vm : vms) {
            log.info("PID: {} - {}", vm.id(), vm.displayName());
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            log.info("Usage: java -jar agent-attacher.jar <pid> <agent-jar-path>");
            listJavaProcesses();
            return;
        }
        
        try {
            String pid = args[0];
            String agentJarPath = args[1];
            
            if (!new File(agentJarPath).exists()) {
                log.error("Agent JAR file not found: {}", agentJarPath);
                return;
            }
            
            attachToProcess(pid, agentJarPath);
            log.info("Successfully attached agent to process {}", pid);
        } catch (AgentException e) {
            log.error("Error attaching agent: {} - Process details: {}", 
                e.getMessage(), getProcessDetails(args[0]), e);
        }
    }
} 