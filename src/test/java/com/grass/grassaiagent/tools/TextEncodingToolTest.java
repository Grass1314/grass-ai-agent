package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TextEncodingToolTest {

    private TextEncodingTool tool;

    @BeforeEach
    public void setUp() {
        tool = new TextEncodingTool();
    }

    @Test
    public void testBase64Encode() {
        String result = tool.base64Encode("Hello AI Agent 你好");
        assertNotNull(result);
        assertTrue(result.contains("Base64 encoded"));
        System.out.println(result);
    }

    @Test
    public void testBase64Decode() {
        // "Hello AI Agent 你好" 的 Base64 编码
        String encoded = "SGVsbG8gQUkgQWdlbnQg5L2g5aW9";
        String result = tool.base64Decode(encoded);
        assertNotNull(result);
        assertTrue(result.contains("Hello AI Agent 你好"));
        System.out.println(result);
    }

    @Test
    public void testBase64RoundTrip() {
        String original = "Spring AI 工具开发测试 🚀";
        String encodeResult = tool.base64Encode(original);
        // 提取编码后的字符串（跳过 "Base64 encoded result:\n" 前缀）
        String encoded = encodeResult.replace("Base64 encoded result:\n", "");
        String decodeResult = tool.base64Decode(encoded);
        assertTrue(decodeResult.contains(original));
    }

    @Test
    public void testUrlEncode() {
        String result = tool.urlEncode("name=小草&role=开发者");
        assertNotNull(result);
        assertTrue(result.contains("URL encoded"));
        assertFalse(result.contains("小草"));
        System.out.println(result);
    }

    @Test
    public void testUrlDecode() {
        String result = tool.urlDecode("name%3D%E5%B0%8F%E8%8D%89%26role%3D%E5%BC%80%E5%8F%91%E8%80%85");
        assertNotNull(result);
        assertTrue(result.contains("小草"));
        assertTrue(result.contains("开发者"));
        System.out.println(result);
    }

    @Test
    public void testMd5Hash() {
        String result = tool.md5Hash("hello");
        assertNotNull(result);
        assertTrue(result.contains("5d41402abc4b2a76b9719d911017c592"));
        System.out.println(result);
    }

    @Test
    public void testSha256Hash() {
        String result = tool.sha256Hash("hello");
        assertNotNull(result);
        assertTrue(result.contains("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"));
        System.out.println(result);
    }

    @Test
    public void testMd5HashChinese() {
        String result = tool.md5Hash("你好世界");
        assertNotNull(result);
        assertTrue(result.contains("MD5 hash:"));
        System.out.println(result);
    }
}
