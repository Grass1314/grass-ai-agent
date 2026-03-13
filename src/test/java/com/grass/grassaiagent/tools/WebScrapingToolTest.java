package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WebScrapingToolTest {

    private WebScrapingTool tool;

    @BeforeEach
    public void setUp() {
        tool = new WebScrapingTool();
    }

    @Test
    public void testScrapeWebPage() {
        String result = tool.scrapeWebPage("https://www.baidu.com");
        assertNotNull(result);
        assertTrue(result.contains("Title:"));
        System.out.println(result);
    }

    @Test
    public void testScrapeWebPageWithArticle() {
        String result = tool.scrapeWebPage("https://spring.io/blog");
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testScrapeWithSelector() {
        String result = tool.scrapeWithSelector("https://www.baidu.com", "a");
        assertNotNull(result);
        assertTrue(result.contains("element(s)"));
        System.out.println(result);
    }

    @Test
    public void testScrapeWithSelectorNoMatch() {
        String result = tool.scrapeWithSelector("https://www.baidu.com", "#nonexistent-element-xyz");
        assertNotNull(result);
        assertTrue(result.contains("No elements found"));
        System.out.println(result);
    }

    @Test
    public void testScrapeInvalidUrl() {
        String result = tool.scrapeWebPage("https://this-domain-does-not-exist-12345.com");
        assertNotNull(result);
        assertTrue(result.contains("Error"));
        System.out.println(result);
    }
}
