package com.grass.grassaiagent.rag;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

import java.util.Objects;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: 基于第三方翻译 API 的查询转换器
 * 替代 Spring AI 内置的基于大模型的 TranslationQueryTransformer，
 * 使用轻量级 HTTP 翻译接口实现跨语言查询转换，大幅降低调用成本。
 * 默认使用 MyMemory 免费翻译 API，可通过 Builder 替换为任意翻译服务。
 * @date 2026/03/09 16:30
 */
@Slf4j
public class TranslationQueryTransformer implements QueryTransformer {

    /**
     * 翻译服务抽象接口，用户可自行实现对接百度翻译、阿里翻译、有道翻译等。
     */
    @FunctionalInterface
    public interface TranslationService {
        String translate(String text, String sourceLang, String targetLang);
    }

    private final String sourceLang;
    private final String targetLang;
    private final TranslationService translationService;

    private TranslationQueryTransformer(Builder builder) {
        this.sourceLang = builder.sourceLang;
        this.targetLang = builder.targetLang;
        this.translationService = builder.translationService;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Query transform(Query query) {
        String original = query.text();
        if (original == null || original.isBlank()) {
            return query;
        }
        try {
            String translated = translationService.translate(original, sourceLang, targetLang);
            log.info("查询翻译: [{}] {} -> {} [{}]", sourceLang, original, translated, targetLang);
            return new Query(translated);
        } catch (Exception e) {
            log.warn("翻译失败，使用原文: {}", e.getMessage());
            return query;
        }
    }

    // ==================== 内置 MyMemory 翻译实现 ====================

    /**
     * 基于 MyMemory 免费翻译 API 的实现。
     * 文档: https://mymemory.translated.net/doc/spec.php
     * 免费额度: 5000 字符/天，无需注册。
     */
    static class MyMemoryTranslationService implements TranslationService {

        private static final String API_URL = "https://api.mymemory.translated.net/get";
        private static final int TIMEOUT_MS = 10_000;

        @Override
        public String translate(String text, String sourceLang, String targetLang) {
            String langPair = sourceLang + "|" + targetLang;
            try (HttpResponse response = HttpRequest.get(API_URL)
                    .form("q", text)
                    .form("langpair", langPair)
                    .timeout(TIMEOUT_MS)
                    .execute()) {
                if (!response.isOk()) {
                    throw new RuntimeException("翻译 API 返回 HTTP " + response.getStatus());
                }
                JSONObject json = JSONUtil.parseObj(response.body());
                int status = json.getInt("responseStatus", 0);
                if (status != 200) {
                    throw new RuntimeException("翻译 API 业务错误, status=" + status);
                }
                return json.getJSONObject("responseData").getStr("translatedText");
            }
        }
    }

    // ==================== Builder ====================

    public static class Builder {
        private String sourceLang = "zh-CN";
        private String targetLang = "en";
        private TranslationService translationService;

        /** 源语言，默认 zh-CN */
        public Builder sourceLang(String sourceLang) {
            this.sourceLang = sourceLang;
            return this;
        }

        /** 目标语言，默认 en */
        public Builder targetLang(String targetLang) {
            this.targetLang = targetLang;
            return this;
        }

        /**
         * 自定义翻译服务实现。
         * 不设置则使用内置的 MyMemory 免费翻译 API。
         */
        public Builder translationService(TranslationService translationService) {
            this.translationService = translationService;
            return this;
        }

        public TranslationQueryTransformer build() {
            Objects.requireNonNull(sourceLang, "sourceLang 不能为空");
            Objects.requireNonNull(targetLang, "targetLang 不能为空");
            if (this.translationService == null) {
                this.translationService = new MyMemoryTranslationService();
            }
            return new TranslationQueryTransformer(this);
        }
    }
}
