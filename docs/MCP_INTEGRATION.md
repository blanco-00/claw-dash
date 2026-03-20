# ClawDash MCP Server 集成方案

> 更新日期：2026-03-20

## ⚠️ 重要更新 (2026-03-20)

**OpenClaw 2026.3.13 尚未支持 MCP Server 配置！**

尝试写入 `mcpServers` 配置会导致 OpenClaw 启动失败并报错：
```
Unrecognized keys: "mcp", "mcpServers"
```

**当前状态：**
- ❌ OpenClaw 一键配置功能**暂停使用**
- ✅ MCP Server 端（ClawDash）**已就绪**
- ⏳ 等待 OpenClaw 官方支持：[Issue #43509](https://github.com/openclaw/openclaw/issues/43509)

---

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
                     
                     配置（暂不支持）：
                     "mcp": { "servers": { "clawdash": { "url": "http://localhost:5178/sse" } } }
                     
                     未来自动获得所有新工具！
```

---

## 当前状态

| 组件 | 状态 | 说明 |
|------|------|------|
| Spring AI MCP 依赖 | ✅ | spring-ai-starter-mcp-server-webmvc |
| MCP 配置 | ✅ | application.yml 正确配置 |
| Security 配置 | ✅ | /sse/** 和 /mcp/** 已放行 |
| 工具注册 | ✅ | 6 个工具已注册 |
| SSE 端点 | ✅ | /sse 返回 sessionId |
| 消息端点 | ✅ | /mcp/message 处理请求 |
| OpenClaw 端配置 | ❌ | **OpenClaw 不支持 mcpServers** |

---

## 临时方案：OpenClaw Skill + REST API

由于 OpenClaw 尚未支持 MCP Server 配置，我们创建了一个 **OpenClaw Skill** 作为临时方案：

**位置**: `~/.openclaw/workspace/skills/clawdash/SKILL.md`

该 Skill 提供：
- ClawDash REST API 调用示例
- 任务 CRUD 操作指南
- 任务状态追踪方法

### 使用方法

OpenClaw Agent 可以通过以下方式调用 ClawDash：

```bash
# 创建任务
curl -X POST http://localhost:5178/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"type": "agent_task", "payload": {...}, "priority": 5}'

# 查询任务
curl http://localhost:5178/api/tasks

# 标记完成
curl -X PUT http://localhost:5178/api/tasks/{id}/complete
```

---

## 未来计划

### 等待 OpenClaw MCP 支持

| 里程碑 | 状态 | 链接 |
|--------|------|------|
| Issue #43509 - MCP Server 配置支持 | 🟡 Open | [GitHub](https://github.com/openclaw/openclaw/issues/43509) |
| PR #44916 - MCP Server 实现 | 🟡 Open (待合并) | [GitHub](https://github.com/openclaw/openclaw/pull/44916) |

**预计支持格式**（基于 PR #44916）：
```json
{
  "mcp": {
    "servers": {
      "clawdash": {
        "url": "http://localhost:5178/sse",
        "transport": "sse"
      }
    }
  }
}
```

### 实现计划

#### Phase 1：MCP Server 基础能力 ✅

| 任务 | 描述 | 状态 |
|------|------|------|
| 添加 Spring AI MCP 依赖 | pom.xml 添加 spring-ai-starter-mcp-server-webmvc | ✅ |
| 配置 MCP Server | application.yml 配置 sse-endpoint | ✅ |
| 暴露 TaskQueue Tools | @McpTool 注解暴露 TaskQueueService 方法 | ✅ |
| Security 配置 | 放行 /sse/** 和 /mcp/** 路径 | ✅ |
| 修复依赖问题 | Lombok 1.18.40, Spring Boot 3.4.0 | ✅ |
| 编译测试 | ✅ 通过 | ✅ |

#### Phase 2：OpenClaw 集成（等待中）

| 任务 | 描述 | 状态 |
|------|------|------|
| 一键配置 MCP | 前端添加「一键配置」按钮 | ✅ 已实现 |
| OpenClaw 配置支持 | **等待 OpenClaw 更新** | ❌ 阻塞 |
| 插件清理 | 移除 @openclaw-task-queue/core 和 privy-jiner | ⏳ |

#### Phase 3：功能完善

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
- **OpenClaw 不支持 mcpServers 配置** - 需要等待官方更新

---

## 环境要求

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

## OpenClaw 配置（未来支持）

> ⚠️ **暂不可用** - 需要 OpenClaw 更新支持

用户只需在 OpenClaw 配置文件中添加：

```json
{
  "mcp": {
    "servers": {
      "clawdash": {
        "url": "http://localhost:5178/sse",
        "transport": "sse"
      }
    }
  }
}
```

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

- `frontend/src/views/OpenClaw.vue` - OpenClaw 管理页面（显示等待状态）

### OpenClaw Skill

- `~/.openclaw/workspace/skills/clawdash/SKILL.md` - ClawDash REST API 集成指南

### 外部参考

- [Spring AI MCP 官方文档](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-examples.html)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [MCP 官方规范](https://spec.modelcontextprotocol.io/)
- [OpenClaw Issue #43509](https://github.com/openclaw/openclaw/issues/43509) - MCP 配置支持请求
- [OpenClaw PR #44916](https://github.com/openclaw/openclaw/pull/44916) - MCP 配置实现

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
- **2026-03-20**: 发现 OpenClaw 不支持 mcpServers 配置
  - 移除一键配置功能（会导致 OpenClaw 启动失败）
  - 前端 UI 改为显示「等待 OpenClaw 更新」
  - 创建 ClawDash Skill 作为临时方案
  - 文档更新：标注 Phase 2 等待状态
