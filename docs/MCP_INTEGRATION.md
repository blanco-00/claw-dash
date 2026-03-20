# ClawDash MCP Server 集成方案

> 更新日期：2026-03-20

## 背景

OpenClaw 项目有一个内置的任务队列插件 (`@openclaw-task-queue/core`)，基于 SQLite 实现。原有的集成方案存在以下问题：

1. OpenClaw 需要维护独立的 task-queue 插件
2. ClawDash 和 OpenClaw 各自有独立的任务队列实现
3. 两边数据不同步，UI 展示不一致

## 目标

让 **ClawDash 本身成为 MCP Server**，通过 MCP 协议向 OpenClaw 提供工具，实现：

1. **统一任务队列** - ClawDash 管理所有任务，OpenClaw 通过 MCP 调用
2. **一次性配置** - 用户只需配置一次 MCP URL，未来自动获得所有新工具
3. **扩展性强** - 未来增加功能只需添加 `@McpTool`，无需修改 OpenClaw 配置

---

## 架构设计

```
┌─────────────────────────────────────────────────────────────────────┐
│                         ClawDash Backend                            │
│                         (MCP Server)                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │              MCP Tools (Spring AI)                          │   │
│  │                                                             │   │
│  │  @McpTool("task_queue")      @McpTool("future_feature")   │   │
│  │  ├── task_create              ├── future_tool1            │   │
│  │  ├── task_list               ├── future_tool2            │   │
│  │  ├── task_status             ├── future_tool3            │   │
│  │  ├── task_complete                                      │   │
│  │  ├── task_fail                                          │   │
│  │  └── task_stats                                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              │                                     │
│                    Spring AI MCP Server                            │
│                              │                                     │
│              SSE Transport: /sse + /mcp/message                    │
│                              │                                     │
└──────────────────────────────┼─────────────────────────────────────┘
                                │
                                ▼
                     OpenClaw Agent
                    
                    配置：
                    "mcp": { "servers": { "clawdash": { "url": "http://localhost:5178/sse" } } }
                    
                    未来自动获得所有新工具！
```

## 当前状态

✅ **已完成**：MCP Server 已成功运行，6 个工具已注册

