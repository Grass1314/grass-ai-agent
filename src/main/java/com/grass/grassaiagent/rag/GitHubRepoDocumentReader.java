package com.grass.grassaiagent.rag;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: GitHub 仓库文档读取器
 * 通过 GitHub REST API 读取公开仓库的元信息、README 和文件树，转为 Spring AI Document 列表。
 * 实现 Spring AI 的 DocumentReader 接口，可直接作为 RAG 文档源使用。
 * @date 2026/03/09 16:00
 */
@Slf4j
public class GitHubRepoDocumentReader implements DocumentReader {

    private static final String GITHUB_API = "https://api.github.com";
    private static final int DEFAULT_TIMEOUT_MS = 15_000;

    private final String owner;
    private final String repo;
    private final String branch;
    private final String token;
    private final int timeoutMs;

    private GitHubRepoDocumentReader(Builder builder) {
        this.owner = Objects.requireNonNull(builder.owner, "owner 不能为空");
        this.repo = Objects.requireNonNull(builder.repo, "repo 不能为空");
        this.branch = builder.branch;
        this.token = builder.token;
        this.timeoutMs = builder.timeoutMs;
    }

    public static Builder builder(String owner, String repo) {
        return new Builder(owner, repo);
    }

    @Override
    public List<Document> get() {
        List<Document> documents = new ArrayList<>();
        Map<String, String> baseMeta = Map.of("source", "github", "owner", owner, "repo", repo);

        JSONObject repoInfo = fetchRepoInfo();
        if (repoInfo != null) {
            documents.add(buildRepoMetadataDocument(repoInfo, baseMeta));
        }

        String readme = fetchReadmeContent();
        if (readme != null && !readme.isBlank()) {
            documents.add(buildReadmeDocument(readme, baseMeta));
        }

        String fileTree = fetchFileTree(repoInfo);
        if (fileTree != null && !fileTree.isBlank()) {
            documents.add(buildFileTreeDocument(fileTree, baseMeta));
        }

        log.info("GitHub 仓库 {}/{} 共读取 {} 个文档", owner, repo, documents.size());
        return documents;
    }

    // ==================== 内部构建方法 ====================

    private Document buildRepoMetadataDocument(JSONObject repoInfo, Map<String, String> baseMeta) {
        String description = repoInfo.getStr("description", "无描述");
        String language = repoInfo.getStr("language", "未知");
        int stars = repoInfo.getInt("stargazers_count", 0);
        int forks = repoInfo.getInt("forks_count", 0);
        String htmlUrl = repoInfo.getStr("html_url", "");
        String createdAt = repoInfo.getStr("created_at", "");
        String updatedAt = repoInfo.getStr("updated_at", "");

        JSONArray topicsArr = repoInfo.getJSONArray("topics");
        String topics = (topicsArr != null) ? topicsArr.toList(String.class).toString() : "[]";

        String content = String.format("""
                GitHub 仓库: %s/%s
                描述: %s
                主要语言: %s
                Star 数: %d | Fork 数: %d
                主题标签: %s
                创建时间: %s | 最后更新: %s
                仓库地址: %s""",
                owner, repo, description, language, stars, forks, topics, createdAt, updatedAt, htmlUrl);

        Map<String, Object> metadata = new HashMap<>(baseMeta);
        metadata.put("type", "repo_metadata");
        metadata.put("stars", stars);
        metadata.put("language", language);
        return new Document(content, metadata);
    }

    private Document buildReadmeDocument(String readme, Map<String, String> baseMeta) {
        Map<String, Object> metadata = new HashMap<>(baseMeta);
        metadata.put("type", "readme");
        metadata.put("filename", "README.md");
        return new Document(readme, metadata);
    }

    private Document buildFileTreeDocument(String fileTree, Map<String, String> baseMeta) {
        Map<String, Object> metadata = new HashMap<>(baseMeta);
        metadata.put("type", "file_tree");
        return new Document(fileTree, metadata);
    }

    // ==================== GitHub API 调用 ====================

    private JSONObject fetchRepoInfo() {
        String url = GITHUB_API + "/repos/" + owner + "/" + repo;
        try {
            String body = doGet(url);
            return JSONUtil.parseObj(body);
        } catch (Exception e) {
            log.warn("获取仓库信息失败 {}/{}: {}", owner, repo, e.getMessage());
            return null;
        }
    }

    private String fetchReadmeContent() {
        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/readme";
        try {
            String body = doGet(url);
            JSONObject json = JSONUtil.parseObj(body);
            String contentBase64 = json.getStr("content", "");
            String encoding = json.getStr("encoding", "base64");
            if ("base64".equals(encoding) && !contentBase64.isBlank()) {
                String cleaned = contentBase64.replaceAll("\\s+", "");
                return new String(Base64.getDecoder().decode(cleaned), StandardCharsets.UTF_8);
            }
            return contentBase64;
        } catch (Exception e) {
            log.warn("获取 README 失败 {}/{}: {}", owner, repo, e.getMessage());
            return null;
        }
    }

    private String fetchFileTree(JSONObject repoInfo) {
        String defaultBranch = this.branch;
        if (defaultBranch == null && repoInfo != null) {
            defaultBranch = repoInfo.getStr("default_branch", "main");
        }
        if (defaultBranch == null) {
            defaultBranch = "main";
        }
        String url = GITHUB_API + "/repos/" + owner + "/" + repo + "/git/trees/" + defaultBranch + "?recursive=1";
        try {
            String body = doGet(url);
            JSONObject json = JSONUtil.parseObj(body);
            JSONArray tree = json.getJSONArray("tree");
            if (tree == null || tree.isEmpty()) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("文件树 (").append(owner).append("/").append(repo).append("):\n");
            for (int i = 0; i < tree.size(); i++) {
                JSONObject node = tree.getJSONObject(i);
                String path = node.getStr("path");
                String type = node.getStr("type");
                sb.append(type.equals("tree") ? "[目录] " : "       ").append(path).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取文件树失败 {}/{}: {}", owner, repo, e.getMessage());
            return null;
        }
    }

    private String doGet(String url) {
        HttpRequest request = HttpRequest.get(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "grass-ai-agent")
                .timeout(timeoutMs);
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }
        try (HttpResponse response = request.execute()) {
            if (!response.isOk()) {
                throw new RuntimeException("HTTP " + response.getStatus() + ": " + response.body());
            }
            return response.body();
        }
    }

    // ==================== Builder ====================

    public static class Builder {
        private final String owner;
        private final String repo;
        private String branch;
        private String token;
        private int timeoutMs = DEFAULT_TIMEOUT_MS;

        private Builder(String owner, String repo) {
            this.owner = owner;
            this.repo = repo;
        }

        /** 指定分支，不指定则自动取仓库默认分支 */
        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        /** GitHub Personal Access Token，用于提升 API 速率限制 */
        public Builder token(String token) {
            this.token = token;
            return this;
        }

        /** HTTP 请求超时时间（毫秒） */
        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public GitHubRepoDocumentReader build() {
            return new GitHubRepoDocumentReader(this);
        }
    }
}
