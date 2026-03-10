package com.grass.grassaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 联网搜索工具，基于 SearchAPI 实现互联网实时搜索
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class WebSearchTool {

    private final String apiKey;
    private final String apiUrl;

    public WebSearchTool(String apiKey, String apiUrl) {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
    }

    @Tool(description = "Search the internet for real-time information. Use this when you need up-to-date information, current events, or facts you're not sure about.")
    public String searchWeb(@ToolParam(description = "The search query keywords") String query,
                            @ToolParam(description = "Number of results to return, default 5") int count) {
        if (count <= 0 || count > 10) {
            count = 5;
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("engine", "google");
            params.put("q", query);
            params.put("api_key", apiKey);
            params.put("num", count);

            String response = HttpUtil.get(apiUrl, params);
            JSONObject json = JSONUtil.parseObj(response);
            JSONArray organicResults = json.getJSONArray("organic_results");
            if (organicResults == null || organicResults.isEmpty()) {
                return "No search results found for: " + query;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Search results for: ").append(query).append("\n\n");
            int size = Math.min(organicResults.size(), count);
            for (int i = 0; i < size; i++) {
                JSONObject result = organicResults.getJSONObject(i);
                sb.append(i + 1).append(". ").append(result.getStr("title", "No title")).append("\n");
                sb.append("   URL: ").append(result.getStr("link", "")).append("\n");
                sb.append("   ").append(result.getStr("snippet", "No description")).append("\n\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return "Error performing web search: " + e.getMessage();
        }
    }
}
