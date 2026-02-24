package com.grass.grassaiagent.advisor;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 自定义Re2 Advisor
 * 提高大模型语言模型理解度
 * @date 2026/02/24 11:11
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        HashMap<String, Object> advisedRequestParams = new HashMap<>(advisedRequest.userParams());
        advisedRequestParams.put("re2_input_query", advisedRequest.userText());
        return AdvisedRequest.from(advisedRequest)
                .userText("""
                {re2_input_query}
                Read the question again: {re2_input_query}
                """)
                .userParams(advisedRequestParams)
                .build();
    }

    @Override
    public @NonNull AdvisedResponse aroundCall(@NonNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(this.before(advisedRequest));
    }

    @Override
    public @NonNull Flux<AdvisedResponse> aroundStream(@NonNull AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(this.before(advisedRequest));
    }

    @Override
    public @NonNull String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
