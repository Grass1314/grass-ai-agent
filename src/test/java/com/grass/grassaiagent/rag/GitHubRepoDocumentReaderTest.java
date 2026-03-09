package com.grass.grassaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: GitHubRepoDocumentReader 测试
 * 读取 GitHub 公开仓库信息，验证文档读取器功能
 * @date 2026/03/09 16:00
 */
@Slf4j
class GitHubRepoDocumentReaderTest {

    @Test
    @DisplayName("读取 GitHub 公开仓库（octocat/Hello-World）基本信息")
    void readPublicRepo() {
        GitHubRepoDocumentReader reader = GitHubRepoDocumentReader
                .builder("octocat", "Hello-World")
                .timeoutMs(20_000)
                .build();

        List<Document> documents = reader.get();

        Assertions.assertNotNull(documents);
        Assertions.assertFalse(documents.isEmpty(), "至少应读取到一个文档");

        for (Document doc : documents) {
            log.info("=== 文档类型: {} ===", doc.getMetadata().get("type"));
            log.info("元信息: {}", doc.getMetadata());
            String text = doc.getText();
            log.info("内容前200字: {}", text.substring(0, Math.min(200, text.length())));
            Assertions.assertFalse(text.isBlank(), "文档内容不能为空");
        }

        boolean hasMetadata = documents.stream()
                .anyMatch(d -> "repo_metadata".equals(d.getMetadata().get("type")));
        Assertions.assertTrue(hasMetadata, "应包含仓库元信息文档");
    }

    @Test
    @DisplayName("读取 Spring Boot 仓库并验证 README 文档")
    void readSpringBootRepo() {
        GitHubRepoDocumentReader reader = GitHubRepoDocumentReader
                .builder("spring-projects", "spring-boot")
                .branch("main")
                .timeoutMs(20_000)
                .build();

        List<Document> documents = reader.get();

        Assertions.assertNotNull(documents);
        Assertions.assertTrue(documents.size() >= 2, "应至少包含元信息和 README 两个文档");

        Document readmeDoc = documents.stream()
                .filter(d -> "readme".equals(d.getMetadata().get("type")))
                .findFirst()
                .orElse(null);
        Assertions.assertNotNull(readmeDoc, "应包含 README 文档");
        Assertions.assertTrue(readmeDoc.getText().contains("Spring Boot"),
                "Spring Boot 仓库的 README 应包含 'Spring Boot'");

        log.info("Spring Boot 仓库共读取 {} 个文档", documents.size());
    }

    @Test
    @DisplayName("读取不存在的仓库应返回空列表而非抛异常")
    void readNonExistentRepo() {
        GitHubRepoDocumentReader reader = GitHubRepoDocumentReader
                .builder("this-owner-does-not-exist-12345", "this-repo-does-not-exist")
                .timeoutMs(10_000)
                .build();

        List<Document> documents = reader.get();

        Assertions.assertNotNull(documents);
        Assertions.assertTrue(documents.isEmpty(), "不存在的仓库应返回空列表");
        log.info("不存在的仓库正确返回空列表");
    }
}
