package com.grass.grassaiagent.service.aiMultimodal.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.grass.grassaiagent.service.aiMultimodal.AiMultimodalExplainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Mr.Liuxq
 * @description 多模态图片解释服务实现（基于百炼 qwen-vl-plus 等）
 * @createDate 2026-02-24
 */
@Slf4j
@Service
public class AiMultimodalExplainServiceImpl implements AiMultimodalExplainService {

    private static final String DEFAULT_PROMPT = "请描述或解释这张图片的内容。";

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.multimodal.model:qwen-vl-plus}")
    private String model;

    @Override
    public String explainImage(String imageUrl, String imageBase64, String prompt)
            throws ApiException, NoApiKeyException, UploadFileException {
        List<Map<String, Object>> content = new ArrayList<>();

        if (imageUrl != null && !imageUrl.isBlank()) {
            content.add(Collections.singletonMap("image", imageUrl.trim()));
        } else if (imageBase64 != null && !imageBase64.isBlank()) {
            String dataUrl = toDataUrl(imageBase64.trim());
            content.add(Collections.singletonMap("image", dataUrl));
        } else {
            throw new IllegalArgumentException("必须提供 imageUrl 或 imageBase64 之一");
        }

        String question = (prompt != null && !prompt.isBlank()) ? prompt.trim() : DEFAULT_PROMPT;
        content.add(Collections.singletonMap("text", question));

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(content)
                .build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .message(userMessage)
                .build();

        MultiModalConversation conv = new MultiModalConversation();
        MultiModalConversationResult result = conv.call(param);

        if (result == null || result.getOutput() == null || result.getOutput().getChoices() == null
                || result.getOutput().getChoices().isEmpty()) {
            log.warn("多模态接口返回为空: {}", result);
            return "";
        }

        Object resultContent = result.getOutput().getChoices().get(0).getMessage().getContent();
        if (resultContent == null) {
            return "";
        }
        if (resultContent instanceof String s) {
            return s;
        }
        if (resultContent instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> map && map.get("text") != null) {
                return map.get("text").toString();
            }
            return first.toString();
        }
        return resultContent.toString();
    }

    private static String toDataUrl(String raw) {
        if (raw.startsWith("data:")) {
            return raw;
        }
        return "data:image/jpeg;base64," + raw;
    }
}
