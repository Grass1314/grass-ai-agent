package com.grass.grassaiagent.app;

import com.grass.grassaiagent.rag.hybrid.HybridDocumentRetriever;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@Slf4j
@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员小草";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第二轮
        message = "我想让另一半（婷婷）更爱我";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是程序员小草，我想让另一半（婷婷）更爱我，但我不知道怎么做";
//        String message = "你好，我是程序员小草，我想攻击，我想让另一半（婷婷）更爱我，但我不知道怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void testChatWithReport2() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是程序员小草";
        LoveApp.LoveReport loveReport = loveApp.doChatReport(message, chatId);
        Assertions.assertNotNull(loveReport);
        // 第二轮
        message = "我想让另一半（婷婷）更爱我";
        loveReport = loveApp.doChatReport(message, chatId);
        Assertions.assertNotNull(loveReport);
        // 第三轮
        message = "我的另一半叫什么来着？刚跟你说过，帮我回忆一下";
        loveReport = loveApp.doChatReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void testChatWithReport3() {
        String chatId = UUID.randomUUID().toString();
        String message = "我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    @DisplayName("混合检索 RAG - MERGE 合并策略（VectorStore + MySQL 同时检索）")
    void testChatWithHybridRagMerge() {
        String chatId = UUID.randomUUID().toString();
        String message = "已婚后感觉关系不太亲密，婆媳关系也有些紧张，该怎么办？";
        String answer = loveApp.doChatWithHybridRag(message, chatId, HybridDocumentRetriever.Strategy.MERGE);
        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("【MERGE 策略回答】\n{}", answer);
    }

    @Test
    @DisplayName("混合检索 RAG - FALLBACK 降级策略（VectorStore 优先，不足时降级到 MySQL）")
    void testChatWithHybridRagFallback() {
        String chatId = UUID.randomUUID().toString();
        String message = "婚后财务管理有什么好建议吗？";
        String answer = loveApp.doChatWithHybridRag(message, chatId, HybridDocumentRetriever.Strategy.FALLBACK);
        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("【FALLBACK 策略回答】\n{}", answer);
    }

    @Test
    void testChatWithHybridToolRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我想要使用工具知道婚后关系不太亲密解决方式，请给出建议";
        String answer = loveApp.doChatWithHybridToolRag(message, chatId, HybridDocumentRetriever.Strategy.MERGE);
        Assertions.assertNotNull(answer);
        Assertions.assertFalse(answer.isBlank());
        log.info("工具调用\n{}", answer);
    }
}
