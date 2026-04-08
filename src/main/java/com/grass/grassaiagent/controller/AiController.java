package com.grass.grassaiagent.controller;

import com.grass.grassaiagent.agent.GrassManus;
import com.grass.grassaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

/**
 * @description: AI 控制器
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/04/07 16:29
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 聊天（同步）
     * @param message 消息体
     * @param chatId 会话ID
     * @return 聊天结果
     */
    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * 聊天（SSE流式输出）
     * @param message 消息
     * @param chatId 会话ID
     * @return  内容
     */
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 聊天（SSE流式输出Flux<ServerSentEvent<String>>格式）
     * @param message 输入
     * @param chatId 会话ID
     * @return 内容
     */
    @GetMapping("/love_app/chat/server_sent_event/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEventSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId).map(data -> ServerSentEvent.builder(data).build());
    }

    /**
     * 聊天（SSE流式输出SseEmitter格式）
     * @param message 输入
     * @param chatId 会话ID
     * @return 内容
     */
    @GetMapping("/love_app/chat/sse/emitter")
    public SseEmitter doChatWithLoveAppSseEmitter(String message, String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId).subscribe(chunk -> {
            try {
                emitter.send(chunk);
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, emitter::completeWithError, emitter::complete);
        return emitter;
    }

    /**
     * 聊天（流式调用 Manus 超级智能体）
     * @param message 输入
     * @return 内容
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        GrassManus grassManus = new GrassManus(allTools, dashscopeChatModel);
        return grassManus.runStream(message);
    }

}
