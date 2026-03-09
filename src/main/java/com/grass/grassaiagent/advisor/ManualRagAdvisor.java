package com.grass.grassaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 手动实现的 RAG Advisor
 * 不借助 Spring AI 内置的 RetrievalAugmentationAdvisor / QuestionAnswerAdvisor，
 * 完全自主实现 "检索 → 拼装上下文 → 增强提示词" 的 RAG 流程。
 * <p>
 * 核心流程:
 * 1. 从用户消息中提取查询文本
 * 2. 调用 VectorStore.similaritySearch 检索相关文档
 * 3. 将检索结果格式化为上下文文本
 * 4. 将上下文注入到用户消息中，构成增强后的提示词
 * 5. 转发给后续 Advisor / LLM 处理
 * </p>
 * @date 2026/03/09 17:00
 */
@Slf4j
public class ManualRagAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_PROMPT_TEMPLATE = """
            请基于以下参考资料回答用户问题。如果参考资料中没有相关信息，请明确说明并根据你的知识尽力回答。
            
            ===== 参考资料 =====
            %s
            ===== 参考资料结束 =====
            
            用户问题: %s""";

    private static final String DEFAULT_EMPTY_CONTEXT_RESPONSE =
            "未检索到相关参考资料，以下回答基于通用知识。\n\n";

    private final VectorStore vectorStore;
    private final int topK;
    private final double similarityThreshold;
    private final String promptTemplate;
    private final int order;

    private ManualRagAdvisor(Builder builder) {
        this.vectorStore = Objects.requireNonNull(builder.vectorStore, "vectorStore 不能为空");
        this.topK = builder.topK;
        this.similarityThreshold = builder.similarityThreshold;
        this.promptTemplate = builder.promptTemplate;
        this.order = builder.order;
    }

    public static Builder builder(VectorStore vectorStore) {
        return new Builder(vectorStore);
    }

    // ==================== CallAdvisor ====================

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        ChatClientRequest augmented = augmentRequest(request);
        return chain.nextCall(augmented);
    }

    // ==================== StreamAdvisor ====================

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        ChatClientRequest augmented = augmentRequest(request);
        return chain.nextStream(augmented);
    }

    // ==================== RAG 核心逻辑 ====================

    private ChatClientRequest augmentRequest(ChatClientRequest request) {
        String userQuery = request.prompt().getUserMessage().getText();
        log.debug("ManualRAG - 原始查询: {}", userQuery);

        // Step 1: 向量检索
        List<Document> retrievedDocs = retrieveDocuments(userQuery);
        log.info("ManualRAG - 检索到 {} 篇相关文档", retrievedDocs.size());

        // Step 2: 格式化上下文
        String context = formatContext(retrievedDocs);

        // Step 3: 构建增强后的提示词
        String augmentedMessage = String.format(promptTemplate, context, userQuery);
        log.debug("ManualRAG - 增强后提示词长度: {} 字符", augmentedMessage.length());

        // Step 4: 替换用户消息
        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(augmentedMessage))
                .build();
    }

    private List<Document> retrieveDocuments(String query) {
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();
            return vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("ManualRAG - 向量检索失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String formatContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return DEFAULT_EMPTY_CONTEXT_RESPONSE;
        }
        return documents.stream()
                .map(doc -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[文档");
                    Object filename = doc.getMetadata().get("filename");
                    if (filename != null) {
                        sb.append(" - ").append(filename);
                    }
                    sb.append("]\n");
                    sb.append(doc.getText());
                    return sb.toString();
                })
                .collect(Collectors.joining("\n\n"));
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
        private final VectorStore vectorStore;
        private int topK = 5;
        private double similarityThreshold = 0.5;
        private String promptTemplate = DEFAULT_PROMPT_TEMPLATE;
        private int order = 100;

        private Builder(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }

        /** 返回的最相关文档数量，默认 5 */
        public Builder topK(int topK) {
            this.topK = topK;
            return this;
        }

        /** 相似度阈值，低于此值的文档不返回，默认 0.5 */
        public Builder similarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        /** 自定义 RAG 提示词模板，必须包含两个 %s 占位符（第一个是上下文，第二个是用户问题） */
        public Builder promptTemplate(String promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        /** Advisor 排序，数值越小越先执行，默认 100 */
        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public ManualRagAdvisor build() {
            return new ManualRagAdvisor(this);
        }
    }
}
