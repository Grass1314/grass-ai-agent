package com.grass.grassaiagent.advisor;

import com.grass.grassaiagent.mapper.AiKnowledgeDocMapper;
import com.grass.grassaiagent.rag.hybrid.HybridDocumentRetriever;
import com.grass.grassaiagent.rag.hybrid.MysqlDocumentSearchSource;
import com.grass.grassaiagent.rag.hybrid.VectorStoreSearchSource;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: HybridRagAdvisor 混合检索集成测试
 * 验证 MERGE 合并策略与 FALLBACK 降级策略在真实数据源上的工作效果
 * @date 2026/03/10 10:50
 */
@Slf4j
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HybridRagAdvisorTest {

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private AiKnowledgeDocMapper aiKnowledgeDocMapper;

    @Resource
    private DataSource dataSource;

    @BeforeAll
    void initMysqlKnowledgeTable() {
        log.info("初始化 MySQL 知识文档表...");
        var jdbc = new org.springframework.jdbc.core.JdbcTemplate(dataSource);
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS ai_knowledge_doc (
                id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                title      VARCHAR(255)  NOT NULL,
                content    TEXT          NOT NULL,
                category   VARCHAR(100)  DEFAULT NULL,
                keywords   VARCHAR(500)  DEFAULT NULL,
                create_time DATETIME     DEFAULT CURRENT_TIMESTAMP,
                update_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_category (category)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
            """);
        jdbc.execute("""
            INSERT IGNORE INTO ai_knowledge_doc (id, title, content, category, keywords) VALUES
            (1, '初次约会建议',
             '第一次约会建议选择安静舒适的场所，比如咖啡厅或安静的餐厅。约会前了解对方的兴趣爱好，准备一些共同话题。注意倾听对方说话，保持真诚的微笑和眼神交流。',
             '单身', '约会,初次见面,恋爱技巧,社交'),
            (2, '如何拓展社交圈认识新朋友',
             '单身人士拓展社交圈的方式有很多：参加兴趣社团或俱乐部；报名线下课程；参加行业交流活动或志愿者活动；通过朋友介绍认识朋友的朋友。关键是走出舒适区，主动与人交流。',
             '单身', '社交圈,交友,单身,兴趣,活动'),
            (3, '婚后财务管理与家庭和谐',
             '婚后财务管理是家庭和谐的重要基石：建议建立共同账户加个人账户模式，每月进行一次家庭财务会议，设定短期和长期的共同财务目标，避免隐性消费和财务隐瞒。',
             '已婚', '财务,理财,婚姻,家庭,金钱'),
            (4, '处理恋爱中的争吵与矛盾',
             '情侣吵架是正常的，关键在于如何处理：首先冷静下来给彼此空间，吵架时就事论事不翻旧账，尝试站在对方角度理解问题，争吵后主动寻求和解。',
             '恋爱', '吵架,争吵,矛盾,冷静,和解'),
            (5, '婆媳关系处理技巧',
             '处理婆媳关系的核心原则：丈夫是关键桥梁，设立清晰的家庭边界，避免在背后抱怨有问题当面温和沟通，寻找共同话题建立独立的关系。',
             '已婚', '婆媳,家庭关系,亲属,边界,沟通')
            """);
        log.info("MySQL 知识文档表初始化完成");
    }

    @Test
    @DisplayName("MERGE 策略 - 同时检索 VectorStore 和 MySQL，合并结果")
    void testMergeStrategy() {
        var vectorSource = new VectorStoreSearchSource(loveAppVectorStore, 0.3);
        var mysqlSource = new MysqlDocumentSearchSource(aiKnowledgeDocMapper);

        HybridDocumentRetriever retriever = HybridDocumentRetriever.builder()
                .addSource(vectorSource)
                .addSource(mysqlSource)
                .strategy(HybridDocumentRetriever.Strategy.MERGE)
                .build();

        HybridRagAdvisor advisor = HybridRagAdvisor.builder(retriever)
                .topK(5)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一位恋爱心理专家，请基于提供的多源参考资料回答用户问题。")
                .defaultAdvisors(advisor)
                .build();

        String answer = chatClient.prompt()
                .user("已婚后感觉关系不太亲密，婆媳关系也有些紧张，该怎么办？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("【MERGE 策略回答】\n{}", answer);
    }

    @Test
    @DisplayName("FALLBACK 策略 - VectorStore 优先，结果不足时降级到 MySQL")
    void testFallbackStrategy() {
        var vectorSource = new VectorStoreSearchSource(loveAppVectorStore, 0.3);
        var mysqlSource = new MysqlDocumentSearchSource(aiKnowledgeDocMapper);

        HybridDocumentRetriever retriever = HybridDocumentRetriever.builder()
                .addSource(vectorSource)
                .addSource(mysqlSource)
                .strategy(HybridDocumentRetriever.Strategy.FALLBACK)
                .fallbackMinResults(3)
                .build();

        HybridRagAdvisor advisor = HybridRagAdvisor.builder(retriever)
                .topK(5)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一位恋爱心理专家。")
                .defaultAdvisors(advisor)
                .build();

        String answer = chatClient.prompt()
                .user("婚后财务管理有什么好建议吗？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("【FALLBACK 策略回答】\n{}", answer);
    }

    @Test
    @DisplayName("纯 MySQL 检索 - 验证 MySQL 数据源独立工作")
    void testMysqlOnlyRetrieval() {
        var mysqlSource = new MysqlDocumentSearchSource(aiKnowledgeDocMapper);

        List<Document> results = mysqlSource.search("约会", 3);

        Assertions.assertFalse(results.isEmpty(), "MySQL 应检索到约会相关文档");
        log.info("MySQL 独立检索到 {} 篇文档:", results.size());
        results.forEach(doc -> log.info("  - [{}] {}", doc.getMetadata().get("title"),
                doc.getText().substring(0, Math.min(60, doc.getText().length())) + "..."));
    }

    @Test
    @DisplayName("MERGE 策略搭配多个 Advisor 链式调用")
    void testWithAdvisorChain() {
        var vectorSource = new VectorStoreSearchSource(loveAppVectorStore, 0.3);
        var mysqlSource = new MysqlDocumentSearchSource(aiKnowledgeDocMapper);

        HybridDocumentRetriever retriever = HybridDocumentRetriever.builder()
                .addSource(vectorSource)
                .addSource(mysqlSource)
                .strategy(HybridDocumentRetriever.Strategy.MERGE)
                .build();

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem("你是一位恋爱心理专家。")
                .defaultAdvisors(
                        new BannedWordsAdvisor(),
                        new MyLoggerAdvisor(),
                        HybridRagAdvisor.builder(retriever).topK(5).build()
                )
                .build();

        String answer = chatClient.prompt()
                .user("恋爱中总是吵架，沟通不顺畅怎么办？")
                .call()
                .content();

        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("【Advisor 链 + MERGE 回答】\n{}", answer);
    }
}
