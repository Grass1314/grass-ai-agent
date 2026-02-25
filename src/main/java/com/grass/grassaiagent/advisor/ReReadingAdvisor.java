package com.grass.grassaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;

import java.util.Map;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 自定义Re2 Advisor
 * 提高大模型语言模型理解度
 * 参考 Spring AI 1.1 官方 ReReadingAdvisor 写法，使用 BaseAdvisor 接口
 * @date 2026/02/24 11:11
 */
public class ReReadingAdvisor implements BaseAdvisor {

    private static final String DEFAULT_RE2_TEMPLATE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    private final String re2Template;

    private int order = 0;

    public ReReadingAdvisor() {
        this(DEFAULT_RE2_TEMPLATE);
    }

    public ReReadingAdvisor(String re2Template) {
        this.re2Template = re2Template;
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String augmentedUserText = PromptTemplate.builder()
                .template(this.re2Template)
                .variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
                .build()
                .render();

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public ReReadingAdvisor withOrder(int order) {
        this.order = order;
        return this;
    }
}
