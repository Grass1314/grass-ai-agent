package com.grass.grassaiagent.tools;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Jasypt 加密工具测试：生成所有敏感配置项的密文，
 * 输出可直接复制到 application.yaml 的 ENC() 中使用。
 */
public class JasyptEncryptTest {

    private static final String ENCRYPTOR_PASSWORD = "grass-ai-agent-secret-2026";
    private PooledPBEStringEncryptor encryptor;

    @BeforeEach
    public void setUp() {
        encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(ENCRYPTOR_PASSWORD);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
    }

    @Test
    public void generateAllEncryptedValues() {
        String[][] items = {
                {"MySQL username",      "grass_ai_agent_root"},
                {"MySQL password",      "grass_ai_agent_root_123456"},
                {"PostgreSQL username",  "ai_agent_pg_root"},
                {"PostgreSQL password",  "ai_agent_pg_root_123456!"},
                {"DashScope API Key",   "sk-eb1069f36bcd4af0bd638c5e6fa4f026"},
                {"SearchAPI Key",       "rNFWvAY2puMxosqTXpkvjUAZ"},
        };

        System.out.println("========== Jasypt 加密结果 ==========");
        System.out.println("加密密钥: " + ENCRYPTOR_PASSWORD);
        System.out.println("算法: PBEWITHHMACSHA512ANDAES_256\n");

        for (String[] item : items) {
            String encrypted = encryptor.encrypt(item[1]);
            System.out.println(item[0] + ":");
            System.out.println("  明文: " + item[1]);
            System.out.println("  密文: " + encrypted);
            System.out.println("  YAML: ENC(" + encrypted + ")");
            System.out.println();
        }
    }

    @Test
    public void verifyEncryptDecrypt() {
        String original = "grass_ai_agent_root_123456";
        String encrypted = encryptor.encrypt(original);
        String decrypted = encryptor.decrypt(encrypted);
        System.out.println("原文: " + original);
        System.out.println("密文: " + encrypted);
        System.out.println("解密: " + decrypted);
        assert original.equals(decrypted) : "解密结果与原文不一致!";
    }
}
