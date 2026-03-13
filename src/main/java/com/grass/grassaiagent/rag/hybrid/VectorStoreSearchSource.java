package com.grass.grassaiagent.rag.hybrid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Objects;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 基于向量数据库的检索源
 * 封装 Spring AI VectorStore，按语义相似度检索文档。
 * @date 2026/03/10 10:15
 */
@Slf4j
public class VectorStoreSearchSource implements DocumentSearchSource {

    private final VectorStore vectorStore;
    private final double similarityThreshold;

    public VectorStoreSearchSource(VectorStore vectorStore, double similarityThreshold) {
        this.vectorStore = Objects.requireNonNull(vectorStore);
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public String getName() {
        return "VectorStore";
    }

    @Override
    public List<Document> search(String query, int topK) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();
            List<Document> results = vectorStore.similaritySearch(request);
            log.debug("[{}] 检索到 {} 篇文档", getName(), results.size());
            return results;
        } catch (Exception e) {
            log.warn("[{}] 检索失败: {}", getName(), e.getMessage());
            return List.of();
        }
    }
}
