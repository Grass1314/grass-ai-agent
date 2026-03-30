package com.grass.grassaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @description: ReAct 模式的代理抽象类
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/30 14:07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return 行动执行结果
     */
    public abstract String act();

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "Thought completed, no action needed";
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志
            log.error("Error occurred during ReActAgent run: {}", e.getMessage());
            return "Step execution failed: " + e.getMessage();
        }
    }
}

