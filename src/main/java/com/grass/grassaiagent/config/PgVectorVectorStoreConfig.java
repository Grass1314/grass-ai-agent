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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

/**
 * @description: PgVectorVector向量库 配置
 * 因项目使用 dynamic-datasource，默认 DataSource / JdbcTemplate 指向 MySQL，
 * 此处在方法内部创建专用于 PostgreSQL 的连接，不注册为 Spring Bean，
 * 避免干扰 MyBatis-Plus 的数据源路由。
 * @author Mr.Liuxq
 * @date 2025/7/22 16:54
 * @version 1.0
 */
@Slf4j
@Configuration
public class PgVectorVectorStoreConfig {

    private static final int EMBEDDING_BATCH_SIZE = 10;
    private static final String VECTOR_TABLE_NAME = "ai_pg_vector_store";

    @Value("${spring.datasource.dynamic.datasource.postgresql.url}")
    private String pgUrl;

    @Value("${spring.datasource.dynamic.datasource.postgresql.username}")
    private String pgUsername;

    @Value("${spring.datasource.dynamic.datasource.postgresql.password}")
    private String pgPassword;

    @Value("${spring.datasource.dynamic.datasource.postgresql.driver-class-name}")
    private String pgDriverClassName;

    private JdbcTemplate createPgJdbcTemplate() {
        return new JdbcTemplate(DataSourceBuilder.create()
                .url(pgUrl)
                .username(pgUsername)
                .password(pgPassword)
                .driverClassName(pgDriverClassName)
                .build());
    }

    @Bean
    public VectorStore pgVectorVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        return PgVectorStore.builder(createPgJdbcTemplate(), dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(COSINE_DISTANCE)
                .indexType(HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName(VECTOR_TABLE_NAME)
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

                JdbcTemplate pgJdbcTemplate = createPgJdbcTemplate();
                List<Document> missingDocuments = findMissingDocuments(pgJdbcTemplate, documents);
                if (missingDocuments.isEmpty()) {
                    log.info("PgVector: 知识库文档已存在，跳过 Embedding 加载");
                    return;
                }

                for (int i = 0; i < missingDocuments.size(); i += EMBEDDING_BATCH_SIZE) {
                    int end = Math.min(i + EMBEDDING_BATCH_SIZE, missingDocuments.size());
                    pgVectorVectorStore.add(missingDocuments.subList(i, end));
                }
                log.info("PgVector 缺失文档加载完成，共 {} 条", missingDocuments.size());
            } catch (Exception e) {
                log.warn("PgVector 文档加载失败（不影响启动）: {}", e.getMessage());
            }
        };
    }

    private List<Document> findMissingDocuments(JdbcTemplate pgJdbcTemplate, List<Document> documents) {
        Map<String, List<Document>> documentsByFilename = documents.stream()
                .collect(Collectors.groupingBy(this::getFilename, LinkedHashMap::new, Collectors.toList()));

        List<Document> missingDocuments = new ArrayList<>();
        for (Map.Entry<String, List<Document>> entry : documentsByFilename.entrySet()) {
            String filename = entry.getKey();
            if (filename.isBlank()) {
                missingDocuments.addAll(entry.getValue());
                continue;
            }
            if (hasDocumentsByFilename(pgJdbcTemplate, filename)) {
                log.info("PgVector: 知识库文件已存在，跳过加载: {}", filename);
                continue;
            }
            missingDocuments.addAll(entry.getValue());
        }
        return missingDocuments;
    }

    private boolean hasDocumentsByFilename(JdbcTemplate pgJdbcTemplate, String filename) {
        Integer count = pgJdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM public.%s
                WHERE metadata ->> 'filename' = ?
                """.formatted(VECTOR_TABLE_NAME), Integer.class, filename);
        return count != null && count > 0;
    }

    private String getFilename(Document document) {
        Object filename = document.getMetadata().get("filename");
        return filename == null ? "" : filename.toString();
    }
}
