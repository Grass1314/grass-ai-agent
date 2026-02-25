-- ============================================================
-- AI 会话存储 - 推荐表结构（MySQL）
-- 设计要点：
-- 1. 会话表用 conversation_id 作为业务唯一键（与客户端 chatId 一致）
-- 2. 消息表仅通过 session_id 外键关联会话主键，无冗余字段
-- 3. 索引覆盖：按用户查会话列表、按会话查最近 N 条消息
-- 4. 级联删除：删会话时自动删消息
-- ============================================================

-- 会话表：一个 conversation_id 对应一条会话
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
    `id`              BIGINT       NOT NULL COMMENT '主键',
    `conversation_id` VARCHAR(64)  NOT NULL COMMENT '客户端会话ID（如 chatId），业务唯一',
    `user_id`         VARCHAR(64)  NOT NULL COMMENT '用户标识',
    `title`           VARCHAR(255) DEFAULT NULL COMMENT '会话标题',
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_user_update` (`user_id`, `update_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI会话表';

-- 消息表：仅通过 session_id 关联会话，无冗余 conversation_id
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
    `id`             BIGINT       NOT NULL COMMENT '主键',
    `session_id`     BIGINT       NOT NULL COMMENT '关联 ai_chat_session.id',
    `message_order`  INT          NOT NULL COMMENT '消息序号，从 1 递增',
    `role_type`      VARCHAR(20)  NOT NULL COMMENT '角色：user / assistant / system',
    `content`        TEXT         NOT NULL COMMENT '消息内容',
    `tokens`         INT          DEFAULT NULL COMMENT '消耗 token 数',
    `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_session_order` (`session_id`, `message_order`),
    KEY `idx_session_order` (`session_id`, `message_order`),
    CONSTRAINT `fk_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI消息记录表';
