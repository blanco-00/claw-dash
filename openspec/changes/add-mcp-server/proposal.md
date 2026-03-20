## Why

当前 OpenClaw 使用独立的 task-queue 插件，ClawDash 也有自己的任务队列实现，两者数据不同步。更好的方案是让 **ClawDash 本身成为 MCP Server**，通过 MCP 协议向 OpenClaw 提供工具。这样可以实现：

1. 统一的任务队列管理
2. 一次性配置，未来增加工具自动被发现
3. 遵循行业标准 MCP 协议，接入成本低

## What Changes

- 在 ClawDash Backend 添加 Spring AI MCP 依赖
- 创建 MCP Server 配置类，启用 `/mcp` 端点
- 创建 `@McpTool` 注解的 TaskQueue 工具类
- 前端 OpenClaw 页面添加「一键配置 MCP」按钮
- 用户配置后，OpenClaw 可自动发现 ClawDash 提供的所有工具

## Capabilities

### New Capabilities

- `mcp-server`: 让 ClawDash 本身成为 MCP Server，通过 MCP 协议向外部（包括 OpenClaw）提供工具

### Modified Capabilities

- (无) 现有功能需求不变

## Impact

- **Backend**: 添加 Spring AI MCP 依赖，创建 MCP 工具类
- **Frontend**: OpenClaw 页面添加 MCP 配置功能
- **OpenClaw**: 用户需要在配置中添加 MCP Server URL
