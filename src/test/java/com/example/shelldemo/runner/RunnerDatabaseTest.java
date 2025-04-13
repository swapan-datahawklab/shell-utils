package com.example.shelldemo.runner;

import com.example.shelldemo.analysis.CommandData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RunnerDatabaseTest {

    private RunnerDatabase runnerDatabase;
    
    @Mock
    private CommandData command1;
    
    @Mock
    private CommandData command2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        runnerDatabase = new RunnerDatabase();
        
        when(command1.getName()).thenReturn("test1");
        when(command2.getName()).thenReturn("test2");
    }

    @Test
    void testRunAllCommandTests() {
        List<CommandData> commands = Arrays.asList(command1, command2);
        runnerDatabase.runAllCommandTests(commands);
        
        assertNotNull(runnerDatabase.getTestResults());
        assertEquals(2, runnerDatabase.getTestResults().size());
        assertTrue(runnerDatabase.getTestResults().containsKey("test1"));
        assertTrue(runnerDatabase.getTestResults().containsKey("test2"));
    }

    @Test
    void testGetSuccessfulCommands() {
        List<CommandData> commands = Arrays.asList(command1, command2);
        runnerDatabase.runAllCommandTests(commands);
        
        List<CommandData> successfulCommands = runnerDatabase.getSuccessfulCommands();
        assertNotNull(successfulCommands);
        assertTrue(successfulCommands.size() <= 2);
    }
} 