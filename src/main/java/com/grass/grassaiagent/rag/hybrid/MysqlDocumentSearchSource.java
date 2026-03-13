package com.grass.grassaiagent.rag.hybrid;

import com.grass.grassaiagent.domain.AiKnowledgeDoc;
import com.grass.grassaiagent.mapper.AiKnowledgeDocMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import java.util.*;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 基于 MySQL 关键词检索的文档搜索源
 * 利用 MySQL LIKE 模糊匹配实现关键词检索，适用于：
 * - 向量数据库无法覆盖的结构化知识
 * - 向量检索无结果时的降级兜底
 * - 与语义检索结果合并以提升召回率
 *
 * 中文检索策略：先用原文整体匹配，不足时自动拆分为2字短语逐一补充。
 * @date 2026/03/10 10:20
 */
@Slf4j
public class MysqlDocumentSearchSource implements DocumentSearchSource {

    private final AiKnowledgeDocMapper mapper;

    public MysqlDocumentSearchSource(AiKnowledgeDocMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    public List<Document> search(String query, int topK) {
        try {
            Set<Long> seenIds = new LinkedHashSet<>();
            List<AiKnowledgeDoc> results = new ArrayList<>();

            // 第一轮：完整查询文本匹配
            collectResults(query, topK, seenIds, results);

            // 第二轮：拆分为2字短语逐一补充（适配中文无分隔符的特点）
            if (results.size() < topK && query.length() >= 2) {
                for (int i = 0; i <= query.length() - 2 && results.size() < topK; i++) {
                    String segment = query.substring(i, i + 2);
                    collectResults(segment, topK - results.size(), seenIds, results);
                }
            }

            log.debug("[{}] 检索到 {} 篇文档 (query={})", getName(), results.size(), query);
            return results.stream().limit(topK).map(this::toDocument).toList();
        } catch (Exception e) {
            log.warn("[{}] 检索失败: {}", getName(), e.getMessage());
            return List.of();
        }
    }

    private void collectResults(String keyword, int limit, Set<Long> seenIds, List<AiKnowledgeDoc> results) {
        List<AiKnowledgeDoc> found = mapper.searchByKeyword(keyword, limit);
        for (AiKnowledgeDoc doc : found) {
            if (seenIds.add(doc.getId())) {
                results.add(doc);
            }
        }
    }

    private Document toDocument(AiKnowledgeDoc doc) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "mysql");
        metadata.put("title", doc.getTitle());
        metadata.put("category", doc.getCategory());
        metadata.put("doc_id", doc.getId());
        String text = "【" + doc.getTitle() + "】\n" + doc.getContent();
        return new Document(text, metadata);
    }
}
