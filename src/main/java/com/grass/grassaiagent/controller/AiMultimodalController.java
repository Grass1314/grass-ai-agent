package com.grass.grassaiagent.controller;

import com.grass.grassaiagent.dto.AiMultimodalExplainRequest;
import com.grass.grassaiagent.dto.AiMultimodalExplainResponse;
import com.grass.grassaiagent.service.aiMultimodal.AiMultimodalExplainService;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Mr.Liuxq
 * @description 多模态对话助手：图片解释接口（阿里云百炼）
 * @createDate 2026-02-24
 */
@Tag(name = "多模态对话助手", description = "图片解释（阿里云百炼）")
@RestController
@RequestMapping("/multimodal")
@RequiredArgsConstructor
@Slf4j
public class AiMultimodalController {

    private final AiMultimodalExplainService aiMultimodalExplainService;

    @Operation(summary = "解释图片", description = "传入图片 URL 或 Base64，可选提问，返回模型对图片的解释")
    @PostMapping("/explain")
    public ResponseEntity<AiMultimodalExplainResponse> explainImage(@Valid @RequestBody AiMultimodalExplainRequest request) {
        try {
            String text = aiMultimodalExplainService.explainImage(
                    request.getImageUrl(),
                    request.getImageBase64(),
                    request.getPrompt()
            );
            return ResponseEntity.ok(new AiMultimodalExplainResponse(text));
        } catch (IllegalArgumentException e) {
            log.warn("多模态请求参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new AiMultimodalExplainResponse("参数错误: " + e.getMessage()));
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("百炼多模态调用异常", e);
            return ResponseEntity.internalServerError()
                    .body(new AiMultimodalExplainResponse("服务暂时不可用: " + e.getMessage()));
        }
    }
}
