package com.grass.grassaiagent.tools;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.crypto.SecureUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * @description: 文本编解码工具，支持Base64、URL编解码及哈希计算
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
public class TextEncodingTool {

    @Tool(description = "Encode text to Base64 format")
    public String base64Encode(
            @ToolParam(description = "The text to encode") String text) {
        try {
            return "Base64 encoded result:\n" + Base64.encode(text);
        } catch (Exception e) {
            return "Error encoding to Base64: " + e.getMessage();
        }
    }

    @Tool(description = "Decode Base64 encoded text back to original text")
    public String base64Decode(
            @ToolParam(description = "The Base64 encoded text to decode") String encodedText) {
        try {
            return "Decoded result:\n" + Base64.decodeStr(encodedText);
        } catch (Exception e) {
            return "Error decoding Base64: " + e.getMessage();
        }
    }

    @Tool(description = "URL-encode a text string (useful for building query parameters)")
    public String urlEncode(
            @ToolParam(description = "The text to URL-encode") String text) {
        try {
            return "URL encoded result:\n" + URLEncodeUtil.encodeAll(text, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error URL encoding: " + e.getMessage();
        }
    }

    @Tool(description = "Decode a URL-encoded text string back to original")
    public String urlDecode(
            @ToolParam(description = "The URL-encoded text to decode") String encodedText) {
        try {
            return "URL decoded result:\n" + URLDecoder.decode(encodedText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "Error URL decoding: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate MD5 hash of a text string")
    public String md5Hash(
            @ToolParam(description = "The text to hash") String text) {
        try {
            return "MD5 hash: " + SecureUtil.md5(text);
        } catch (Exception e) {
            return "Error calculating MD5: " + e.getMessage();
        }
    }

    @Tool(description = "Calculate SHA256 hash of a text string")
    public String sha256Hash(
            @ToolParam(description = "The text to hash") String text) {
        try {
            return "SHA256 hash: " + SecureUtil.sha256(text);
        } catch (Exception e) {
            return "Error calculating SHA256: " + e.getMessage();
        }
    }
}