| 组件 | 状态 | 说明 |
|------|------|------|
| Spring AI MCP 依赖 | ✅ | spring-ai-starter-mcp-server-webmvc |
| MCP 配置 | ✅ | application.yml 正确配置 |
| Security 配置 | ✅ | /sse/** 和 /mcp/** 已放行 |
| 工具注册 | ✅ | 6 个工具已注册 |
| SSE 端点 | ✅ | /sse 返回 sessionId |
| 消息端点 | ✅ | /mcp/message 处理请求 |

---

## 技术选型

### 为什么选择 MCP 协议？

| 对比维度 | 纯 CLI + Skills | MCP Server | OpenClaw 插件 |
|---------|-----------------|------------|---------------|
| Token 消耗 | ⭐⭐⭐⭐⭐ (最低) | ⭐⭐ (较高) | ⭐⭐⭐⭐⭐ |
| 可靠性 | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| 标准化 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ |
| 生态兼容 | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| 开发工作量 | 低 | 中 | 高 |

**选择 MCP 的原因**：
1. **行业趋势** - 12,000+ MCP 服务器，主流厂商全部支持（GitHub、Stripe、Notion 等）
2. **6 个月增长率 232%** - 已成事实标准
3. **一次配置** - 未来增加工具自动被发现，无需改配置
4. **标准化** - 工具发现、Schema 定义、类型安全

### 为什么选择 Spring AI MCP？

ClawDash 是 Java Spring Boot 项目，Spring AI 官方提供 MCP 支持：

```java
@SpringBootApplication
public class McpServerConfig {
    public static void main(String[] args) {
        SpringApplication.run(McpServerConfig.class, args);
    }
}

@Component
public class TaskQueueMcpTools {

    @McpTool(name = "task_create", description = "创建新任务到任务队列")
    public TaskDto createTask(
        @McpToolParam(description = "任务类型", required = true) String type,
        @McpToolParam(description = "任务载荷 JSON") Map<String, Object> payload,
        @McpToolParam(description = "优先级 1-10") Integer priority
    ) {
        return taskQueueService.create(...);
    }
}
```

---

## 实现计划

### Phase 1：MCP Server 基础能力 ✅

| 任务 | 描述 | 状态 |
|------|------|------|
| 添加 Spring AI MCP 依赖 | pom.xml 添加 spring-ai-starter-mcp-server-webmvc | ✅ |
| 配置 MCP Server | application.yml 配置 sse-endpoint | ✅ |
| 暴露 TaskQueue Tools | @McpTool 注解暴露 TaskQueueService 方法 | ✅ |
| Security 配置 | 放行 /sse/** 和 /mcp/** 路径 | ✅ |
| 修复依赖问题 | Lombok 1.18.40, Spring Boot 3.4.0 | ✅ |
| 编译测试 | ✅ 通过 | ✅ |

### Phase 2：OpenClaw 集成

| 任务 | 描述 | 状态 |
|------|------|------|
| 一键配置 MCP | 前端添加「查看 MCP 配置」按钮 | ⏳ |
| 生成配置代码 | 自动生成 OpenClaw 配置 JSON | ⏳ |
| 插件清理 | 移除 @openclaw-task-queue/core 和 privy-jiner | ⏳ |

### Phase 3：功能完善

| 任务 | 描述 | 状态 |
|------|------|------|
| 补充缺失 API | 添加 task_cancel、task_retry 等 | ⏳ |
| MCP 认证 | 添加 API Key 认证 | ⏳ |
| 日志记录 | 记录 MCP 工具调用日志 | ⏳ |

---

## 测试验证

### 手动测试

```bash
# 1. 启动后端
cd backend && mvn spring-boot:run

# 2. 测试 SSE 端点
curl -s -N -H "Accept: text/event-stream" http://localhost:5178/sse

# 返回: event:endpoint
#       data:/mcp/message?sessionId=xxx
```

### MCP Inspector 测试

```bash
npx @modelcontextprotocol/inspector --transport sse --server-url http://localhost:5178/sse
```

### 已知问题

- SSE session 有时效性，需要先连接 `/sse` 获取 sessionId
- 部分 MCP 客户端可能需要 Streamable HTTP 支持（未来可选）

---

## 环境要求

### 当前状态

- **代码已完成**：所有 MCP 相关代码已编写完成
- **需要环境配置**：需要以下环境才能编译运行：

### 依赖要求

| 组件 | 要求 | 说明 |
|------|------|------|
| Java | 17+ | 当前环境 Java 25，需要 Java 17 |
| Spring Boot | 3.4+ | MCP Server 需要 |
| Lombok | 1.18.36+ | Java 25 兼容性 |

### 启用 MCP Server

MCP Server 通过以下依赖启用：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>

<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations</artifactId>
    <version>0.8.0</version>
</dependency>
```

### 配置说明

`application.yml` 中的 MCP 配置：

```yaml
spring:
  ai:
    mcp:
      server:
        name: clawdash
        version: 1.0.0
        type: SYNC
        protocol: SSE
        sse-endpoint: /sse
        sse-message-endpoint: /mcp/message
        capabilities:
          tool: true
```

**关键配置项**：
- `protocol: SSE` - 使用 SSE 传输协议
- `sse-endpoint: /sse` - SSE 连接端点
- `sse-message-endpoint: /mcp/message` - 消息处理端点

---

## OpenClaw 配置示例

用户只需在 OpenClaw 配置文件中添加：

```json
{
  "mcp": {
    "servers": {
      "clawdash": {
        "url": "http://localhost:5178/sse"
      }
    }
  }
}
```

> **注意**：使用 SSE 传输协议，URL 指向 `/sse` 端点。

配置完成后，OpenClaw 会自动发现以下工具：

| 工具名称 | 描述 |
|---------|------|
| task_create | 创建新任务 |
| task_list | 获取任务列表 |
| task_status | 获取任务状态 |
| task_complete | 标记任务完成 |
| task_fail | 标记任务失败 |
| task_cancel | 取消任务 |
| task_stats | 获取任务统计 |

---

## 相关文件

### 后端

- `pom.xml` - Maven 依赖配置
- `src/main/java/com/clawdash/config/SecurityConfig.java` - Security 配置（/sse/**, /mcp/** 放行）
- `src/main/java/com/clawdash/mcp/TaskQueueMcpTools.java` - TaskQueue MCP 工具类（6个工具）
- `src/main/resources/application.yml` - MCP 端点配置
- `TaskQueueService.java` - 现有任务队列服务

### 前端

- `frontend/src/views/OpenClaw.vue` - OpenClaw 管理页面（MCP 配置查看功能）

### 外部参考

- [Spring AI MCP 官方文档](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-examples.html)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [MCP 官方规范](https://spec.modelcontextprotocol.io/)

---

## 附录：MCP vs CLI 对比数据

> 以下数据来自 Scalekit 75 次基准测试

| 指标 | CLI | MCP | 结论 |
|------|-----|-----|------|
| Token 消耗 | 1,365 | 44,026 | CLI 便宜 32× |
| 成功率 | 100% | 72% | CLI 更可靠 |
| 延迟 | <50ms | 连接建立耗时 | CLI 更快 |

**为什么还要选 MCP？**
- MCP 解决的是**企业级问题**：多租户 OAuth、审计日志、租户隔离
- 对于个人/内部工具，CLI 确实更高效
- 但 MCP 的**标准化和生态**是其核心价值

---

## 更新日志

- **2026-03-20**: 创建文档，记录 MCP Server 集成方案
- **2026-03-20**: 实现 Phase 1 & 2
  - 添加 Spring AI MCP 依赖
  - 创建 McpServerConfig 配置类
  - 配置 application.yml MCP 端点
  - 实现 TaskQueueMcpTools（6个工具）
  - 前端添加 MCP 配置查看和复制功能
  - 清理 OpenClaw 旧插件配置
- **2026-03-20**: 修复配置问题
  - 修复 application.yml 属性名（sse-endpoint, sse-message-endpoint）
  - 添加 SecurityConfig /sse/** 和 /mcp/** 放行
  - 切换到 spring-ai-starter-mcp-server-webmvc
  - MCP Server 成功运行，6 工具已注册
