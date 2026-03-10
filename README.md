# grass-ai-agent

密钥管理
加密算法：PBEWITHHMACSHA512ANDAES_256（AES-256 强加密）
开发环境：使用默认密钥 grass-ai-agent-secret-2026（已写入 yaml 默认值）
生产环境：通过环境变量传入密钥，配置文件中不暴露真实密钥：
export JASYPT_ENCRYPTOR_PASSWORD=你的生产密钥
或 JVM 启动参数：
java -Djasypt.encryptor.password=你的生产密钥 -jar app.jar