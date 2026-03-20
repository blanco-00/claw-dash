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
│  │  ├── task_cancel                                         │   │
│  │  └── task_stats                                          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              │                                     │
│                    Spring AI MCP Server                            │
│                              │                                     │
│                    SSE / HTTP  (MCP Protocol)                     │
│                              │                                     │
└──────────────────────────────┼─────────────────────────────────────┘
                               │
                               ▼
                    OpenClaw Agent
                    
                    一次性配置：
                    "mcp": { "servers": { "clawdash": { "url": "http://localhost:5178/mcp" } } }
                    
                    未来自动获得所有新工具！
```

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

### Phase 1：MCP Server 基础能力

| 任务 | 描述 | 状态 |
|------|------|------|
| 添加 Spring AI MCP 依赖 | 在 pom.xml 中添加 spring-ai-mcp-server-starter | ⏳ |
| 配置 MCP Server | 创建配置类，启用 MCP 端点 | ⏳ |
| 暴露 TaskQueue Tools | 用 @McpTool 注解暴露现有 TaskQueueService 方法 | ⏳ |
| 本地测试 MCP 连接 | 用 MCP Inspector 验证工具可用 | ⏳ |

### Phase 2：OpenClaw 集成

| 任务 | 描述 | 状态 |
|------|------|------|
| 一键配置 MCP | 前端添加「连接 ClawDash MCP」按钮 | ⏳ |
| 生成配置代码 | 自动生成 OpenClaw 配置 JSON | ⏳ |
| 测试任务创建 | 从 OpenClaw 调用 task_create 工具 | ⏳ |
| 测试任务查询 | 从 OpenClaw 调用 task_list 工具 | ⏳ |

### Phase 3：功能完善

| 任务 | 描述 | 状态 |
|------|------|------|
| 补充缺失 API | 添加 task_cancel、task_retry 等 | ⏳ |
| MCP 认证 | 添加 API Key 认证 | ⏳ |
| 日志记录 | 记录 MCP 工具调用日志 | ⏳ |

---

## OpenClaw 配置示例

用户只需在 OpenClaw 配置文件中添加：

```json
{
  "mcp": {
    "servers": {
      "clawdash": {
        "url": "http://localhost:5178/mcp"
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
- `McpServerConfig.java` - MCP Server 配置类（待创建）
- `TaskQueueMcpTools.java` - TaskQueue MCP 工具类（待创建）
- `TaskQueueService.java` - 现有任务队列服务

### 前端

- `OpenClaw.vue` - OpenClaw 管理页面（添加 MCP 配置功能）

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
