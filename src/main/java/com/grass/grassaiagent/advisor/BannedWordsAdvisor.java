package com.grass.grassaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 违禁词 Advisor
 * 参照 Spring AI 1.1 SafeGuardAdvisor 模式实现
 * @date 2026/02/24 14:49
 */
@Slf4j
public class BannedWordsAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_FAILURE_RESPONSE = "抱歉，您的消息包含不当内容，无法继续处理。请使用文明用语。";

    private static final List<String> BANNED_WORDS = Arrays.asList(
            "暴力", "色情", "赌博", "毒品", "诈骗", "恐怖主义",
            "违法", "犯罪", "攻击", "辱骂", "歧视", "仇恨言论"
    );

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        if (containsBannedWords(chatClientRequest.prompt().getContents())) {
            log.warn("请求中包含违禁词：{}", chatClientRequest.prompt().getContents());
            return createFailureResponse(chatClientRequest);
        }
        return callAdvisorChain.nextCall(chatClientRequest);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        if (containsBannedWords(chatClientRequest.prompt().getContents())) {
            log.warn("请求中包含违禁词：{}", chatClientRequest.prompt().getContents());
            return Flux.just(createFailureResponse(chatClientRequest));
        }
        return streamAdvisorChain.nextStream(chatClientRequest);
    }

    private boolean containsBannedWords(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        String lowerMessage = message.toLowerCase();
        return BANNED_WORDS.stream().anyMatch(word -> lowerMessage.contains(word.toLowerCase()));
    }

    private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
        return ChatClientResponse.builder()
                .chatResponse(ChatResponse.builder()
                        .generations(List.of(new Generation(new AssistantMessage(DEFAULT_FAILURE_RESPONSE))))
                        .build())
                .context(Map.copyOf(chatClientRequest.context()))
                .build();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
