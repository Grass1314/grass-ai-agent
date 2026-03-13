package com.grass.grassaiagent.rag.hybrid;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 文档检索源抽象接口
 * 统一不同数据存储（VectorStore、MySQL、Redis、Elasticsearch 等）的检索行为，
 * 使 HybridDocumentRetriever 能以相同方式调度所有数据源。
 * @date 2026/03/10 10:10
 */
public interface DocumentSearchSource {

    /**
     * 数据源名称，用于日志和调试
     */
    String getName();

    /**
     * 从该数据源中检索与查询相关的文档
     *
     * @param query 用户查询文本
     * @param topK  最多返回的文档数量
     * @return 检索到的文档列表，已转为 Spring AI Document 格式
     */
    List<Document> search(String query, int topK);
}
