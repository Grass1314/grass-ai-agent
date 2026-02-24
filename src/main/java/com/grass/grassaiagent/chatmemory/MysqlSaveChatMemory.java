package com.grass.grassaiagent.chatmemory;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.grass.grassaiagent.domain.AiChatMessage;
import com.grass.grassaiagent.domain.AiChatSession;
import com.grass.grassaiagent.service.AiChatMessageService;
import com.grass.grassaiagent.service.AiChatSessionService;
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
        // 校验会话是否存在
        AiChatSession existingSession = aiChatSessionService.getOne(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getSessionId, conversationId)
        );

        String sessionId;
        if (existingSession == null) {
            // 创建新会话
            AiChatSession aiChatSession = new AiChatSession();
            sessionId = UUID.randomUUID().toString();
            aiChatSession.setSessionId(sessionId);

            // 标题
            aiChatSession.setTitle(findFistMatchTitle(message.getText()));
            // 用户ID(使用用户真实ID，暂时随机生成)
            aiChatSession.setUserId("user_" + UUID.randomUUID());
            // 保存
            aiChatSessionService.save(aiChatSession);
        } else {
            sessionId = existingSession.getSessionId();
        }

        // 为新消息创建存储对象
        AiChatMessage aiChatMessage = new AiChatMessage();
        aiChatMessage.setSessionId(conversationId);
        aiChatMessage.setRoleType(message.getMessageType().getValue());
        aiChatMessage.setContent(message.getText());
        aiChatMessage.setChatAiId(sessionId);

        // 消息排序号
        AiChatMessage lastMessage = aiChatMessageService.getOne(
                new LambdaQueryWrapper<AiChatMessage>()
                        .select(AiChatMessage::getMessageOrder)
                        .eq(AiChatMessage::getSessionId, conversationId)
                        .orderByDesc(AiChatMessage::getMessageOrder)
                        .last("LIMIT 1")
        );
        aiChatMessage.setMessageOrder(lastMessage == null ? 1 : lastMessage.getMessageOrder() + 1);
        // 保存
        aiChatMessageService.save(aiChatMessage);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 校验消息列表
        if (CollUtil.isEmpty(messages)) {
            return;
        }

        // 获取或创建会话
        AiChatSession existingSession = aiChatSessionService.getOne(
                new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getSessionId, conversationId)
        );

        String sessionId;
        if (existingSession == null) {
            // 创建新会话
            AiChatSession aiChatSession = new AiChatSession();
            sessionId = UUID.randomUUID().toString();
            aiChatSession.setSessionId(sessionId);

            // 标题
            aiChatSession.setTitle(findFistMatchTitle(messages.getFirst().getText()));
            // 用户ID(使用用户真实ID，暂时随机生成)
            aiChatSession.setUserId("user_" + UUID.randomUUID());
            // 保存
            aiChatSessionService.save(aiChatSession);
        } else {
            sessionId = existingSession.getSessionId();
        }

        // 优化后的批量处理逻辑
        handleBatchMessages(conversationId, sessionId, messages);
    }

    /**
     * 优化的批量消息处理方法
     * 处理三种场景：
     * 1. 所有消息都需要存储
     * 2. 部分消息已存在，部分需要存储
     * 3. 只有最后一条是新的，其余都已存在
     */
    private void handleBatchMessages(String conversationId, String sessionId, List<Message> messages) {
        // 获取该会话已存在的最大消息序号
        Integer maxExistingOrder = getMaxMessageOrder(conversationId);
        int nextOrder = (maxExistingOrder == null) ? 1 : maxExistingOrder + 1;
        
        // 转换并过滤需要保存的消息
        List<AiChatMessage> messagesToSave = new ArrayList<>();
        
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            int expectedOrder = i + 1;
            
            // 如果该序号的消息已存在，则跳过
            if (maxExistingOrder != null && expectedOrder <= maxExistingOrder) {
                continue;
            }
            
            // 创建需要保存的消息对象
            AiChatMessage aiChatMessage = new AiChatMessage();
            aiChatMessage.setSessionId(conversationId);
            aiChatMessage.setRoleType(message.getMessageType().getValue());
            aiChatMessage.setContent(message.getText());
            aiChatMessage.setChatAiId(sessionId);
            aiChatMessage.setMessageOrder(nextOrder++);
            
            messagesToSave.add(aiChatMessage);
        }
        
        // 批量保存新消息
        if (!messagesToSave.isEmpty()) {
            aiChatMessageService.saveBatch(messagesToSave);
            log.info("批量保存 {} 条新消息到会话 {}", messagesToSave.size(), conversationId);
        } else {
            log.info("会话 {} 的所有消息均已存在，无需保存", conversationId);
        }
    }
    
    /**
     * 获取指定会话的最大消息序号
     */
    private Integer getMaxMessageOrder(String conversationId) {
        AiChatMessage lastMessage = aiChatMessageService.getOne(
                new LambdaQueryWrapper<AiChatMessage>()
                        .select(AiChatMessage::getMessageOrder)
                        .eq(AiChatMessage::getSessionId, conversationId)
                        .orderByDesc(AiChatMessage::getMessageOrder)
                        .last("LIMIT 1")
        );
        return lastMessage != null ? lastMessage.getMessageOrder() : null;
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 校验
        if (StrUtil.isBlank(conversationId) || lastN <= 0) {
            return List.of();
        }
        // 获取指定会话的会话对象
        LambdaQueryWrapper<AiChatMessage> queryWrapper = new LambdaQueryWrapper<AiChatMessage>();
        queryWrapper.eq(AiChatMessage::getSessionId, conversationId).orderByDesc(AiChatMessage::getMessageOrder);
        List<AiChatMessage> messages = aiChatMessageService.list(queryWrapper);
        // 如果需要最后N条消息，则返回指定数量的消息
        if (messages.size() > lastN) {
            messages = messages.subList(messages.size() - lastN, messages.size());
        }
        // 将数据库实体转换成Message对象  标准消息类型
        return messages.stream().map(msg -> switch (msg.getRoleType()) {
            case "user" -> new UserMessage(msg.getContent());
            case "assistant" -> new AssistantMessage(msg.getContent());
            default -> new SystemMessage(msg.getContent());
        }).collect(Collectors.toList());
    }

    @Override
    public void clear(String conversationId) {
        aiChatMessageService.remove(new LambdaQueryWrapper<AiChatMessage>().eq(AiChatMessage::getSessionId, conversationId));
        aiChatSessionService.remove(new LambdaQueryWrapper<AiChatSession>().eq(AiChatSession::getSessionId, conversationId));
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
