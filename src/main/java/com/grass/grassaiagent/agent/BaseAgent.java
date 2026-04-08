package com.grass.grassaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.grass.grassaiagent.agent.model.AgentState;
import com.grass.grassaiagent.exception.BusinessException;
import com.grass.grassaiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 *     抽象基础代理类，用于管理代理状态和执行流程
 *     提供状态转换、内存管理和基于步骤的执行循环的基础功能
 *     包含循环检测机制（参考 OpenManus BaseAgent）
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

    // ===== 循环检测参数（参考 OpenManus） =====
    /** 判定为重复的阈值：最近消息中出现 >= duplicateThreshold 条相同内容即视为卡住 */
    private int duplicateThreshold = 2;
    /** 连续卡住次数上限，超过后强制终止 */
    private int maxConsecutiveStuck = 3;
    /** 当前连续卡住计数器 */
    private int consecutiveStuckCount = 0;
    /** 保存原始 nextStepPrompt，cleanup 时恢复 */
    private String originalNextStepPrompt;

    /**
     * 执行方法
     * @param userPrompt 用户输入
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new BusinessException(ErrorCode.AGENT_RUNNING_ERROR);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new BusinessException(ErrorCode.AGENT_PROMPT_ERROR);
        }
        this.state = AgentState.RUNNING;
        this.originalNextStepPrompt = this.nextStepPrompt;
        messageList.add(new UserMessage(userPrompt));
        List<String> resultList = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                String stepResult = step();
                resultList.add("Step " + stepNumber + ": " + stepResult);

                // 循环检测：执行完每一步后检查是否陷入重复
                if (isStuck()) {
                    consecutiveStuckCount++;
                    if (consecutiveStuckCount >= maxConsecutiveStuck) {
                        log.warn("Agent stuck for {} consecutive steps, forcing termination", consecutiveStuckCount);
                        state = AgentState.FINISHED;
                        resultList.add("Terminated: Agent detected stuck in loop after "
                                + consecutiveStuckCount + " consecutive duplicate steps");
                        break;
                    }
                    handleStuckState();
                } else {
                    consecutiveStuckCount = 0;
                }
            }
            if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                state = AgentState.FINISHED;
                resultList.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", resultList);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "Error: " + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    emitter.send("错误: 无法从状态运行代理：" + this.state);
                    emitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitter.send("错误: 无效的提示词");
                    emitter.complete();
                    return;
                }
                this.state = AgentState.RUNNING;
                this.originalNextStepPrompt = this.nextStepPrompt;
                messageList.add(new UserMessage(userPrompt));
                try {
                    for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentStep = stepNumber;
                        log.info("Executing step {}/{}", stepNumber, maxSteps);
                        // 单步执行
                        String stepResult = step();
                        String result = "Step " + stepNumber + ": " + stepResult;

                        // 发送每一步的结果
                        emitter.send(result);
                    }
                    // 检查是否超出步骤限制
                    if (currentStep >= maxSteps && state != AgentState.FINISHED) {
                        state = AgentState.FINISHED;
                        emitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                    }
                    // 正常完成
                    emitter.complete();
                } catch (Exception e) {
                    state = AgentState.ERROR;
                    log.error("Error executing agent", e);
                    emitter.send("Error: " + e.getMessage());
                    emitter.complete();
                } finally {
                    this.cleanup();
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        // 设置超时与完成回调
        emitter.onTimeout(() -> {
            this.state = AgentState.TIMEOUT;
            this.cleanup();
            log.warn("SSE connection timed out");
        });
        emitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return emitter;
    }

    /**
     * 抽象方法，子类实现
     * @return 执行结果
     */
    public abstract String step();

    /**
     * 检测智能体是否陷入重复循环（参考 OpenManus BaseAgent.is_stuck）
     * 通过比对最近的 Assistant 消息内容判断是否重复
     */
    protected boolean isStuck() {
        List<Message> messages = getMessageList();
        if (messages.size() < 2) {
            return false;
        }

        // 获取最后一条消息内容
        Message lastMessage = messages.get(messages.size() - 1);
        String lastContent = lastMessage.getText();
        if (lastContent == null || lastContent.isBlank()) {
            return false;
        }

        // 统计前面消息中与最后一条内容相同的 Assistant 消息数量
        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            Message msg = messages.get(i);
            if (msg instanceof AssistantMessage && lastContent.equals(msg.getText())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= duplicateThreshold;
    }

    /**
     * 处理卡住状态：修改 nextStepPrompt 引导模型改变策略（参考 OpenManus BaseAgent.handle_stuck_state）
     */
    protected void handleStuckState() {
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted.";
        setNextStepPrompt(stuckPrompt + "\n" + getNextStepPrompt());
        log.warn("Agent detected stuck state (count: {}), injecting strategy-change prompt", consecutiveStuckCount);
    }

    /**
     * 清理方法，恢复初始状态
     * 子类重写此方法时需调用 super.cleanup()
     */
    public void cleanup() {
        this.state = AgentState.IDLE;
        this.currentStep = 0;
        this.consecutiveStuckCount = 0;
        this.messageList.clear();
        if (this.originalNextStepPrompt != null) {
            this.nextStepPrompt = this.originalNextStepPrompt;
            this.originalNextStepPrompt = null;
        }
    }
}
