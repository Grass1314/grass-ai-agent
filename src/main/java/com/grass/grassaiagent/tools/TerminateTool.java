package com.grass.grassaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * @description: 终止工具
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/30 15:09
 */
public class TerminateTool {

    @Tool(name = "terminate", description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}

