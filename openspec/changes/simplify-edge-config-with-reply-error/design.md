# Simplify Edge Config — Technical Design

## Context

### Background

当前 multi-agent-routing 实现中，task/reply/error 三种边都作为独立连线存储在数据库。这导致：
1. reply/error 边通常是反向的（从执行者回溯到委派者/监督者），画在图上非常混乱
2. 同一个 task 的 reply/error 配置分散在不同地方，难以管理
3. 用户需要为不同类型的边选择不同的颜色和样式

### New Design

**核心思路**：一条 task 边自带 reply/error 配置，编辑一条边时同时配置：
- 源 agent → 目标 agent 的 task
- 目标 agent → 指定 agent 的 reply（汇报）
- 目标 agent → 指定 agent 的 error（错误上报）

---

## Decisions

### Decision 1: Database Schema

**Choice**: 扩展 `config_graph_edges` 表，新增 4 个字段存储 reply/error 配置。

```sql
ALTER TABLE config_graph_edges 
ADD COLUMN reply_target VARCHAR(100) DEFAULT NULL,
ADD COLUMN reply_template TEXT DEFAULT NULL,
ADD COLUMN error_target VARCHAR(100) DEFAULT NULL,
ADD COLUMN error_template TEXT DEFAULT NULL;
```

**字段说明**：
| 字段 | 说明 |
|------|------|
| reply_target | 执行者完成后回复给哪个 agent |
| reply_template | 回复模板 |
| error_target | 执行者出错时上报给哪个 agent |
| error_template | 错误模板 |

### Decision 2: AGENTS.md Block 位置

**Choice**: 
- task 块 → 写入**源 agent** 的 AGENTS.md
- reply 块 → 写入**目标 agent** 的 AGENTS.md
- error 块 → 写入**目标 agent** 的 AGENTS.md

**示例**：假设 main → jishu 有一条 task 边，同时配置 jishu 回复给 main，jishu 出错上报给 jinyiwei

**main 的 AGENTS.md**:
```markdown
<!-- CLAWDASH:BLOCK id="edge-1" edge_type="task" decision="llm" -->
## [CLAWDASH] Task → jishu (LLM Judged)

Type: 任务 (委托任务)
使用 sessions_send 发送消息。
Message: {original_message}
<!-- CLAWDASH:BLOCK END -->
```

**jishu 的 AGENTS.md**:
```markdown
<!-- CLAWDASH:BLOCK id="edge-1-reply" edge_type="reply" decision="llm" -->
## [CLAWDASH] Reply → main (LLM Judged)

Type: 回复 (完成任务后回复)
使用 sessions_send 发送消息。
Message: 任务完成：{original_message}
<!-- CLAWDASH:BLOCK END -->

<!-- CLAWDASH:BLOCK id="edge-1-error" edge_type="error" decision="llm" -->
## [CLAWDASH] Error → jinyiwei (LLM Judged)

Type: 错误 (发生错误时通知)
使用 sessions_send 发送消息。
Message: 执行出错：{error_message}
<!-- CLAWDASH:BLOCK END -->
```

### Decision 3: Edge ID 命名规则

**Choice**: 
- task 块 ID: `edge-{id}`
- reply 块 ID: `edge-{id}-reply`
- error 块 ID: `edge-{id}-error`

这样可以保证同一 task 边的三个块有关联性。

### Decision 4: Sync 预览分组

**Choice**: 预览按 agent 分组显示变更，每个 agent 显示需要同步的块：

```
main 的 AGENTS.md:
  + edge-1 (new task block)

jishu 的 AGENTS.md:
  + edge-1-reply (new reply block)
  + edge-1-error (new error block)
```

### Decision 5: 前端交互

**Choice**: EdgeDetailPanel 重构为左右两栏：

```
┌────────────────────────────────────────────────┐
│  边配置                            [删除边]     │
├────────────────────────────────────────────────┤
│                                                │
│  ┌─ 源配置 (main) ─┐  ┌─ 目标配置 (jishu) ─┐ │
│  │                  │  │                     │ │
│  │ 【Task 发送】    │  │ 【回复配置】        │ │
│  │                  │  │ ☐ 启用回复          │ │
│  │ 消息模板:        │  │ 回复给: [main  ▼]  │ │
│  │ ┌──────────────┐ │  │ 模板:              │ │
│  │ │{original_    │ │  │ ┌────────────────┐ │ │
│  │ │ message}     │ │  │ │任务完成: ...   │ │ │
│  │ └──────────────┘ │  │ └────────────────┘ │ │
│  │                  │  │                     │ │
│  │ 可用变量:        │  │ 【错误配置】        │ │
│  │ {original_msg}   │  │ ☐ 启用错误处理      │ │
│  │                  │  │ 错误上报: [jinyiwei▼│ │
│  └──────────────────┘  │ 模板:              │ │
│                       │ ┌────────────────┐ │ │
│                       │ │出错: {error_   │ │ │
│                       │ │ message}        │ │ │
│                       │ └────────────────┘ │ │
│                       └───────────────────┘ │
│                                                │
│  [预览]                        [保存]          │
└────────────────────────────────────────────────┘
```

---

## API Design

### Edge Update Payload

```json
{
  "sourceId": "main",
  "targetId": "jishu",
  "edgeType": "task",
  "decisionMode": "llm",
  "messageTemplate": "{original_message}",
  "enabled": true,
  "replyTarget": "main",
  "replyTemplate": "任务完成：{original_message}",
  "errorTarget": "jinyiwei",
  "errorTemplate": "执行出错：{error_message}"
}
```

### SyncPreviewResult 修改

```json
{
  "agents": [
    {
      "agentId": "main",
      "blocksAdded": ["edge-1"],
      "blocksModified": [],
      "blocksRemoved": []
    },
    {
      "agentId": "jishu", 
      "blocksAdded": ["edge-1-reply", "edge-1-error"],
      "blocksModified": [],
      "blocksRemoved": []
    }
  ],
  "totalEdgesSynced": 1
}
```

---

## Frontend Changes

### ConfigGraphTab.vue

- 移除 `linkMode` 状态和工具栏选择器
- 新建边时默认类型为 `task`，添加 reply/error 默认空值

### EdgeDetailPanel.vue

- 重构为左右两栏布局
- 左侧：源 agent 的 task 配置
- 右侧：目标 agent 的 reply/error 配置
- 移除边类型单选按钮（固定为 task）
- 移除决策模式选择（固定为 llm）

---

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| reply/error 配置后未填写目标 | 块不生成 | 前端校验或后端跳过空配置 |
| 目标 agent 不存在 | 同步失败 | 验证 agent 存在性 |
| 现有数据迁移 | 需要迁移脚本 | V6 migration 添加新字段 |
