package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalOperationToolTest {

    private TerminalOperationTool tool;

    @BeforeEach
    public void setUp() {
        tool = new TerminalOperationTool();
    }

    @Test
    public void testExecuteEcho() {
        String result = tool.executeCommand("echo 'Hello AI Agent'", 10);
        assertNotNull(result);
        assertTrue(result.contains("Hello AI Agent"));
        assertTrue(result.contains("Exit code: 0"));
        System.out.println(result);
    }

    @Test
    public void testExecutePwd() {
        String result = tool.executeCommand("pwd", 10);
        assertNotNull(result);
        assertTrue(result.contains("Exit code: 0"));
        System.out.println(result);
    }

    @Test
    public void testExecuteLs() {
        String result = tool.executeCommand("ls -la", 10);
        assertNotNull(result);
        assertTrue(result.contains("Exit code: 0"));
        System.out.println(result);
    }

    @Test
    public void testExecuteDate() {
        String result = tool.executeCommand("date", 10);
        assertNotNull(result);
        assertTrue(result.contains("Exit code: 0"));
        System.out.println(result);
    }

    @Test
    public void testBlockedCommand() {
        String result = tool.executeCommand("rm -rf /", 10);
        assertNotNull(result);
        assertTrue(result.contains("blocked"));
        System.out.println(result);
    }

    @Test
    public void testCommandTimeout() {
        String result = tool.executeCommand("sleep 10", 2);
        assertNotNull(result);
        assertTrue(result.contains("timed out"));
        System.out.println(result);
    }

    @Test
    public void testInvalidCommand() {
        String result = tool.executeCommand("this_command_does_not_exist_xyz", 5);
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testDefaultTimeout() {
        String result = tool.executeCommand("echo 'default timeout test'", -1);
        assertNotNull(result);
        assertTrue(result.contains("default timeout test"));
        System.out.println(result);
    }
}
