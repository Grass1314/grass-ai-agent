package com.grass.grassaiagent.service.aiMultimodal;

import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;

/**
 * @author Mr.Liuxq
 * @description 多模态图片解释服务（阿里云百炼）
 * @createDate 2026-02-24
 */
public interface AiMultimodalExplainService {

    /**
     * 根据图片与提示词，调用多模态模型得到解释文本。
     *
     * @param imageUrl    图片 URL（与 imageBase64 二选一）
     * @param imageBase64 图片 Base64（与 imageUrl 二选一），可为纯 base64 或 data URL
     * @param prompt      用户提问，为空时使用默认提示
     * @return 模型返回的文本
     */
    String explainImage(String imageUrl, String imageBase64, String prompt)
            throws ApiException, NoApiKeyException, UploadFileException;
}
