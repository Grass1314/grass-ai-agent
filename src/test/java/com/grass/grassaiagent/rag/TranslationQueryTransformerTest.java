package com.grass.grassaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;

/**
 * @author Mr.Grass
 * @version 1.0
 * @description: TranslationQueryTransformer 测试
 * @date 2026/03/09 16:30
 */
@Slf4j
class TranslationQueryTransformerTest {

    @Test
    @DisplayName("使用 MyMemory API 将中文查询翻译为英文")
    void translateChineseToEnglish() {
        TranslationQueryTransformer transformer = TranslationQueryTransformer.builder()
                .sourceLang("zh-CN")
                .targetLang("en")
                .build();

        Query original = new Query("单身的人如何拓展社交圈");
        Query translated = transformer.transform(original);

        Assertions.assertNotNull(translated);
        Assertions.assertFalse(translated.text().isBlank());
        log.info("原文: {}", original.text());
        log.info("译文: {}", translated.text());
    }

    @Test
    @DisplayName("使用 MyMemory API 将英文查询翻译为中文")
    void translateEnglishToChinese() {
        TranslationQueryTransformer transformer = TranslationQueryTransformer.builder()
                .sourceLang("en")
                .targetLang("zh-CN")
                .build();

        Query original = new Query("How to improve relationship after marriage?");
        Query translated = transformer.transform(original);

        Assertions.assertNotNull(translated);
        Assertions.assertFalse(translated.text().isBlank());
        log.info("原文: {}", original.text());
        log.info("译文: {}", translated.text());
    }

    @Test
    @DisplayName("使用自定义翻译服务替换默认实现")
    void useCustomTranslationService() {
        TranslationQueryTransformer.TranslationService mockService =
                (text, src, tgt) -> "[MOCK_" + tgt + "] " + text;

        TranslationQueryTransformer transformer = TranslationQueryTransformer.builder()
                .sourceLang("zh-CN")
                .targetLang("en")
                .translationService(mockService)
                .build();

        Query original = new Query("测试自定义翻译");
        Query translated = transformer.transform(original);

        Assertions.assertEquals("[MOCK_en] 测试自定义翻译", translated.text());
        log.info("自定义翻译结果: {}", translated.text());
    }

    @Test
    @DisplayName("翻译失败时使用降级策略返回原文")
    void fallbackReturnOriginalOnError() {
        TranslationQueryTransformer.TranslationService brokenService =
                (text, src, tgt) -> { throw new RuntimeException("网络超时"); };

        TranslationQueryTransformer transformer = TranslationQueryTransformer.builder()
                .translationService(brokenService)
                .build();

        Query original = new Query("降级测试用例");
        Query result = transformer.transform(original);

        Assertions.assertEquals("降级测试用例", result.text(), "翻译失败应降级返回原文");
    }

    @Test
    @DisplayName("翻译失败时应降级返回原文")
    void fallbackOnTranslationFailure() {
        TranslationQueryTransformer.TranslationService failingService =
                (text, src, tgt) -> { throw new RuntimeException("模拟翻译失败"); };

        TranslationQueryTransformer transformer = TranslationQueryTransformer.builder()
                .translationService(failingService)
                .build();

        Query original = new Query("翻译会失败的文本");
        Query result = transformer.transform(original);

        Assertions.assertEquals("翻译会失败的文本", result.text(), "翻译失败应返回原文");
    }
}
