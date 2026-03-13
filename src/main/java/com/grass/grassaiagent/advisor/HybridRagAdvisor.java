package com.grass.grassaiagent.advisor;

import com.grass.grassaiagent.rag.hybrid.HybridDocumentRetriever;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 基于混合检索的 RAG Advisor
 * 核心思路：利用 HybridDocumentRetriever 从多个数据源（向量数据库 + MySQL 等）
 * 检索相关文档，拼装为上下文后注入到用户提示词中，再交由 LLM 生成回答。
 *
 * <p>与 ManualRagAdvisor 的区别：
 * ManualRagAdvisor 仅使用单一 VectorStore 检索；
 * HybridRagAdvisor 支持多数据源混合检索，可选 MERGE（合并）或 FALLBACK（降级）策略。</p>
 * @date 2026/03/10 10:40
 */
@Slf4j
public class HybridRagAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_PROMPT_TEMPLATE = """
            请基于以下参考资料回答用户问题。
            参考资料来自多个知识源，如果某条资料与问题无关请忽略。
            如果所有资料均无关，请根据你的知识回答并说明。
            
            ===== 参考资料（来自 %d 个数据源，共 %d 条） =====
            %s
            ===== 参考资料结束 =====
            
            用户问题: %s""";

    private final HybridDocumentRetriever retriever;
    private final int topK;
    private final int order;

    private HybridRagAdvisor(Builder builder) {
        this.retriever = Objects.requireNonNull(builder.retriever, "retriever 不能为空");
        this.topK = builder.topK;
        this.order = builder.order;
    }

    public static Builder builder(HybridDocumentRetriever retriever) {
        return new Builder(retriever);
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        return chain.nextCall(augmentRequest(request));
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        return chain.nextStream(augmentRequest(request));
    }

    private ChatClientRequest augmentRequest(ChatClientRequest request) {
        String userQuery = request.prompt().getUserMessage().getText();
        log.debug("HybridRAG - 原始查询: {}", userQuery);

        List<Document> documents = retriever.retrieve(userQuery, topK);

        String context = formatContext(documents);
        long sourceCount = documents.stream()
                .map(d -> d.getMetadata().getOrDefault("source", "unknown"))
                .distinct().count();

        String augmented = String.format(DEFAULT_PROMPT_TEMPLATE,
                sourceCount, documents.size(), context, userQuery);

        log.info("HybridRAG - 检索到 {} 篇文档，来自 {} 个数据源", documents.size(), sourceCount);

        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(augmented))
                .build();
    }

    private String formatContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "（无相关参考资料）";
        }
        return documents.stream()
                .map(doc -> {
                    String source = String.valueOf(doc.getMetadata().getOrDefault("source", "unknown"));
                    String title = String.valueOf(doc.getMetadata().getOrDefault("title", ""));
                    String label = title.isBlank() ? source : source + " - " + title;
                    return "[" + label + "]\n" + doc.getText();
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    // ==================== Builder ====================

    public static class Builder {
        private final HybridDocumentRetriever retriever;
        private int topK = 5;
        private int order = 100;

        private Builder(HybridDocumentRetriever retriever) {
            this.retriever = retriever;
        }

        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public HybridRagAdvisor build() {
            return new HybridRagAdvisor(this);
        }
    }
}
