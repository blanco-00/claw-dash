# Multi-Agent Routing — Technical Design

## Context

### Background

ClawDash Config Graph visualizes OpenClaw multi-agent topology as nodes (agents) and edges (A2A relationships). The graph currently stores topology in MySQL but has no effect on OpenClaw agent behavior. This design enables Config Graph edges to drive actual inter-agent communication via OpenClaw's `sessions_send` tool.

### Current State

- Config Graph stores nodes (references to OpenClaw agents) and edges (A2A relationships with type: assigns/reports_to/communicates) in MySQL
- OpenClaw agents are managed via CLI (`openclaw agents add/delete`)
- Agent communication uses `sessions_send` but requires manual configuration in `AGENTS.md`
- ClawDash can read/write agent workspace files via OpenClawService

### Constraints

- OpenClaw `AGENTS.md` may be manually edited — ClawDash must not destroy manual changes
- Agents may have multiple workspace files — only `AGENTS.md` is modified
- Sync is one-way: Config Graph → OpenClaw (not bidirectional)
- No support for sub-agents or spawned sessions (only named agents via `openclaw agents add`)

---

## Decisions

### Decision 1: Edge Data Model

**Choice**: Extend `config_graph_edges` table with routing fields. Edge type values stored in a manageably table.

**Edge Type Enum** (stored in `edge_types` table):

| Value | Name | Description |
|-------|------|-------------|
| `always` | 始终 | 无条件发送，收到就发 |
| `task` | 任务 | 委托任务给目标 agent |
| `reply` | 回复 | 完成任务/收到结果后回复给某 agent |
| `error` | 错误 | 发生错误时通知某 agent |

**Decision Mode** (how the agent decides to send):
- `always` — 直接发送，不需判断
- `llm` — 由 AI 判断是否发送，以及发送什么内容

**Schema**:
```sql
ALTER TABLE config_graph_edges ADD COLUMN edge_type VARCHAR(20) NOT NULL DEFAULT 'task';
ALTER TABLE config_graph_edges ADD COLUMN decision_mode VARCHAR(20) NOT NULL DEFAULT 'always';
ALTER TABLE config_graph_edges ADD COLUMN message_template TEXT NOT NULL DEFAULT '';
ALTER TABLE config_graph_edges ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;
```

**New `edge_types` table**:
```sql
CREATE TABLE edge_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  value VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO edge_types (value, name, description) VALUES
  ('always', '始终', '无条件发送'),
  ('task', '任务', '委托任务给目标 agent'),
  ('reply', '回复', '完成任务后回复给某 agent'),
  ('error', '错误', '发生错误时通知某 agent');
```

**Rationale**: Separating edge_type (semantic intent) from decision_mode (how to decide) allows flexible combinations. Both are stored in manageably tables for extensibility.

### Decision 2: AGENTS.md Block Format

**Choice**: Use `<!-- CLAWDASH:BLOCK -->` markers with UUID-based ids.

**Format**:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="{edge_type}" decision="{decision_mode}" -->
## [CLAWDASH] {edge_type} → {target_agent}

Type: {edge_type} ({edge_type_description})
Decision: {decision_mode_description}
Message: {message_template}

<!-- CLAWDASH:BLOCK END -->
```

**Block Examples by Edge Type**:

`task` + `always`:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="task" decision="always" -->
## [CLAWDASH] Task → test

Type: 任务 (委托任务)
Decision: 直接发送，无条件
Message: 请执行测试: {original_message}

<!-- CLAWDASH:BLOCK END -->
```

`task` + `llm`:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="task" decision="llm" -->
## [CLAWDASH] Task → test (LLM Judged)

Type: 任务 (委托任务)
Decision: 由 AI 判断是否发送此任务，以及发送什么内容
如果判断需要发送给 test，使用 sessions_send 发送任务消息。
Message template: {original_message}

<!-- CLAWDASH:BLOCK END -->
```

`reply` + `always`:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="reply" decision="always" -->
## [CLAWDASH] Reply → main

Type: 回复 (完成任务后回复)
Decision: 直接发送，无条件
Message: 任务完成: {task_result}

<!-- CLAWDASH:BLOCK END -->
```

`error` + `always`:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="error" decision="always" -->
## [CLAWDASH] Error → main

