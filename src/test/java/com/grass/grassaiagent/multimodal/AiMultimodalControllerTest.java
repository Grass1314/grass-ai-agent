package com.grass.grassaiagent.multimodal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.grass.grassaiagent.controller.AiMultimodalController;
import com.grass.grassaiagent.dto.AiMultimodalExplainRequest;
import com.grass.grassaiagent.service.aiMultimodal.AiMultimodalExplainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 多模态图片解释控制器测试：Mock 服务层，校验请求/响应与错误处理。
 */
@Deprecated
@WebMvcTest(controllers = AiMultimodalController.class)
class AiMultimodalControllerTest {

    private static final String EXPLAIN_URL = "/multimodal/explain";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiMultimodalExplainService aiMultimodalExplainService;

    @Test
    @DisplayName("POST /multimodal/explain 传入 imageUrl 返回 200 与解释文本")
    void explainWithImageUrlReturns200() throws Exception {
        String imageUrl = "https://dashscope.oss-cn-beijing.aliyuncs.com/images/dog_and_girl.jpeg";
        String expectedText = "图中有一条狗和一位女孩。";
        when(aiMultimodalExplainService.explainImage(eq(imageUrl), isNull(), isNull()))
                .thenReturn(expectedText);

        AiMultimodalExplainRequest request = new AiMultimodalExplainRequest();
        request.setImageUrl(imageUrl);

        mockMvc.perform(post(EXPLAIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(expectedText));
    }

    @Test
    @DisplayName("POST /multimodal/explain 传入 imageBase64 与 prompt 返回 200")
    void explainWithImageBase64AndPromptReturns200() throws Exception {
        String base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==";
        String prompt = "这是什么？";
        String expectedText = "这是一张图片。";
        when(aiMultimodalExplainService.explainImage(isNull(), eq(base64), eq(prompt)))
                .thenReturn(expectedText);

        AiMultimodalExplainRequest request = new AiMultimodalExplainRequest();
        request.setImageBase64(base64);
        request.setPrompt(prompt);

        mockMvc.perform(post(EXPLAIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(expectedText));
    }

    @Test
    @DisplayName("POST /multimodal/explain 未传图片返回 400")
    void explainWithoutImageReturns400() throws Exception {
        AiMultimodalExplainRequest request = new AiMultimodalExplainRequest();
        request.setPrompt("描述一下");

        when(aiMultimodalExplainService.explainImage(isNull(), isNull(), eq("描述一下")))
                .thenThrow(new IllegalArgumentException("必须提供 imageUrl 或 imageBase64 之一"));

        mockMvc.perform(post(EXPLAIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.text").value(containsString("参数错误")));
    }
}
