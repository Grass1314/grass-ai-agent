package com.grass.grassaiagent.rag.hybrid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;

import java.util.*;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 混合文档检索器
 * 编排多个 DocumentSearchSource，支持两种检索策略：
 *
 * <ul>
 *   <li><b>MERGE（合并策略）</b>：同时查询所有数据源，合并去重后返回。
 *       适用于需要最大化召回率的场景。</li>
 *   <li><b>FALLBACK（降级策略）</b>：优先查询主数据源（第一个），
 *       结果不足时依次查询后续数据源补充。适用于节省成本、减少延迟的场景。</li>
 * </ul>
 *
 * @date 2026/03/10 10:30
 */
@Slf4j
public class HybridDocumentRetriever {

    /**
     * 混合检索策略枚举
     */
    public enum Strategy {
        /** 多源合并：同时检索所有数据源，合并去重后返回 */
        MERGE,
        /** 降级兜底：优先使用主数据源，结果不足时从备选源补充 */
        FALLBACK
    }

    private final List<DocumentSearchSource> sources;
    private final Strategy strategy;
    private final int fallbackMinResults;

    private HybridDocumentRetriever(Builder builder) {
        this.sources = List.copyOf(builder.sources);
        this.strategy = builder.strategy;
        this.fallbackMinResults = builder.fallbackMinResults;
        if (sources.isEmpty()) {
            throw new IllegalArgumentException("至少需要一个数据源");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 执行混合检索
     *
     * @param query 用户查询文本
     * @param topK  最多返回的文档数量
     * @return 合并后的检索结果
     */
    public List<Document> retrieve(String query, int topK) {
        return switch (strategy) {
            case MERGE -> mergeRetrieve(query, topK);
            case FALLBACK -> fallbackRetrieve(query, topK);
        };
    }

    /**
     * MERGE 策略：查询所有数据源，按"内容前100字"去重后返回
     */
    private List<Document> mergeRetrieve(String query, int topK) {
        Set<String> seen = new HashSet<>();
        List<Document> merged = new ArrayList<>();

        for (DocumentSearchSource source : sources) {
            log.info("[MERGE] 正在检索数据源: {}", source.getName());
            List<Document> docs = source.search(query, topK);
            for (Document doc : docs) {
                String fingerprint = contentFingerprint(doc);
                if (seen.add(fingerprint)) {
                    merged.add(doc);
                }
            }
        }

        List<Document> result = merged.size() > topK ? merged.subList(0, topK) : merged;
        log.info("[MERGE] 所有数据源合并后共 {} 篇文档（去重前 {}）",
                result.size(), merged.size());
        return result;
    }

    /**
     * FALLBACK 策略：依次查询数据源，直到结果数量 >= fallbackMinResults
     */
    private List<Document> fallbackRetrieve(String query, int topK) {
        Set<String> seen = new HashSet<>();
        List<Document> results = new ArrayList<>();

        for (int i = 0; i < sources.size(); i++) {
            DocumentSearchSource source = sources.get(i);
            boolean isPrimary = (i == 0);
            log.info("[FALLBACK] {}检索数据源: {}", isPrimary ? "主数据源 " : "降级 ", source.getName());

            List<Document> docs = source.search(query, topK);
            for (Document doc : docs) {
                String fingerprint = contentFingerprint(doc);
                if (seen.add(fingerprint)) {
                    results.add(doc);
                }
            }

            if (results.size() >= fallbackMinResults) {
                log.info("[FALLBACK] 已获取 {} 篇文档，满足最小要求({}), 无需继续降级",
                        results.size(), fallbackMinResults);
                break;
            }
            log.info("[FALLBACK] 当前仅获取 {} 篇文档，不足 {}，继续降级查询",
                    results.size(), fallbackMinResults);
        }

        return results.size() > topK ? results.subList(0, topK) : results;
    }

    /**
     * 以文档内容前100字符作为指纹，用于跨数据源去重
     */
    private String contentFingerprint(Document doc) {
        String text = doc.getText();
        if (text == null) return UUID.randomUUID().toString();
        return text.length() <= 100 ? text : text.substring(0, 100);
    }

    // ==================== Builder ====================

    public static class Builder {
        private final List<DocumentSearchSource> sources = new ArrayList<>();
        private Strategy strategy = Strategy.FALLBACK;
        private int fallbackMinResults = 3;

        /** 添加数据源（添加顺序即优先级，第一个为主数据源） */
        public Builder addSource(DocumentSearchSource source) {
            this.sources.add(source);
            return this;
        }

        /** 检索策略，默认 FALLBACK */
        public Builder strategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        /** FALLBACK 策略下，主数据源至少返回多少条结果才不继续降级，默认 3 */
        public Builder fallbackMinResults(int fallbackMinResults) {
            this.fallbackMinResults = fallbackMinResults;
            return this;
        }

        public HybridDocumentRetriever build() {
            return new HybridDocumentRetriever(this);
        }
    }
}
