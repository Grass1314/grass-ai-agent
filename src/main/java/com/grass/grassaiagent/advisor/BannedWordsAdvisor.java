package com.grass.grassaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 违禁词 Advisor
 * @date 2026/02/24 14:49
 */
@Slf4j
public class BannedWordsAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    // 定义违禁词列表
    private static final List<String> BANNED_WORDS = Arrays.asList(
            "暴力", "色情", "赌博", "毒品", "诈骗", "恐怖主义",
            "违法", "犯罪", "攻击", "辱骂", "歧视", "仇恨言论"
    );

    @Override
    public @NonNull AdvisedResponse aroundCall(@NonNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 检查请求中是否包含违禁词
        String userText = advisedRequest.userText();
        if (containsBannedWords(userText)) {
            log.warn("请求中包含违禁词：{}", userText);
            // 返回错误信息，阻止请求继续处理
            return createErrorResponse("抱歉，您的消息包含不当内容，无法继续处理。请使用文明用语。");
        }
        // 如果没有违禁词，继续处理请求
        return chain.nextAroundCall(advisedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 检查请求中是否包含违禁词
        String userText = advisedRequest.userText();
        if (containsBannedWords(userText)) {
            log.warn("请求中包含违禁词：{}", userText);
            // 返回错误信息的流
            AdvisedResponse errorResponse = createErrorResponse("抱歉，您的消息包含不当内容，无法继续处理。请使用文明用语。");
            return Flux.just(errorResponse);
        }
        // 如果没有违禁词，继续处理请求
        return chain.nextAroundStream(advisedRequest);
    }

    @Override
    public @NonNull String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    // 检查消息中是否包含违禁词（忽略大小写）
    private boolean containsBannedWords(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase();
        for (String bannedWord : BANNED_WORDS) {
            if (lowerMessage.contains(bannedWord.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // 创建错误响应
    private AdvisedResponse createErrorResponse(String errorMessage) {
        // 创建AssistantMessage
        AssistantMessage assistantMessage = new AssistantMessage(errorMessage);

        // 创建Generation
        Generation generation = new Generation(assistantMessage);

        // 创建ChatResponse
        ChatResponse chatResponse = new ChatResponse(List.of(generation));

        // 返回AdvisedResponse
        return new AdvisedResponse(chatResponse, new HashMap<>());
    }
}
