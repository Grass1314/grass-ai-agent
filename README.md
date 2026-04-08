# grass-ai-agent

基于 **Spring Boot 3** 与 **Spring AI / 阿里云 DashScope** 的 AI 对话与知识服务后端，配套 **Vue 3 + Vite** 前端（「小草 AI」平台：恋爱大师、超级智能体等）。支持会话与消息持久化、PgVector 向量检索、MCP 客户端（SSE 连接外部 MCP 服务）及多模态读图等能力。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21、Spring Boot 3.5.10、Spring AI、Spring AI Alibaba（DashScope）、LangChain4j（DashScope）、MyBatis-Plus、动态多数据源（MySQL + PostgreSQL）、Jasypt |
| 向量与 RAG | PostgreSQL + pgvector、Spring AI Vector Store / Advisors |
| 前端 | Vue 3、Vue Router、Vite 4、Axios、@vueuse/head |
| 文档与调试 | SpringDoc OpenAPI、Knife4j |
| 可选子目录 | `grass-image-search-mcp-server`：独立 Spring Boot MCP 服务（默认端口 8127，与主工程非同一 Maven 多模块） |

## 仓库结构

```
grass-ai-agent/                 # 主后端（单模块 Maven 项目）
├── src/main/java/com/grass/grassaiagent/
│   ├── controller/             # REST API
│   ├── agent/、app/            # 智能体与业务应用（如 LoveApp、GrassManus）
│   └── ...
├── src/main/resources/
│   ├── application.yaml        # 主配置（数据源、AI、MCP、日志等）
│   ├── sql/                    # 会话/消息等表结构说明与脚本
│   └── document/               # 内置文档资源（如情感问答素材）
├── grass-ai-agent-frontend/    # Vue 前端
├── grass-image-search-mcp-server/  # 图片检索等 MCP 服务端（可选，需单独 mvn 启动）
├── Dockerfile                  # 后端镜像（镜像内 Maven 打包 + Corretto 21 运行）
├── docs/                       # 项目内技术笔记
└── HELP.md                     # Spring Boot 脚手架说明
```

## 环境要求

- **JDK 21**、**Maven 3.9+**
- **Node.js**（建议 18+）与 **npm**，用于前端
- **MySQL**、**PostgreSQL（含 pgvector）**：连接信息在配置中自行维护
- 使用 **阿里云 DashScope** 时需准备 **API Key**
- 若启用 MCP SSE：需有可访问的 MCP 服务地址（默认配置为 `http://localhost:8127`）

## 配置说明

### 敏感信息与 Jasypt

配置文件中的数据库口令、DashScope Key 等使用 **Jasypt** 加密（`ENC(...)`）。

- **算法**：`PBEWITHHMACSHA512ANDAES_256`
- **开发**：可使用 `application.yaml` 中的默认 `jasypt.encryptor.password`（或通过环境变量 `JASYPT_ENCRYPTOR_PASSWORD` 覆盖）
- **生产**：务必通过环境变量或 JVM 参数注入，勿在仓库中存放真实密钥：

```bash
export JASYPT_ENCRYPTOR_PASSWORD=你的生产密钥
# 或
java -Djasypt.encryptor.password=你的生产密钥 -jar app.jar
```

主应用还提供 `/encryption/encrypt`、`/encryption/decrypt`（开发辅助，生产请评估是否暴露）。

### 必改项（本地/新环境）

在 `src/main/resources/application.yaml` 中按需修改：

- `spring.datasource.dynamic.datasource.mysql`：业务库 JDBC
- `spring.datasource.dynamic.datasource.postgresql`：供向量/RAG 使用的 PG
- `spring.ai.dashscope.api-key`：DashScope（建议加密后写入）
- `spring.ai.mcp.client.sse.connections.server.url`：MCP SSE 地址
- `search-api`：第三方搜索 API（若使用相关能力）

会话表设计说明见：`src/main/resources/sql/README.md`。

## 启动后端

```bash
cd /path/to/grass-ai-agent
mvn spring-boot:run
# 或
mvn clean package -DskipTests
java -jar target/grass-ai-agent-1.0.0-SNAPSHOT.jar
```

- **HTTP 端口**：`8123`
- **上下文路径**：`/api`（例如健康检查：`http://localhost:8123/api/health`）

## 启动前端

```bash
cd grass-ai-agent-frontend
npm install
npm run dev
```

- **开发服务**：默认 `http://localhost:3000`（见 `vite.config.js`）
- **API**：开发环境请求 `http://localhost:8123/api`（见 `src/api/index.js`）
- **主要页面路由**：`/` 首页、`/love-master` 恋爱大师、`/super-agent` 超级智能体

生产构建使用相对路径 `/api`，需与反向代理或同域部署一致：

```bash
npm run build
npm run preview   # 本地预览构建结果
```

## API 与主要路由

在上下文 `/api` 之下，控制器前缀包括：

| 前缀 | 说明 |
|------|------|
| `/health` | 健康检查 |
| `/ai` | 恋爱应用同步/多种 SSE 流式接口、超级智能体（Manus）SSE 等 |
| `/chat/session`、`/chat/message` | 会话与消息 CRUD / 分页 |
| `/knowledge/doc` | 知识文档管理 |
| `/multimodal` | 多模态（如图片解释） |
| `/encryption` | 加解密辅助接口 |

`/ai` 中与前端联调常见路径示例：

| 路径 | 说明 |
|------|------|
| `GET /ai/love_app/chat/sync` | 恋爱应用同步对话 |
| `GET /ai/love_app/chat/sse` | 恋爱应用 SSE（`text/event-stream`） |
| `GET /ai/love_app/chat/server_sent_event/sse` | SSE（`ServerSentEvent` 格式） |
| `GET /ai/love_app/chat/sse/emitter` | SSE（`SseEmitter`） |
| `GET /ai/manus/chat` | 超级智能体流式（Manus + Tools） |

联调文档（随应用启动）：

- Swagger UI：`http://localhost:8123/api/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8123/api/v3/api-docs`

## Docker（后端）

根目录 `Dockerfile` 基于 `maven:3.9-amazoncorretto-21` 在镜像内执行 `mvn clean package -DskipTests`，并以 **prod** profile 启动：

```bash
docker build -t grass-ai-agent .
docker run -p 8123:8123 -e JASYPT_ENCRYPTOR_PASSWORD=你的生产密钥 grass-ai-agent
```

**说明**：当前仓库根目录下**未**提供 `application-prod.yaml`，`--spring.profiles.active=prod` 仍会生效，实际以默认 `application.yaml` 及环境变量覆盖为准；生产环境建议新增 profile 配置或通过环境变量注入敏感项。

## MCP 子服务（可选）

`grass-image-search-mcp-server` 为**独立**工程，默认端口 **8127**；其 `application.yaml` 中 `spring.profiles.active` 默认为 `sse`，另有 `application-sse.yaml`、`application-stdio.yaml` 可按需切换。主应用通过 `spring.ai.mcp.client.sse` 连接时需先启动：

```bash
cd grass-image-search-mcp-server
mvn spring-boot:run
```

## 生产部署提示

前端目录下的 `nginx.conf` 示例包含 **SSE 反向代理** 所需项（如 `proxy_buffering off`、`proxy_read_timeout` 等），部署时需将 `proxy_pass`、`Host` 替换为实际后端地址。

## 测试

```bash
mvn test
```

---

更多 Spring Boot 脚手架说明见仓库中的 `HELP.md`。
