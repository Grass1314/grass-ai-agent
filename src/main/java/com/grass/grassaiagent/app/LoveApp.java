package com.grass.grassaiagent.app;

import com.grass.grassaiagent.advisor.BannedWordsAdvisor;
import com.grass.grassaiagent.advisor.HybridRagAdvisor;
import com.grass.grassaiagent.advisor.MyLoggerAdvisor;
import com.grass.grassaiagent.chatmemory.MysqlSaveChatMemory;
import com.grass.grassaiagent.mapper.AiKnowledgeDocMapper;
import com.grass.grassaiagent.rag.QueryRewriter;
import com.grass.grassaiagent.rag.hybrid.HybridDocumentRetriever;
import com.grass.grassaiagent.rag.hybrid.MysqlDocumentSearchSource;
import com.grass.grassaiagent.rag.hybrid.VectorStoreSearchSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Liuxq
 * @version 1.0
 * @description: 恋爱APP
 * @date 2026/02/17 22:43
 */
@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    @jakarta.annotation.Resource
    private VectorStore loveAppVectorStore;

    /** 从模板渲染后的默认系统提示词（用于 doChatReport 等未走模板的场景） */
    private final String systemPromptFromTemplate;

    @jakarta.annotation.Resource
    private QueryRewriter queryRewriter;

    @jakarta.annotation.Resource
    private AiKnowledgeDocMapper aiKnowledgeDocMapper;

    @jakarta.annotation.Resource
    private ToolCallback[] toolCallbacks;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /*public LoveApp(ChatModel dashscopeChatModel) {
        // 基于内存的对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }*/

    /*public LoveApp(ChatModel dashscopeChatModel) {
        // 基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new BannedWordsAdvisor(),
                        new MyLoggerAdvisor())
                .build();
    }*/

    /*public LoveApp(ChatModel dashscopeChatModel, MysqlSaveChatMemory mysqlSaveChatMemory) {
        // 基于mysql的对话记忆
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(mysqlSaveChatMemory).build(),
                        new BannedWordsAdvisor(),
                        new MyLoggerAdvisor())
                .build();
    }*/

    public LoveApp(
            ChatModel dashscopeChatModel,
            MysqlSaveChatMemory mysqlSaveChatMemory,
            @Value("classpath:/prompts/system-message.st") Resource systemPromptResource) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPromptResource);
        this.systemPromptFromTemplate = systemPromptTemplate.createMessage(Map.of("name", "小爱")).getText();
        log.debug("系统提示词模板已加载: {}", systemPromptFromTemplate);
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(systemPromptFromTemplate)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(mysqlSaveChatMemory).build(),
                        new BannedWordsAdvisor(),
                        new MyLoggerAdvisor())
                .build();
    }

    /**
     * 对话
     *
     * @param message 消息
     * @param chatId 会话id
     * @return  内容
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * 对话并生成恋爱报告
     *
     * @param message 输入
     * @param chatId 会话id
     * @return 恋爱报告
     */
    public LoveReport doChatReport(String message, String chatId) {
        try {
            LoveReport loveReport = chatClient
                    .prompt()
                    .system(systemPromptFromTemplate + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .entity(LoveReport.class);
            log.info("loveReport: {}", loveReport);
            return loveReport;
        } catch (Exception e) {
            log.warn("生成恋爱报告时发生错误: {}", e.getMessage());
            return new LoveReport("内容审核提醒", Arrays.asList("您的消息包含不当内容，无法生成恋爱报告", "请使用文明用语重新输入"));
        }
    }

    /**
     * 对话并使用 RAG
     *
     * @param message 输入
     * @param chatId 会话id
     * @return 内容
     */
    public String doChatWithRag(String message, String chatId) {
        // 查询重写
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 混合检索 RAG 对话（VectorStore + MySQL 多源检索）
     *
     * @param message  输入
     * @param chatId   会话id
     * @param strategy 检索策略：MERGE 合并多源结果 / FALLBACK 主源不足时降级
     * @return 内容
     */
    public String doChatWithHybridRag(String message, String chatId, HybridDocumentRetriever.Strategy strategy) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        HybridDocumentRetriever retriever = HybridDocumentRetriever.builder()
                .addSource(new VectorStoreSearchSource(loveAppVectorStore, 0.5))
                .addSource(new MysqlDocumentSearchSource(aiKnowledgeDocMapper))
                .strategy(strategy)
                .fallbackMinResults(3)
                .build();

        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(HybridRagAdvisor.builder(retriever).topK(5).build())
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 混合检索 + 工具 RAG 对话（VectorStore + MySQL 多源检索）
     *
     * @param message  输入
     * @param chatId   会话id
     * @param strategy 检索策略：MERGE 合并多源结果 / FALLBACK 主源不足时降级
     * @return 内容
     */
    public String doChatWithHybridToolRag(String message, String chatId, HybridDocumentRetriever.Strategy strategy) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        HybridDocumentRetriever retriever = HybridDocumentRetriever.builder()
                .addSource(new VectorStoreSearchSource(loveAppVectorStore, 0.5))
                .addSource(new MysqlDocumentSearchSource(aiKnowledgeDocMapper))
                .strategy(strategy)
                .fallbackMinResults(3)
                .build();

        ChatResponse chatResponse = chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(HybridRagAdvisor.builder(retriever).topK(5).build())
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbacks)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
