package com.grass.grassaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

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
}
