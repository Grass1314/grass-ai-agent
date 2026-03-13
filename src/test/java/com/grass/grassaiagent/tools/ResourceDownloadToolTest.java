package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResourceDownloadToolTest {

    private ResourceDownloadTool tool;

    @BeforeEach
    public void setUp() {
        tool = new ResourceDownloadTool();
    }

    @Test
    public void testDownloadImage() {
        String result = tool.downloadResource(
                "https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png",
                "baidu_logo.png");
        assertNotNull(result);
        assertTrue(result.contains("downloaded successfully"));
        System.out.println(result);
    }

    @Test
    public void testDownloadWithSpecialFileName() {
        String result = tool.downloadResource(
                "https://www.baidu.com/robots.txt",
                "baidu_robots.txt");
        assertNotNull(result);
        System.out.println(result);
    }

    @Test
    public void testDownloadInvalidUrl() {
        String result = tool.downloadResource(
                "https://this-domain-does-not-exist-12345.com/file.zip",
                "test.zip");
        assertNotNull(result);
        assertTrue(result.contains("Error"));
        System.out.println(result);
    }

    @Test
    public void testBatchDownload() {
        String urls = "https://www.baidu.com/robots.txt, https://www.baidu.com/favicon.ico";
        String result = tool.batchDownload(urls);
        assertNotNull(result);
        assertTrue(result.contains("Batch download results"));
        System.out.println(result);
    }

    @Test
    public void testBatchDownloadWithInvalid() {
        String urls = "https://www.baidu.com/robots.txt, https://invalid-url-xyz.com/nothing.dat";
        String result = tool.batchDownload(urls);
        assertNotNull(result);
        assertTrue(result.contains("succeeded") || result.contains("failed"));
        System.out.println(result);
    }
}
