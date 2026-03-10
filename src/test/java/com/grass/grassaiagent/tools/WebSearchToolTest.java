package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebSearchToolTest {

    private WebSearchTool tool;

    @BeforeEach
    public void setUp() {
        tool = new WebSearchTool("rNFWvAY2puMxosqTXpkvjUAZ", "https://www.searchapi.io/api/v1/search");
    }

    @Test
    public void testSearchWeb() {
        String result = tool.searchWeb("Spring AI tutorial", 3);
        assertNotNull(result);
        assertFalse(result.contains("Error"));
        System.out.println(result);
    }

    @Test
    public void testSearchWebChinese() {
        String result = tool.searchWeb("Java 开发入门教程", 5);
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testSearchWebDefaultCount() {
        String result = tool.searchWeb("OpenAI GPT", 0);
        assertNotNull(result);
        System.out.println(result);
    }
}
