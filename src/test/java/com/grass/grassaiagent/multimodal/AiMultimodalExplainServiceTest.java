package com.grass.grassaiagent.multimodal;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.grass.grassaiagent.service.aiMultimodal.AiMultimodalExplainService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 多模态图片解释服务测试：使用百炼公开图片 URL 调用真实接口。
 */
@Deprecated
@SpringBootTest
class AiMultimodalExplainServiceTest {

    private static final String PUBLIC_IMAGE_URL =
            "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";

    @Resource
    private AiMultimodalExplainService aiMultimodalExplainService;

    @Test
    @DisplayName("通过图片 URL + 默认提示词解释图片")
    void explainImageByUrlWithDefaultPrompt() throws ApiException, NoApiKeyException, UploadFileException {
        String text = aiMultimodalExplainService.explainImage(PUBLIC_IMAGE_URL, null, null);
        Assertions.assertNotNull(text);
        Assertions.assertFalse(text.isBlank());
    }

    @Test
    @DisplayName("通过图片 URL + 自定义提问解释图片")
    void explainImageByUrlWithCustomPrompt() throws ApiException, NoApiKeyException, UploadFileException {
        String prompt = "这张图里有哪些人和物？简要描述。";
        String text = aiMultimodalExplainService.explainImage(PUBLIC_IMAGE_URL, null, prompt);
        Assertions.assertNotNull(text);
        Assertions.assertFalse(text.isBlank());
    }

    @Test
    @DisplayName("未传图片时应抛出 IllegalArgumentException")
    void explainImageWithoutImageThrows() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                aiMultimodalExplainService.explainImage(null, null, "描述图片"));
    }

    @Test
    @DisplayName("imageUrl 与 imageBase64 同时存在时优先使用 imageUrl")
    void explainImagePrefersUrlWhenBothProvided() throws ApiException, NoApiKeyException, UploadFileException {
        String text = aiMultimodalExplainService.explainImage(PUBLIC_IMAGE_URL, "invalidBase64", null);
        Assertions.assertNotNull(text);
        Assertions.assertFalse(text.isBlank());
    }
}
