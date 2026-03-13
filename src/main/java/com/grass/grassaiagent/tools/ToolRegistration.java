package com.grass.grassaiagent.tools;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: 工具注册
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/13 10:26
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Value("${search-api.api-url}")
    private String searchApiUrl;

    @Bean
    public ToolCallback[] toolCallbacks() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey, searchApiUrl);
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        HttpRequestTool httpRequestTool = new HttpRequestTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        TextEncodingTool textEncodingTool = new TextEncodingTool();
        TimeInfoTool timeInfoTool = new TimeInfoTool();
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        return ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                pdfGenerationTool,
                httpRequestTool,
                resourceDownloadTool,
                terminalOperationTool,
                textEncodingTool,
                timeInfoTool,
                webScrapingTool);
    }
}
