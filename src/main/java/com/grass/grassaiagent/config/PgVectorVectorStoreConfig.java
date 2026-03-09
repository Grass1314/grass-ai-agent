package com.grass.grassaiagent.config;

import com.grass.grassaiagent.rag.LoveAppDocumentLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * @description: PgVectorVector向量库 配置
 * 因项目使用 dynamic-datasource，默认 JdbcTemplate 指向 MySQL，
 * 此处手动创建专用于 PostgreSQL 的 DataSource / JdbcTemplate 供 PgVector 使用。
 * @author Mr.Liuxq
 * @date 2025/7/22 16:54
 * @version 1.0
 */
@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    private static final int EMBEDDING_BATCH_SIZE = 10;

    @Value("${spring.datasource.dynamic.datasource.postgresql.url}")
    private String pgUrl;

    @Value("${spring.datasource.dynamic.datasource.postgresql.username}")
    private String pgUsername;

    @Value("${spring.datasource.dynamic.datasource.postgresql.password}")
    private String pgPassword;

    @Value("${spring.datasource.dynamic.datasource.postgresql.driver-class-name}")
    private String pgDriverClassName;

    @Bean
    public DataSource pgDataSource() {
        return DataSourceBuilder.create()
                .url(pgUrl)
                .username(pgUsername)
                .password(pgPassword)
                .driverClassName(pgDriverClassName)
                .build();
    }

    @Bean
    public JdbcTemplate pgJdbcTemplate(DataSource pgDataSource) {
        return new JdbcTemplate(pgDataSource);
    }

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate pgJdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(pgJdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("ai_pg_vector_store")
                .maxDocumentBatchSize(EMBEDDING_BATCH_SIZE)
                .build();
    }

    /**
     * 应用启动完成后，分批加载文档到 PgVector。
     * DashScope Embedding API 每批最多 10 条。
     */
    @Bean
    public CommandLineRunner loadDocumentsIntoPgVector(VectorStore pgVectorVectorStore,
                                                       LoveAppDocumentLoader loveAppDocumentLoader) {
        return args -> {
            try {
                List<Document> documents = loveAppDocumentLoader.loadMarkdownDocuments();
                if (documents.isEmpty()) {
                    log.info("PgVector: 无文档需要加载");
                    return;
                }
                for (int i = 0; i < documents.size(); i += EMBEDDING_BATCH_SIZE) {
                    int end = Math.min(i + EMBEDDING_BATCH_SIZE, documents.size());
                    pgVectorVectorStore.add(documents.subList(i, end));
                }
                log.info("PgVector 文档加载完成，共 {} 条", documents.size());
            } catch (Exception e) {
                log.warn("PgVector 文档加载失败（不影响启动）: {}", e.getMessage());
            }
        };
    }
}
