package com.grass.grassimagesearchmcp;

import com.grass.grassimagesearchmcp.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class GrassImageSearchMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GrassImageSearchMcpServerApplication.class, args);
    }

    /**
     * 注册图片搜索工具
     */
    @Bean
    public ToolCallbackProvider imageSearchTools(ImageSearchTool imageSearchTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(imageSearchTool)
                .build();
    }

}
