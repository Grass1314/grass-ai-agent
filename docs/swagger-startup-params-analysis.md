# Swagger/Knife4j 生产环境启动参数分析报告

## 一、项目技术栈概览

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.10 | 最新稳定版 |
| Java | 21 | LTS |
| Knife4j | 4.4.0 | 基于 springdoc-openapi3-jakarta |
| springdoc-openapi | 2.x | 由 knife4j-openapi3-jakarta-spring-boot-starter 传递引入 |
| Spring Boot Actuator | **未引入** | pom.xml 中无此依赖 |
| Springfox | **未引入** | 项目使用 springdoc，非 Springfox |

---

## 二、原始启动命令

```bash
java -Dmanagement.endpoints.enabled-by-default=false \
     -Dmanagement.server.port=-1 \
     -Dspringdoc.enabled=false \
     -Dspringfox.documentation.enabled=false \
     -Dspring.web.resources.add-mappings=false \
     -Dserver.error.include-stacktrace=never \
     -Dcom.sun.management.jmxremote=false \
     -jar xxx.jar
```

---

## 三、逐参数评估

### 1. `-Dmanagement.endpoints.enabled-by-default=false`

| 项目 | 结论 |
|------|------|
| **作用** | 禁用 Spring Boot Actuator 的所有端点（/actuator/*） |
| **所属框架** | Spring Boot Actuator |
| **本项目是否生效** | ❌ **无效** |
| **原因** | 项目 `pom.xml` 中 **未引入** `spring-boot-starter-actuator` 依赖，此参数没有任何目标 Bean 可以作用 |

> **适用版本**：Spring Boot 2.x / 3.x（需有 Actuator 依赖时才生效）

### 2. `-Dmanagement.server.port=-1`

| 项目 | 结论 |
|------|------|
| **作用** | 禁用 Actuator 的独立管理端口（设为 -1 表示完全关闭管理服务器） |
| **所属框架** | Spring Boot Actuator |
| **本项目是否生效** | ❌ **无效** |
| **原因** | 同上，项目无 Actuator 依赖 |

> **适用版本**：Spring Boot 2.x / 3.x（需有 Actuator 依赖时才生效）

### 3. `-Dspringdoc.enabled=false`

| 项目 | 结论 |
|------|------|
| **作用** | 尝试关闭 springdoc-openapi |
| **所属框架** | springdoc-openapi |
| **本项目是否生效** | ❌ **无效 — 属性名不存在** |
| **原因** | springdoc-openapi（包括 2.6.0）中 **没有** `springdoc.enabled` 这个配置项。正确的属性名为 `springdoc.api-docs.enabled` 和 `springdoc.swagger-ui.enabled` |

> **官方文档确认**：[springdoc.org/properties](https://springdoc.org/properties.html) 中不包含 `springdoc.enabled`，只有 `springdoc.api-docs.enabled` 和 `springdoc.swagger-ui.enabled`

### 4. `-Dspringfox.documentation.enabled=false`

| 项目 | 结论 |
|------|------|
| **作用** | 禁用 Springfox（Swagger 2.x）的自动配置 |
| **所属框架** | Springfox（io.springfox:springfox-boot-starter） |
| **本项目是否生效** | ❌ **无效** |
| **原因** | 项目使用的是 **Knife4j 4.4.0 + springdoc-openapi3（OpenAPI 3.0）**，不是 Springfox。Springfox 是旧版 Swagger 2 的 Spring 集成库，与本项目完全无关 |

> **重要区分**：Springfox ≠ springdoc-openapi。Springfox 在 Spring Boot 3.x 中已不兼容。

### 5. `-Dspring.web.resources.add-mappings=false`

| 项目 | 结论 |
|------|------|
| **作用** | 禁用 Spring Boot 的默认静态资源映射（classpath:/static/、classpath:/public/ 等） |
| **所属框架** | Spring Boot Web |
| **本项目是否生效** | ✅ **生效，但有严重副作用** |
| **副作用** | 会导致 Knife4j UI（`/doc.html`）和 Swagger UI（`/swagger-ui.html`）的静态资源（JS/CSS/HTML）全部返回 404，从而无法打开文档页面。同时也会影响应用中任何其他静态资源 |

> **适用版本**：Spring Boot 2.x（`spring.resources.add-mappings`）/ Spring Boot 3.x（`spring.web.resources.add-mappings`）
>
> ⚠️ 这是一种"暴力"方式，虽然能阻止文档页面加载，但会影响所有静态资源。**不推荐**用于仅禁用 Swagger 的场景。

### 6. `-Dserver.error.include-stacktrace=never`

| 项目 | 结论 |
|------|------|
| **作用** | 错误响应中不包含 Java 堆栈跟踪信息 |
| **所属框架** | Spring Boot Web |
| **本项目是否生效** | ✅ **有效 — 推荐保留** |
| **说明** | 这是生产环境安全加固的最佳实践，防止通过错误页面泄露内部代码结构 |

> **适用版本**：Spring Boot 1.x / 2.x / 3.x 均支持，属性名未变化

### 7. `-Dcom.sun.management.jmxremote=false`

| 项目 | 结论 |
|------|------|
| **作用** | 禁用 JVM 远程 JMX 连接 |
| **所属框架** | JVM（非 Spring） |
| **本项目是否生效** | ✅ **有效 — 推荐保留** |
| **说明** | 安全加固措施，阻止外部通过 JMX 远程连接管理 JVM。生产环境推荐 |

> **适用版本**：所有 JDK 版本

---

## 四、评估总结矩阵

| 参数 | 是否生效 | 推荐 | 原因 |
|------|:--------:|:----:|------|
| `management.endpoints.enabled-by-default=false` | ❌ | 🔸 可选 | 项目无 Actuator，无效但无害；若未来引入 Actuator 可保留 |
| `management.server.port=-1` | ❌ | 🔸 可选 | 同上 |
| `springdoc.enabled=false` | ❌ | ❌ 移除 | **属性不存在**，误导性强，应替换为正确属性 |
| `springfox.documentation.enabled=false` | ❌ | ❌ 移除 | 项目不使用 Springfox，完全无关 |
| `spring.web.resources.add-mappings=false` | ✅ | ⚠️ 慎用 | 副作用大，会影响所有静态资源 |
| `server.error.include-stacktrace=never` | ✅ | ✅ 保留 | 安全最佳实践 |
| `com.sun.management.jmxremote=false` | ✅ | ✅ 保留 | 安全最佳实践 |

---

## 五、推荐方案：针对本项目的正确禁用方式

### 方案 A：使用 Knife4j 内置生产模式（推荐）

Knife4j 4.x 提供了开箱即用的生产环境保护，只需一个参数即可屏蔽所有文档入口：

```bash
java -Dknife4j.production=true \
     -Dserver.error.include-stacktrace=never \
     -Dcom.sun.management.jmxremote=false \
     -jar grass-ai-agent-1.0.0-SNAPSHOT.jar
```

**效果**：Knife4j 会注册 `ProductionSecurityFilter`，拦截以下所有路径并返回 403：
- `/doc.html`
- `/v3/api-docs`、`/v3/api-docs/**`
- `/swagger-ui.html`、`/swagger-ui/**`

**前提条件**：需同时配置 `knife4j.enable=true`（项目 `application.yaml` 中已配置）。

### 方案 B：使用 springdoc 原生属性彻底禁用（最彻底）

如果希望在底层彻底不加载 springdoc 的 Bean 和端点：

```bash
java -Dspringdoc.api-docs.enabled=false \
     -Dspringdoc.swagger-ui.enabled=false \
     -Dknife4j.production=true \
     -Dserver.error.include-stacktrace=never \
     -Dcom.sun.management.jmxremote=false \
     -jar grass-ai-agent-1.0.0-SNAPSHOT.jar
```

**效果**：
- `springdoc.api-docs.enabled=false` → `/v3/api-docs` 端点不再注册
- `springdoc.swagger-ui.enabled=false` → Swagger UI 不再注册
- `knife4j.production=true` → Knife4j 的 `/doc.html` 入口被拦截

### 方案 C：完整安全加固启动命令（生产推荐）

```bash
java \
  # === Swagger/文档禁用 ===
  -Dspringdoc.api-docs.enabled=false \
  -Dspringdoc.swagger-ui.enabled=false \
  -Dknife4j.production=true \
  # === 安全加固 ===
  -Dserver.error.include-stacktrace=never \
  -Dserver.error.include-message=never \
  -Dcom.sun.management.jmxremote=false \
  # === 预留 Actuator 防护（未来引入时自动生效） ===
  -Dmanagement.endpoints.enabled-by-default=false \
  -Dmanagement.server.port=-1 \
  -jar grass-ai-agent-1.0.0-SNAPSHOT.jar
```

---

## 六、Spring Boot / Swagger 各版本属性对照表

下表列出了在不同技术栈组合下，禁用 Swagger 文档的正确属性：

| 场景 | Spring Boot 版本 | 文档框架 | 禁用属性 |
|------|:---:|------|------|
| Springfox + Swagger 2 | 2.x | springfox-boot-starter | `springfox.documentation.enabled=false` |
| springdoc + OpenAPI 3 | 2.x | springdoc-openapi 1.x | `springdoc.api-docs.enabled=false` + `springdoc.swagger-ui.enabled=false` |
| springdoc + OpenAPI 3 | 3.x | springdoc-openapi 2.x | `springdoc.api-docs.enabled=false` + `springdoc.swagger-ui.enabled=false` |
| Knife4j 4.x + springdoc | 3.x | knife4j-openapi3-jakarta | `knife4j.production=true`（或配合 springdoc 属性） |
| **本项目** | **3.5.10** | **Knife4j 4.4.0 + springdoc 2.x** | **`knife4j.production=true` + `springdoc.api-docs.enabled=false` + `springdoc.swagger-ui.enabled=false`** |

> ⚠️ Springfox 自 2020 年起已停止维护，**不兼容 Spring Boot 3.x**（javax → jakarta 迁移）。如果你的项目使用 Spring Boot 3.x，请只使用 springdoc-openapi 2.x 或 Knife4j 4.x。

---

## 七、补充推荐参数

除了 Swagger 相关参数，以下是生产部署时推荐的 JVM / Spring Boot 参数：

```bash
# 错误信息不暴露内部细节
-Dserver.error.include-message=never
-Dserver.error.include-binding-errors=never
-Dserver.error.include-stacktrace=never

# 禁用 Whitelabel 错误页（避免 Spring Boot 默认错误页暴露框架信息）
-Dserver.error.whitelabel.enabled=false

# 禁用 JMX 远程管理
-Dcom.sun.management.jmxremote=false

# 响应头安全（防点击劫持）— 需项目中配置 Spring Security 或手动添加 Filter
# X-Frame-Options: DENY / X-Content-Type-Options: nosniff 等建议在 CorsConfig 或 SecurityConfig 中配置
```

---

## 八、最终推荐启动命令

针对 **grass-ai-agent** 项目，去除无效参数后的生产环境启动命令：

```bash
java \
  -Dspringdoc.api-docs.enabled=false \
  -Dspringdoc.swagger-ui.enabled=false \
  -Dknife4j.production=true \
  -Dserver.error.include-stacktrace=never \
  -Dserver.error.include-message=never \
  -Dserver.error.include-binding-errors=never \
  -Dserver.error.whitelabel.enabled=false \
  -Dcom.sun.management.jmxremote=false \
  -jar grass-ai-agent-1.0.0-SNAPSHOT.jar
```

| 参数 | 作用 |
|------|------|
| `springdoc.api-docs.enabled=false` | 底层禁用 OpenAPI 文档端点 |
| `springdoc.swagger-ui.enabled=false` | 底层禁用 Swagger UI |
| `knife4j.production=true` | Knife4j 生产模式，拦截 `/doc.html` 等所有文档路径 |
| `server.error.include-stacktrace=never` | 错误响应不含堆栈 |
| `server.error.include-message=never` | 错误响应不含异常消息 |
| `server.error.include-binding-errors=never` | 错误响应不含参数校验详情 |
| `server.error.whitelabel.enabled=false` | 禁用默认错误页 |
| `com.sun.management.jmxremote=false` | 禁用 JMX 远程管理 |

---

## 九、老项目场景：Spring Boot 2.x + Springfox 参数评估

> 以下分析针对使用 **Spring Boot 2.x + Springfox（springfox-swagger2 或 springfox-boot-starter）** 的老项目，重新评估原始命令中 7 个参数的有效性。

### 逐参数评估

#### 1. `-Dmanagement.endpoints.enabled-by-default=false`

| 项目 | 结论 |
|------|------|
| **生效条件** | 项目引入了 `spring-boot-starter-actuator` |
| **Spring Boot 2.x 适用性** | ✅ 属性名一致，Spring Boot 2.x / 3.x 通用 |
| **老项目常见情况** | Spring Boot 2.x 老项目**大多会引入 Actuator**（用于健康检查、监控），此参数大概率**有效** |

#### 2. `-Dmanagement.server.port=-1`

| 项目 | 结论 |
|------|------|
| **生效条件** | 项目引入了 `spring-boot-starter-actuator` |
| **Spring Boot 2.x 适用性** | ✅ 属性名一致，2.x / 3.x 通用 |
| **老项目常见情况** | 同上，大概率有效 |

#### 3. `-Dspringdoc.enabled=false`

| 项目 | 结论 |
|------|------|
| **是否生效** | ❌ **无效 — 在任何框架中都不存在此属性** |
| **说明** | 老项目用 Springfox，不用 springdoc；即使用了 springdoc，`springdoc.enabled` 也不是合法属性 |
| **结论** | 此参数在 **任何技术栈组合下都无效** |

#### 4. `-Dspringfox.documentation.enabled=false`

| 项目 | 结论 |
|------|------|
| **是否生效** | ⚠️ **取决于 Springfox 版本** |
| **Springfox 3.0.0+** | ✅ 有效。该属性在 Springfox 3.0.0（2020-07-14 发布）中引入，配合 `springfox-boot-starter` 使用 |
| **Springfox 2.9.x 及更早** | ❌ **无效**。2.9.x 没有此属性，没有自动配置机制。需通过 `@Profile` + `@EnableSwagger2` 条件注解来控制 |

> **关键事实**：大量 Spring Boot 2.x 老项目使用的是 **Springfox 2.9.2**（最广泛使用的版本），而非 3.0.0。如果你的老项目用的是 2.9.x，**此参数无效**。

**Springfox 版本判断方法**：
```xml
<!-- Springfox 2.9.x（无此属性） -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>

<!-- Springfox 3.0.0（有此属性） -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-boot-starter</artifactId>
    <version>3.0.0</version>
</dependency>
```

#### 5. `-Dspring.web.resources.add-mappings=false`

| 项目 | 结论 |
|------|------|
| **是否生效** | ⚠️ **取决于 Spring Boot 2.x 的具体小版本，属性名不同** |

| Spring Boot 版本 | 正确属性名 | `-Dspring.web.resources.add-mappings=false` 是否生效 |
|:---:|------|:---:|
| 2.0 — 2.3 | `spring.resources.add-mappings` | ❌ 不生效（属性名不对） |
| 2.4 — 2.5 | 两者均可（旧名已废弃） | ✅ 生效 |
| 2.6 — 2.7 | `spring.web.resources.add-mappings` | ✅ 生效 |
| 3.x | `spring.web.resources.add-mappings` | ✅ 生效 |

> ⚠️ 如果老项目使用 **Spring Boot 2.0 ~ 2.3**，此参数**因属性名不匹配而无效**，必须改用 `-Dspring.resources.add-mappings=false`。

#### 6. `-Dserver.error.include-stacktrace=never`

| 项目 | 结论 |
|------|------|
| **是否生效** | ✅ **有效** |
| **说明** | 属性名从 Spring Boot 1.x 到 3.x 从未变化，全版本通用 |

#### 7. `-Dcom.sun.management.jmxremote=false`

| 项目 | 结论 |
|------|------|
| **是否生效** | ✅ **有效** |
| **说明** | JVM 层参数，与 Spring Boot 版本无关，所有 JDK 版本通用 |

### Spring Boot 2.x + Springfox 评估总结

| 参数 | Springfox 2.9.x | Springfox 3.0.0 | Boot 2.0–2.3 | Boot 2.4–2.7 |
|------|:---:|:---:|:---:|:---:|
| `management.endpoints.enabled-by-default=false` | ✅* | ✅* | ✅* | ✅* |
| `management.server.port=-1` | ✅* | ✅* | ✅* | ✅* |
| `springdoc.enabled=false` | ❌ | ❌ | ❌ | ❌ |
| `springfox.documentation.enabled=false` | ❌ | ✅ | — | — |
| `spring.web.resources.add-mappings=false` | — | — | ❌† | ✅ |
| `server.error.include-stacktrace=never` | ✅ | ✅ | ✅ | ✅ |
| `com.sun.management.jmxremote=false` | ✅ | ✅ | ✅ | ✅ |

> \* 需引入 Actuator 依赖  
> † Boot 2.0–2.3 应使用 `spring.resources.add-mappings=false`（无 `.web`）

### 老项目推荐启动命令

**场景 1：Spring Boot 2.4+ & Springfox 3.0.0（含 Actuator）**

```bash
java \
  -Dspringfox.documentation.enabled=false \
  -Dspring.web.resources.add-mappings=false \
  -Dmanagement.endpoints.enabled-by-default=false \
  -Dmanagement.server.port=-1 \
  -Dserver.error.include-stacktrace=never \
  -Dserver.error.include-message=never \
  -Dcom.sun.management.jmxremote=false \
  -jar xxx.jar
```

**场景 2：Spring Boot 2.0–2.3 & Springfox 2.9.x（含 Actuator）**

```bash
java \
  -Dspring.resources.add-mappings=false \
  -Dmanagement.endpoints.enabled-by-default=false \
  -Dmanagement.server.port=-1 \
  -Dserver.error.include-stacktrace=never \
  -Dcom.sun.management.jmxremote=false \
  -jar xxx.jar
```

> ⚠️ Springfox 2.9.x 无 `springfox.documentation.enabled` 属性，禁用 Swagger 需在代码中通过 `@Profile("!prod")` 条件控制 `Docket` Bean 的注册：
>
> ```java
> @Configuration
> @Profile("!prod")
> @EnableSwagger2
> public class SwaggerConfig {
>     @Bean
>     public Docket api() {
>         return new Docket(DocumentationType.SWAGGER_2)
>                 .select()
>                 .apis(RequestHandlerSelectors.basePackage("com.example"))
>                 .paths(PathSelectors.any())
>                 .build();
>     }
> }
> ```
>
> 然后生产启动时加 `-Dspring.profiles.active=prod` 即可不加载 Swagger。
