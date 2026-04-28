package com.grass.grasspdftoimagemcpserver.config;

import com.grass.grasspdftoimagemcpserver.tool.PdfToImageMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider pdfToolCallbackProvider(PdfToImageMcpTool pdfToImageMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(pdfToImageMcpTool)
                .build();
    }
}

