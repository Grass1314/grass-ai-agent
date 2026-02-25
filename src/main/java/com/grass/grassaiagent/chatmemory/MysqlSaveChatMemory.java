package com.grass.grassaiagent.chatmemory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grass.grassaiagent.domain.AiChatMessage;
import com.grass.grassaiagent.domain.AiChatSession;
import com.grass.grassaiagent.service.aiChat.AiChatMessageService;
import com.grass.grassaiagent.service.aiChat.AiChatSessionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 基于mysql的会话记忆
 * @date 2026/02/24 16:42
 */
@Slf4j
@Component
public class MysqlSaveChatMemory implements ChatMemory {

    @Resource
    private AiChatSessionService aiChatSessionService;

    @Resource
    private AiChatMessageService aiChatMessageService;

    @Override
    public void add(String conversationId, Message message) {
        AiChatSession session = getOrCreateSession(conversationId, message.getText());

        AiChatMessage aiChatMessage = new AiChatMessage();
        aiChatMessage.setSessionId(session.getId());
        aiChatMessage.setRoleType(message.getMessageType().getValue());
        aiChatMessage.setContent(message.getText());

        Integer maxOrder = getMaxMessageOrder(session.getId());
        aiChatMessage.setMessageOrder(maxOrder == null ? 1 : maxOrder + 1);
        aiChatMessageService.save(aiChatMessage);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        AiChatSession session = getOrCreateSession(conversationId, messages.getFirst().getText());
        handleBatchMessages(session.getId(), messages);
    }

    /**
     * 根据 conversationId 获取或创建会话（保证 ai_chat_session 中一条会话对应当前对话）。
     */
    private AiChatSession getOrCreateSession(String conversationId, String firstMessageText) {
        AiChatSession existing = aiChatSessionService.getOne(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getConversationId, conversationId)
        );
        if (existing != null) {
            return existing;
        }
        AiChatSession session = new AiChatSession();
        session.setConversationId(conversationId);
        session.setTitle(findFistMatchTitle(firstMessageText));
        session.setUserId("user_" + UUID.randomUUID());
        aiChatSessionService.save(session);
        return session;
    }

    /**
     * 批量消息处理：只持久化尚未存在的消息（请求 + 大模型响应）。
     *
     * @param sessionId 会话主键 ai_chat_session.id
     */
    private void handleBatchMessages(String sessionId, List<Message> messages) {
        Integer maxExistingOrder = getMaxMessageOrder(sessionId);
        int nextOrder = (maxExistingOrder == null) ? 1 : maxExistingOrder + 1;

        List<AiChatMessage> messagesToSave = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            int expectedOrder = i + 1;
            if (maxExistingOrder != null && expectedOrder <= maxExistingOrder) {
                continue;
            }
            AiChatMessage aiChatMessage = new AiChatMessage();
            aiChatMessage.setSessionId(sessionId);
            aiChatMessage.setRoleType(message.getMessageType().getValue());
            aiChatMessage.setContent(message.getText());
            aiChatMessage.setMessageOrder(nextOrder++);
            messagesToSave.add(aiChatMessage);
        }

        if (!messagesToSave.isEmpty()) {
            aiChatMessageService.saveBatch(messagesToSave);
            log.info("批量保存 {} 条新消息到会话 {}", messagesToSave.size(), sessionId);
        }
    }

    /**
     * 获取指定会话的最大消息序号（按 session 主键查）
     */
    private Integer getMaxMessageOrder(String sessionId) {
        AiChatMessage lastMessage = aiChatMessageService.getOne(
                new LambdaQueryWrapper<AiChatMessage>()
                        .select(AiChatMessage::getMessageOrder)
                        .eq(AiChatMessage::getSessionId, sessionId)
                        .orderByDesc(AiChatMessage::getMessageOrder)
                        .last("LIMIT 1")
        );
        return lastMessage != null ? lastMessage.getMessageOrder() : null;
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        if (StrUtil.isBlank(conversationId) || lastN <= 0) {
            return List.of();
        }
        AiChatSession session = aiChatSessionService.getOne(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getConversationId, conversationId)
        );
        if (session == null) {
            return List.of();
        }
        LambdaQueryWrapper<AiChatMessage> queryWrapper = new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, session.getId())
                .orderByAsc(AiChatMessage::getMessageOrder);
        List<AiChatMessage> messages = aiChatMessageService.list(queryWrapper);
        if (messages.size() > lastN) {
            messages = messages.subList(messages.size() - lastN, messages.size());
        }
        return messages.stream().map(msg -> switch (msg.getRoleType()) {
            case "user" -> new UserMessage(msg.getContent());
            case "assistant" -> new AssistantMessage(msg.getContent());
            default -> new SystemMessage(msg.getContent());
        }).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        AiChatSession session = aiChatSessionService.getOne(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getConversationId, conversationId)
        );
        if (session != null) {
            aiChatSessionService.removeById(session.getId());
        }
    }

    public String findFistMatchTitle(String input) {
        if (input == null) {
            return null;
        }
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // 检查逗号,裁剪掉后面的内容
            if (c == ',' || c == '，') {
                return input.substring(0, i);
            }
        }
        return "";
    }
}
