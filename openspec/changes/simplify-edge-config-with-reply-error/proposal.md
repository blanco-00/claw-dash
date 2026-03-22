## Why

当前架构中，task/reply/error 三种边都画成线会导致架构图非常混乱，尤其是 reply 和 error 边通常是从执行者反向汇报给委派者或监督者，路径复杂且难以阅读。

简化方案：**只保留 task 边**，reply/error 作为 task 边的"扩展配置"，在编辑 task 边时同时配置执行者的回复和错误处理目标。

## What Changes

1. **工具栏简化**：移除边类型选择器（linkMode selector），所有边都是 task 边
2. **边详情面板重构**：左侧配置 task（源→目标），右侧配置 reply/error（目标→汇报对象）
3. **同步逻辑调整**：
   - task 块 → 写入源 agent 的 AGENTS.md
   - reply 块 → 写入目标 agent 的 AGENTS.md
   - error 块 → 写入目标 agent 的 AGENTS.md
4. **预览同步**：显示所有受影响的 agent（源和目标）的变更
5. **数据库**：新增 4 个字段存储 reply/error 配置

## Capabilities

### New Capabilities

- `edge-reply-config`: 在编辑 task 边时配置执行者的回复目标
- `edge-error-config`: 在编辑 task 边时配置执行者的错误处理目标

### Modified Capabilities

- `edge-routing-sync`: 同步逻辑需要修改：task 块同步到源 agent，reply/error 块同步到目标 agent

## Impact

- **Frontend**: ConfigGraphTab.vue (移除 linkMode), EdgeDetailPanel.vue (重构 UI)
- **Backend**: ConfigGraphEdge entity (新增字段), AgentsMdSyncService (修改同步逻辑)
- **Database**: 新增 replyTarget, replyTemplate, errorTarget, errorTemplate 字段
- **OpenClaw**: AGENTS.md 同步目标变更
