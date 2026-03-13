package com.grass.grassaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 查询重写
 * @date 2026/03/09 14:50
 */
@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    public QueryRewriter(ChatModel dashscopeChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
        // 创建查询重写
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    /**
     * 执行查询重写
     * @param prompt 转换前的查询
     * @return 转换后的查询
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        Query transform = queryTransformer.transform(query);
        return transform.text();
    }
}
