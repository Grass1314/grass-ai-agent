package com.grass.grassaiagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Mr.Liuxq
 * @description 多模态图片解释响应
 * @createDate 2026-02-24
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "图片解释结果")
public class AiMultimodalExplainResponse {

    @Schema(description = "模型对图片的解释或回答")
    private String text;
}
