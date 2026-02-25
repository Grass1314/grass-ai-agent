package com.grass.grassaiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Mr.Liuxq
 * @description 多模态图片解释请求：imageUrl 与 imageBase64 二选一，prompt 可选
 * @createDate 2026-02-24
 */
@Data
@Schema(description = "图片解释请求：imageUrl 与 imageBase64 二选一")
public class AiMultimodalExplainRequest {

    @Schema(description = "图片 URL，与 imageBase64 二选一")
    private String imageUrl;

    @Schema(description = "图片 Base64 编码（可为纯 base64 或 data:image/xxx;base64,...）")
    private String imageBase64;

    @Schema(description = "对图片的提问，不填则使用默认「请描述或解释这张图片」")
    private String prompt;
}
