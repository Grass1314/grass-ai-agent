package com.grass.grassaiagent.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestToolTest {

    private HttpRequestTool tool;

    @BeforeEach
    public void setUp() {
        tool = new HttpRequestTool();
    }

    @Test
    public void testHttpGet() {
        String result = tool.httpGet("https://httpbin.org/get");
        assertNotNull(result);
        assertTrue(result.contains("HTTP Status: 200"));
        System.out.println(result);
    }

    @Test
    public void testHttpGetWithParams() {
        String result = tool.httpGet("https://httpbin.org/get?name=grass&role=developer");
        assertNotNull(result);
        assertTrue(result.contains("HTTP Status: 200"));
        assertTrue(result.contains("grass"));
        System.out.println(result);
    }

    @Test
    public void testHttpPost() {
        String jsonBody = "{\"name\": \"grass\", \"message\": \"hello from ai agent\"}";
        String result = tool.httpPost("https://httpbin.org/post", jsonBody);
        assertNotNull(result);
        assertTrue(result.contains("HTTP Status: 200"));
        assertTrue(result.contains("grass"));
        System.out.println(result);
    }

    @Test
    public void testHttpGetInvalidUrl() {
        String result = tool.httpGet("https://this-domain-does-not-exist-12345.com/api");
        assertNotNull(result);
        assertTrue(result.contains("Error"));
        System.out.println(result);
    }

    @Test
    public void testHttpGet404() {
        String result = tool.httpGet("https://httpbin.org/status/404");
        assertNotNull(result);
        assertTrue(result.contains("HTTP Status: 404"));
        System.out.println(result);
    }
}
