package com.grass.grassaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 本地初始化向量数据库配置
 * @date 2026/02/26 09:18
 */
@Slf4j
@Configuration
public class LoveAppVectorStoreConfig {

    private static final int EMBEDDING_BATCH_SIZE = 10;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        return SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
    }

    @Bean
    public CommandLineRunner loadDocumentsIntoLoveAppVectorStore(VectorStore loveAppVectorStore,
                                                                 LoveAppDocumentLoader loveAppDocumentLoader) {
        return args -> {
            try {
                List<Document> documents = loveAppDocumentLoader.loadMarkdownDocuments();
                if (documents.isEmpty()) {
                    return;
                }
                /*for (int i = 0; i < documents.size(); i += EMBEDDING_BATCH_SIZE) {
                    int end = Math.min(i + EMBEDDING_BATCH_SIZE, documents.size());
                    loveAppVectorStore.add(documents.subList(i, end));
                }*/
                // 自定义分词器
//                loveAppVectorStore.add(myTokenTextSplitter.splitCustomized(documents));
                // 自定义元信息
                loveAppVectorStore.add(myKeywordEnricher.enrichDocuments(documents));
                log.info("LoveApp 本地向量库文档加载完成，共 {} 条", documents.size());
            } catch (Exception e) {
                log.warn("LoveApp 本地向量库文档加载失败（不影响启动）: {}", e.getMessage());
            }
        };
    }
}
