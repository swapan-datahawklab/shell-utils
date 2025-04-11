package com.example.shelldemo.agent;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class AgentAttacher {
    private static final Logger log = LoggerFactory.getLogger(AgentAttacher.class);
    
    public static class AgentAttachmentException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public AgentAttachmentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    public static void attachToProcess(String processId, String agentJarPath) throws AgentAttachmentException {
        VirtualMachine vm = null;
        try {
            vm = VirtualMachine.attach(processId);
            vm.loadAgent(agentJarPath);
        } catch (Exception e) {
            String msg = String.format("Failed to attach agent to process %s with JAR %s: %s", 
                processId, agentJarPath, e.getMessage());
            log.error("{} - Process details: {}", msg, getProcessDetails(processId), e);
            throw new AgentAttachmentException(msg, e);
        } finally {
            if (vm != null) {
                try {
                    vm.detach();
                } catch (Exception e) {
                    String msg = String.format("Failed to detach from process %s: %s", processId, e.getMessage());
                    log.error("{} - Process details: {}", msg, getProcessDetails(processId), e);
                }
            }
        }
    }
    
    private static String getProcessDetails(String processId) {
        try {
            List<VirtualMachineDescriptor> vms = VirtualMachine.list();
            return vms.stream()
                    .filter(vm -> vm.id().equals(processId))
                    .findFirst()
                    .map(vm -> String.format("Display name: %s", vm.displayName()))
                    .orElse("Process not found");
        } catch (Exception e) {
            log.error("Failed to get process details for PID {}: {}", processId, e.getMessage(), e);
            return "Unable to get process details";
        }
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
        } catch (AgentAttachmentException e) {
            log.error("Error attaching agent: {} - Process details: {}", 
                e.getMessage(), getProcessDetails(args[0]), e);
        }
    }
} 