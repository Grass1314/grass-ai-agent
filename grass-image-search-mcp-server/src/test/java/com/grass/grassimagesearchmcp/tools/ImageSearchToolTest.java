package com.grass.grassimagesearchmcp.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;

    @Test
    void searchImages() {
        String result = imageSearchTool.searchImages("cat");
        assertNotNull(result);
        System.out.println(result);
    }
}