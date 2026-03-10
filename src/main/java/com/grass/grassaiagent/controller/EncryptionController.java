package com.grass.grassaiagent.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @description: 配置加解密接口，用于对敏感配置项进行 Jasypt 加密/解密
 * @author Mr.Grass
 * @version 1.0
 * @date 2026/03/10
 */
@Tag(name = "配置加解密", description = "对敏感配置进行 Jasypt 加密/解密，密文可直接放入 application.yaml 的 ENC() 中")
@RestController
@RequestMapping("/encryption")
public class EncryptionController {

    @Resource
    private StringEncryptor jasyptStringEncryptor;

    @Operation(summary = "加密", description = "将明文加密为 Jasypt 密文，结果可直接以 ENC(密文) 形式写入配置文件")
    @GetMapping("/encrypt")
    public Map<String, String> encrypt(@RequestParam String text) {
        String encrypted = jasyptStringEncryptor.encrypt(text);
        return Map.of(
                "input", text,
                "encrypted", encrypted,
                "yamlFormat", "ENC(" + encrypted + ")"
        );
    }

    @Operation(summary = "解密", description = "将 Jasypt 密文解密为明文，传入 ENC() 括号内的密文即可")
    @GetMapping("/decrypt")
    public Map<String, String> decrypt(@RequestParam String text) {
        String decrypted = jasyptStringEncryptor.decrypt(text);
        return Map.of(
                "input", text,
                "decrypted", decrypted
        );
    }
}
