package com.grass.grassimagesearchmcp.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: 图片搜索工具
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/19 14:03
 * tip 官方文档：<a href="https://www.pexels.com/api/documentation/?language=csharp#photos-search">官方文档</a>
 */
@Service
public class ImageSearchTool {

    // Pexels API Key
    private static final String API_KEY = "bVnO0B83pXsR01HhH2YyP3N72tPquebXM7MzhewK4WgjYpMRqMBcOH00";

    // Pexels API URL
    private static final String API_URL = "https://api.pexels.com/v1/search?query=";

    @Tool(description = "Search images for web")
    public String searchImages(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchMediumImages(query));
        } catch (Exception e) {
            return "Error search image: " + e.getMessage();
        }
    }

    /**
     * 搜索中等分辨率的图片
     * @param query 查询关键字
     * @return 图片链接列表
     */
    private List<String> searchMediumImages(String query) {
        // 设置请求头(包含API秘钥)
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", API_KEY);

        // 设置请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);

        // 发送GET请求
        try (var response = HttpUtil.createGet(API_URL)
                .addHeaders(headers)
                .form(params)
                .execute()) {
            // Get the body of the response
            String responseBody = response.body();
            // 解析响应JSON数据(查看官方文档发现：响应结构包含“photos”数组，每个元素中"src"属性JSON中包含“medium”字段)
            return JSONUtil.parseObj(responseBody)
                .getJSONArray("photos")
                .stream()
                .map(photoObj -> (JSONObject) photoObj)
                .map(photoObj -> photoObj.getJSONObject("src"))
                .map(srcObj -> srcObj.getStr("medium"))
                .filter(StrUtil ::isNotBlank)
                .collect(Collectors.toList());
        }
    }
}
