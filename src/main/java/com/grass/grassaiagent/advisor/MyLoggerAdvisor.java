package com.grass.grassaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 自定义日志 Advisor
 * 打印info级别日志，只输出单次用户提示词和AI回复的文本
 * @date 2026/02/24 10:40
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    @Override
    public @NonNull AdvisedResponse aroundCall(@NonNull AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        advisedResponse = this.after(advisedResponse);
        return advisedResponse;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        Flux<AdvisedResponse> advisedResponse = chain.nextAroundStream(advisedRequest);
        // Tips:MessageAggregator中不能修改advisedResponse，否则会报错，他是一个只读操作
        return new MessageAggregator().aggregateAdvisedResponse(advisedResponse, this::after);
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.info("AI Request Before: {}", advisedRequest.userText());
        return advisedRequest;
    }

    private AdvisedResponse after(AdvisedResponse advisedResponse) {
        log.info("AI Response After: {}", advisedResponse.response().getResult().getOutput().getText());
        return advisedResponse;
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
