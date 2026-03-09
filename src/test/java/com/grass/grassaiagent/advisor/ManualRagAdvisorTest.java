package com.grass.grassaiagent.advisor;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: ManualRagAdvisor 集成测试
 * 验证自主实现的 RAG Advisor 能正确检索文档并增强提示词
 * @date 2026/03/09 17:00
 */
@Slf4j
@SpringBootTest
class ManualRagAdvisorTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private VectorStore loveAppVectorStore;

    @Test
    @DisplayName("使用 ManualRagAdvisor 检索恋爱知识库并回答问题")
    void chatWithManualRag() {
        ManualRagAdvisor ragAdvisor = ManualRagAdvisor.builder(loveAppVectorStore)
                .topK(3)
                .similarityThreshold(0.3)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一位恋爱心理专家，请基于提供的参考资料回答用户问题。")
                .defaultAdvisors(ragAdvisor)
                .build();

        String answer = chatClient.prompt()
                .user("已婚后感觉关系不太亲密，该怎么改善？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("ManualRAG 回答:\n{}", answer);
    }

    @Test
    @DisplayName("ManualRagAdvisor 搭配自定义提示词模板")
    void chatWithCustomPromptTemplate() {
        String customTemplate = """
                你是专业的情感顾问。请参考以下资料，给出有温度、有操作性的建议。
                
                [参考资料]
                %s
                [资料结束]
                
                来访者的问题: %s
                
                请用温暖的语气、分点回答：""";

        ManualRagAdvisor ragAdvisor = ManualRagAdvisor.builder(loveAppVectorStore)
                .topK(5)
                .similarityThreshold(0.3)
                .promptTemplate(customTemplate)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(ragAdvisor)
                .build();

        String answer = chatClient.prompt()
                .user("单身好久了，怎么拓展社交圈认识合适的人？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("自定义模板 RAG 回答:\n{}", answer);
    }

    @Test
    @DisplayName("ManualRagAdvisor 与其他 Advisor 组合使用")
    void chainWithOtherAdvisors() {
        ManualRagAdvisor ragAdvisor = ManualRagAdvisor.builder(loveAppVectorStore)
                .topK(3)
                .similarityThreshold(0.3)
                .order(100)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一位恋爱心理专家。")
                .defaultAdvisors(
                        new BannedWordsAdvisor(),
                        new MyLoggerAdvisor(),
                        ragAdvisor
                )
                .build();

        String answer = chatClient.prompt()
                .user("恋爱中沟通出现问题，总是吵架怎么办？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("多 Advisor 链 RAG 回答:\n{}", answer);
    }
}