Type: 错误 (发生错误时通知)
Decision: 直接发送，无条件
Message: 执行出错: {error_message}

<!-- CLAWDASH:BLOCK END -->
```

**Rationale**:
- UUID-based ids guarantee uniqueness across all edges
- Block edge_type + decision_mode clearly visible
- `error` edge's block lives in the **failing agent's** AGENTS.md
- LLM decision mode provides high-level instruction, letting the model craft the message

### Decision 3: Sync Algorithm

**Choice**: Per-agent AGENTS.md rewrite with block-level diff.

**Algorithm**:
```
For each source_agent in edges.groupBy(source):
  1. Read current ~/.openclaw/agents/{source}/AGENTS.md
  2. Parse existing CLAWDASH blocks → map of id → block
  3. For each edge from source:
     a. Generate new block content
     b. If id exists in map → update block
     c. If id not in map → append new block
  4. Remove blocks for edges that no longer exist or are disabled
  5. Write back to AGENTS.md
```

**Note**: `error` edges are synced to the **target agent's** AGENTS.md (the one that may fail), not the source.

**Conflict Resolution**:
- Blocks are identified by edge UUID
- Edges deleted in Config Graph → block removed on next sync
- Edge modified in Config Graph → block updated on next sync
- Edge disabled → block removed on next sync
- Manual changes outside blocks → preserved

### Decision 4: Message Template Variables

**Supported Variables**:
| Variable | Expands To |
|----------|------------|
| `{original_message}` | The message that triggered the routing decision |
| `{task_result}` | The result from the target agent (for reply edges) |
| `{error_message}` | Error description (for error edges) |
| `{source_id}` | Source agent ID |
| `{target_id}` | Target agent ID |

**Variable Substitution**: Done by OpenClaw agent in `sessions_send` call, or by prompt instruction for `llm` decision mode.

**Note**: For `llm` decision mode, the template is used as a hint — the agent crafts the actual message content based on context.

### Decision 5: Sync Triggering

**Choice**: Manual sync button + optional auto-sync on edge save.

**Behavior**:
- "Sync to OpenClaw" button triggers full sync
- Preview dialog shows diff before writing
- Optional: Auto-sync when edge is created/modified (configurable)

---

## Database Schema

### Extended `config_graph_edges`

```sql
CREATE TABLE config_graph_edges (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  graph_id BIGINT NOT NULL,
  source_agent_id VARCHAR(100) NOT NULL,
  target_agent_id VARCHAR(100) NOT NULL,
  edge_type VARCHAR(20) NOT NULL DEFAULT 'task',
  decision_mode VARCHAR(20) NOT NULL DEFAULT 'always',
  message_template TEXT NOT NULL DEFAULT '',
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (graph_id) REFERENCES config_graphs(id) ON DELETE CASCADE,
  INDEX idx_source (source_agent_id),
  INDEX idx_target (target_agent_id)
);
```

### New `edge_types`

```sql
CREATE TABLE edge_types (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  value VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Note**: `decision_mode` is not a separate table (always/llm are the only two modes currently).

---

## API Design

### Edge Management (extends existing)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `PATCH` | `/api/config-graphs/edges/{id}` | Update edge with routing fields |
| `POST` | `/api/config-graphs/edges` | Create edge with routing fields |
| `GET` | `/api/config-graphs/edges/{id}` | Get edge with routing details |

### Edge Types

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/edge-types` | List all edge types |
| `POST` | `/api/edge-types` | Add new edge type |
| `PATCH` | `/api/edge-types/{id}` | Update edge type |
| `DELETE` | `/api/edge-types/{id}` | Delete edge type (if not used) |

### Sync

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/config-graphs/{id}/sync-preview` | Preview AGENTS.md changes (diff) |
| `POST` | `/api/config-graphs/{id}/sync` | Execute sync to OpenClaw |

**Sync Response**:
```json
{
  "agentsUpdated": ["main", "test", "check"],
  "edgesSynced": 5,
  "blocksAdded": 3,
  "blocksUpdated": 2,
  "blocksRemoved": 0,
  "errors": []
}
```

---

## Frontend Design

### Edge Detail Panel (Sidebar)

```
┌────────────────────────────────────────────────┐
│  边详情                               [删除边]  │
├────────────────────────────────────────────────┤
│  从: [main ▼] ──────────────► 到: [test ▼]   │
│                                                │
│  边类型:                                        │
│  ○ 始终 (always)                               │
│  ● 任务 (task)                                 │
│  ○ 回复 (reply)                                 │
│  ○ 错误 (error)                                 │
│                                                │
│  决策模式:                                      │
│  ● 直接发送 (always)                           │
│  ○ AI 判断 (llm) - 由AI决定是否发送及内容     │
│                                                │
│  消息模板:                                      │
│  ┌──────────────────────────────────────────┐ │
│  │ 请执行测试: {original_message}             │ │
│  └──────────────────────────────────────────┘ │
│                                                │
│  可用变量: {original_message}, {task_result},  │
│            {error_message}, {source_id}        │
│                                                │
│  ☐ 启用此路由                                  │
│                                                │
│  [预览 Sync 效果]               [保存]         │
└────────────────────────────────────────────────┘
```

### Sync Preview Dialog

```
┌────────────────────────────────────────────────────┐
│  Sync Preview — 将同步到 OpenClaw                  │
├────────────────────────────────────────────────────┤
│                                                      │
│  main 的 AGENTS.md (2 个块)                         │
│  ┌────────────────────────────────────────────┐    │
│  │ + <!-- CLAWDASH:BLOCK id="e1" -->          │    │
│  │ + ## [CLAWDASH] Task → test               │    │
│  │ + Type: 任务                               │    │
│  │ + Decision: AI 判断                        │    │
│  │ + Message: 请执行测试: {original_message}  │    │
│  │ + <!-- CLAWDASH:BLOCK END -->              │    │
│  │                                              │    │
│  │ ~ <!-- CLAWDASH:BLOCK id="e2" -->          │    │ (modified)
│  │ ~ ## [CLAWDASH] Reply → main               │    │
│  └────────────────────────────────────────────┘    │
│                                                      │
│  test 的 AGENTS.md (1 个块)                         │
│  ┌────────────────────────────────────────────┐    │
│  │ + <!-- CLAWDASH:BLOCK id="e3" -->          │    │
│  │ + ## [CLAWDASH] Error → main               │    │
│  │ + Type: 错误                               │    │
│  │ + Decision: 直接发送                        │    │
│  └────────────────────────────────────────────┘    │
│                                                      │
│  check 的 AGENTS.md (无变更)                        │
│                                                      │
│            [取消]              [执行 Sync]           │
└────────────────────────────────────────────────────┘
```

Legend: `+` new, `~` modified, `-` removed

### Edge Type Management (Optional P2)

```
┌────────────────────────────────────────────────┐
│  边类型管理                                     │
├────────────────────────────────────────────────┤
│  + 添加新类型                                  │
│                                                │
│  ┌──────┬─────────────┬────────────────────┐   │
│  │ 值   │ 名称        │ 说明                │   │
│  ├──────┼─────────────┼────────────────────┤   │
│  │ always│ 始终       │ 无条件发送          │   │
│  │ task  │ 任务       │ 委托任务            │   │
│  │ reply │ 回复       │ 完成任务后回复      │   │
│  │ error │ 错误       │ 发生错误时通知      │   │
│  └──────┴─────────────┴────────────────────┘   │
└────────────────────────────────────────────────┘
```

---

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| AGENTS.md parse failure | Corrupt block structure | Read existing blocks via regex, backup before write |
| Edge references non-existent agent | Sync writes to wrong path | Validate source/target exist via `openclaw agents list` |
| OpenClaw reloads AGENTS.md mid-write | Partial file state | Atomic write: temp file + rename |
| Multiple ClawDash instances sync same agent | Race condition | Lock per-agent during sync, or warn user |
| LLM judge produces wrong routing | Infinite loops, missed tasks | Include loop prevention in prompt, max depth |

---

## Open Questions

1. **LLM decision prompt**: What exact instructions should go in the AGENTS.md block for `llm` decision mode? Needs careful prompt engineering to avoid over-sending or under-sending.

2. **Error edge location**: `error` edge is placed in the target agent's AGENTS.md (the one that may fail). Is this correct, or should the source agent also have an error-handling block?

3. **Sync conflict with manual edits**: If user manually edits a CLAWDASH block, ClawDash will overwrite on next sync. Should we warn?

4. **Loop prevention**: A→B→C→A is possible. Should we detect cycles in Config Graph and warn before sync?
