package com.grass.grassaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 自动添加元信息
 * @date 2026/03/09 14:37
 */
@Component
class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(dashscopeChatModel)
                .keywordCount(5)
                .build();
        return enricher.apply(documents);
    }
}
