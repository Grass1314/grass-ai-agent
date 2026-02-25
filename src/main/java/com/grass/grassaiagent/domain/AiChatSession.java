package com.grass.grassaiagent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI会话表
 * ai_chat_session
 */
@TableName(value ="ai_chat_session")
@Data
public class AiChatSession implements Serializable {
    /**
     * 自增主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 客户端会话ID（与 chatId 一致），业务唯一
     */
    @TableField(value = "conversation_id")
    private String conversationId;

    /**
     * 用户标识
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 会话标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}