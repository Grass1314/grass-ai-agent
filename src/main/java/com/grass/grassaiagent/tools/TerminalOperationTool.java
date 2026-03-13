package com.grass.grassaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @description: 终端操作工具，在服务器上执行Shell命令
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class TerminalOperationTool {

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    private static final List<String> BLOCKED_COMMANDS = List.of(
            "rm -rf /", "mkfs", "dd if=", ":(){:|:&};:", "shutdown", "reboot",
            "halt", "poweroff", "init 0", "init 6"
    );

    @Tool(description = "Execute a shell command on the server and return its output. Useful for system operations, file listing, process management, etc.")
    public String executeCommand(
            @ToolParam(description = "The shell command to execute, e.g. 'ls -la', 'pwd', 'whoami'") String command,
            @ToolParam(description = "Timeout in seconds, default 30") long timeoutSeconds) {

        if (timeoutSeconds <= 0 || timeoutSeconds > 120) {
            timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;
        }

        String cmdLower = command.toLowerCase().trim();
        for (String blocked : BLOCKED_COMMANDS) {
            if (cmdLower.contains(blocked)) {
                return "Error: Command blocked for safety reasons - contains dangerous operation: " + blocked;
            }
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("cmd", "/c", command);
            } else {
                pb = new ProcessBuilder("sh", "-c", command);
            }
            pb.redirectErrorStream(true);

            Process process = pb.start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                int lineCount = 0;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    if (++lineCount >= 500) {
                        output.append("...(output truncated at 500 lines)\n");
                        break;
                    }
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Command timed out after " + timeoutSeconds + " seconds.\nPartial output:\n" + output;
            }

            int exitCode = process.exitValue();
            String result = output.toString().trim();
            if (result.isEmpty()) {
                result = "(no output)";
            }
            return "Exit code: " + exitCode + "\nOutput:\n" + result;
        } catch (Exception e) {
            return "Error executing command: " + e.getMessage();
        }
    }
}
