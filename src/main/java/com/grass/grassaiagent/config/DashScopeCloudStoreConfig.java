package com.grass.grassaiagent.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @description: Alibaba Cloud DashScope Vector Store 配置
 * @author Mr.Liuxq
 * @date 2025/07/21 15:47
 * @version 1.0
 */
@Component
public class DashScopeCloudStoreConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Bean
    public DashScopeCloudStore myDashScopeCloudStore() {
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashscopeApiKey)
                .build();
        return new DashScopeCloudStore(
                dashScopeApi, new DashScopeStoreOptions("恋爱大师"));
    }
}
