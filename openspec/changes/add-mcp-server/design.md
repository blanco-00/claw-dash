## Context

ClawDash 是基于 OpenClaw 的多 Agent 可视化管理系统。当前 OpenClaw 使用独立的 task-queue 插件（SQLite），ClawDash 也有自己的任务队列实现（MySQL + Redis），两者数据不同步。

目标是将 ClawDash 本身变成 MCP Server，通过 MCP 协议向 OpenClaw 提供工具。

**当前状态：**
- ClawDash Backend: Spring Boot 3.2.0, Java 17
- ClawDash Frontend: Vue 3 + Element Plus
- OpenClaw: 运行在 18789 端口，有 MCP 客户端支持

## Goals / Non-Goals

**Goals:**
1. ClawDash Backend 成为 MCP Server，监听 `/mcp` 端点
2. 通过 MCP 协议暴露 TaskQueue 工具（task_create, task_list 等）
3. 前端添加一键配置 MCP 的功能
4. 用户只需配置一次，未来增加工具自动被发现

**Non-Goals:**
- 不修改 OpenClaw 核心代码
- 不迁移现有的 OpenClaw task-queue 插件数据
- 初期不添加 MCP 认证（后续可选）

## Decisions

### 1. 为什么选择 Spring AI MCP 而不是自实现？

**决定：** 使用 Spring AI MCP 官方 SDK

**理由：**
- 官方维护，支持 Spring Boot 自动配置
- 支持 SSE 和 STDIO 两种传输协议
- 提供 `@McpTool` 注解，声明式暴露方法
- 与现有 Java 后端无缝集成

**替代方案考虑：**
- 自实现 MCP 协议 → 工作量大，需要完整实现 JSON-RPC
- 使用 Node.js 独立服务 → 需要额外进程，与现有架构不一致

### 2. MCP 传输协议选择 SSE 还是 STDIO？

**决定：** 使用 SSE (Server-Sent Events) over HTTP

**理由：**
- ClawDash 已有 HTTP 服务，增加端点即可
- STDIO 适合本地 CLI 工具，但需要单独进程
- OpenClaw 的 MCP 配置支持 HTTP URL

**替代方案考虑：**
- STDIO → 需要用户启动独立进程，不够优雅

### 3. MCP Server 端点路径

**决定：** `/mcp`

**理由：**
- Spring AI MCP 默认路径
- 简洁明了

### 4. 如何组织 MCP Tools 代码？

**决定：** 创建一个 `mcp` 包，包含：
- `McpServerConfig.java` - MCP Server 配置
- `TaskQueueMcpTools.java` - TaskQueue 相关工具

**理由：**
- 与现有业务代码分离
- 未来增加新工具只需在该包下添加类

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Spring AI MCP 版本不稳定 | 可能需要适配 | 使用稳定版本，关注官方 Release |
| MCP 连接失败 | OpenClaw 无法调用工具 | 提供错误提示和重试机制 |
| Token 消耗增加 | MCP 比 CLI 消耗更多 Token | 初期可接受，后续优化 |

## Migration Plan

1. **开发阶段：**
   - 添加 Maven 依赖
   - 创建 MCP 配置类
   - 创建 MCP Tools 类
   - 本地测试 MCP 连接

2. **部署阶段：**
   - 部署新版 ClawDash Backend
   - 用户在 OpenClaw 配置中添加 MCP URL

3. **回滚：**
   - 移除 MCP 配置，OpenClaw 回退到原有工具

## Open Questions

1. MCP 认证机制？ - 初期不加，后续根据需要添加
2. 是否需要 MCP 日志？ - 后续可选
