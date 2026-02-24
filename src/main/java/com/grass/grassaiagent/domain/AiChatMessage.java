package com.grass.grassaiagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI消息记录表
 * ai_chat_message
 */
@TableName(value ="ai_chat_message")
@Data
public class AiChatMessage implements Serializable {
    /**
     * 自增主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 关联的会话ID
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * AI会话主表ID
     */
    @TableField(value = "chat_ai_id")
    private String chatAiId;

    /**
     * 消息顺序（从0递增）
     */
    @TableField(value = "message_order")
    private Integer messageOrder;

    /**
     * 角色类型（USER/ASSISTANT/SYSTEM）
     */
    @TableField(value = "role_type")
    private String roleType;

    /**
     * 消息内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 消息消耗的token数量
     */
    @TableField(value = "tokens")
    private Integer tokens;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}