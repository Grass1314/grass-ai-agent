package com.grass.grassaiagent.tools;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @description: HTTP请求工具，支持GET/POST请求调用外部API
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class HttpRequestTool {

    private static final int TIMEOUT_MS = 15000;
    private static final int MAX_RESPONSE_LENGTH = 5000;

    @Tool(description = "Send an HTTP GET request to a URL and return the response body. Useful for calling REST APIs.")
    public String httpGet(
            @ToolParam(description = "The full URL to send GET request to, e.g. 'https://api.example.com/data'") String url) {
        try {
            HttpResponse response = HttpRequest.of(url)
                    .method(Method.GET)
                    .timeout(TIMEOUT_MS)
                    .execute();
            return formatResponse(response);
        } catch (Exception e) {
            return "Error sending GET request: " + e.getMessage();
        }
    }

    @Tool(description = "Send an HTTP POST request with a JSON body to a URL and return the response. Useful for calling REST APIs that require POST.")
    public String httpPost(
            @ToolParam(description = "The full URL to send POST request to") String url,
            @ToolParam(description = "The JSON request body") String jsonBody) {
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .timeout(TIMEOUT_MS)
                    .execute();
            return formatResponse(response);
        } catch (Exception e) {
            return "Error sending POST request: " + e.getMessage();
        }
    }

    private String formatResponse(HttpResponse response) {
        int status = response.getStatus();
        String body = response.body();
        if (body != null && body.length() > MAX_RESPONSE_LENGTH) {
            body = body.substring(0, MAX_RESPONSE_LENGTH) + "...(response truncated)";
        }
        return "HTTP Status: " + status + "\nResponse:\n" + body;
    }
}
