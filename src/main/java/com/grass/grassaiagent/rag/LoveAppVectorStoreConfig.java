package com.grass.grassaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
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
    private static final File VECTOR_STORE_CACHE_FILE = new File("tmp/vector-store/love-app-simple-vector-store.json");

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    SimpleVectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        if (VECTOR_STORE_CACHE_FILE.exists()) {
            vectorStore.load(VECTOR_STORE_CACHE_FILE);
            log.info("LoveApp 本地向量库缓存加载完成: {}", VECTOR_STORE_CACHE_FILE.getPath());
        }
        return vectorStore;
    }

    @Bean
    public CommandLineRunner loadDocumentsIntoLoveAppVectorStore(SimpleVectorStore loveAppVectorStore,
                                                                 LoveAppDocumentLoader loveAppDocumentLoader) {
        return args -> {
            try {
                if (VECTOR_STORE_CACHE_FILE.exists()) {
                    log.info("LoveApp 本地向量库已有缓存，跳过文档 Embedding 加载");
                    return;
                }
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
                saveVectorStoreCache(loveAppVectorStore);
                log.info("LoveApp 本地向量库文档加载完成，共 {} 条", documents.size());
            } catch (Exception e) {
                log.warn("LoveApp 本地向量库文档加载失败（不影响启动）: {}", e.getMessage());
            }
        };
    }

    private void saveVectorStoreCache(SimpleVectorStore loveAppVectorStore) {
        File parentDir = VECTOR_STORE_CACHE_FILE.getParentFile();
        if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
            log.warn("LoveApp 本地向量库缓存目录创建失败: {}", parentDir.getPath());
            return;
        }
        loveAppVectorStore.save(VECTOR_STORE_CACHE_FILE);
        log.info("LoveApp 本地向量库缓存保存完成: {}", VECTOR_STORE_CACHE_FILE.getPath());
    }
}
