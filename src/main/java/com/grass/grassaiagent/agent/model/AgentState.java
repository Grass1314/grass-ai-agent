package com.grass.grassaiagent.agent.model;

/**
 * @description: 代理执行状态枚举类
 * @author: Mr.Liuxq
 * @date 2026/3/30 11:03
 */
public enum AgentState {

    /**
     * 空闲
     */
    IDLE,

    /**
     * 执行中
     */
    RUNNING,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 错误
     */
    ERROR,

    /**
     * 成功
     */
    SUCCESS,

    /**
     * 失败
     */
    FAIL,

    /**
     * 超时
     */
    TIMEOUT,

    /**
     * 取消
     */
    CANCEL;
}
