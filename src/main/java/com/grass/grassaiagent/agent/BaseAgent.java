package com.grass.grassaiagent.agent;

import cn.hutool.core.util.StrUtil;

import com.grass.grassaiagent.agent.model.AgentState;
import com.grass.grassaiagent.exception.BusinessException;
import com.grass.grassaiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *     抽象基础代理类，用于管理代理状态和执行流程
 *     提供状态转换、内存管理和基于步骤的执行循环的基础功能
 *     子类必须实现step方法
 * </p>
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/30 11:13
 */
@Slf4j
@Data
public abstract class BaseAgent {

    // 代理名称
    private String name;

    // 提示词
    private String systemPrompt;
    private String nextStepPrompt;

    // 代理状态
    private AgentState state = AgentState.IDLE;

    // 执行步骤控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory(需要自主维护上会话下文)
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行方法
     * @param userPrompt 用户输入
     * @return 执行结果
     */
    public String run(String userPrompt) {
        // 1. check state
        if (this.state != AgentState.IDLE) {
            throw new BusinessException(ErrorCode.AGENT_RUNNING_ERROR);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new BusinessException(ErrorCode.AGENT_PROMPT_ERROR);
        }
        // 更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                // 单步执行
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                resultList.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                resultList.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", resultList);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "Error: " + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 抽象方法，子类实现
     * @return 执行结果
     */
    public abstract String step();

    /**
     * 清理方法
     * 子类重写此方法，用于清理资源
     */
    public void  cleanup() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.messageList.clear();
    }


}
