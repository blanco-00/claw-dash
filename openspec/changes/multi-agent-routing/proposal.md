# Multi-Agent Routing — Config Graph to OpenClaw Sync

## Why

ClawDash already visualizes multi-agent topology via Config Graph (nodes = OpenClaw agents, edges = A2A relationships). However, edges are purely decorative — they define **intent** but don't affect agent behavior. Users want: **draw a line in the UI → agents actually communicate**.

The goal is to make Config Graph a **zero-code orchestration layer** for OpenClaw. Users draw edges, define trigger conditions, and ClawDash generates the corresponding `AGENTS.md` routing rules. OpenClaw agents then execute the workflow automatically.

## What Changes

### Core Capability: Edge-Based Agent Communication

Users define directed edges between agents with edge type and decision mode. When triggered, the source agent sends a message to the target agent via OpenClaw's `sessions_send`.

**Example workflow:**
```
User → "帮我测试这个功能"
         ↓
    main (task edge to test, llm decision → AI decides to send)
         ↓ sessions_send
    test agent (executes test)
         ↓ (on completion, reply edge to main, always)
    test → main (notifies completion)
         ↓
    main → User ("测试完成")
```

**Another example (test fails):**
```
test agent (executes test, error occurs)
         ↓ (error edge to main, always)
    test → main (notifies error)
         ↓
    main → User ("测试失败: ...")
```

### Edge Types (Manageable List)

| Value | Name | Description |
|-------|------|-------------|
| `always` | 始终 | 无条件发送，收到就发 |
| `task` | 任务 | 委托任务给目标 agent |
| `reply` | 回复 | 完成任务/收到结果后回复给某 agent |
| `error` | 错误 | 发生错误时通知某 agent |

Edge types are stored in a database table (`edge_types`) and manageable via UI.

### Decision Mode

How the agent decides to send:

| Value | Name | Description |
|-------|------|-------------|
| `always` | 直接发送 | 无条件发送，不需判断 |
| `llm` | AI 判断 | 由 AI 判断是否发送，以及发送什么内容 |

**Example combinations**:
- `task` + `always`: A 无条件把任务发给 B
- `task` + `llm`: A 用 AI 判断是否把任务发给 B
- `reply` + `always`: A 完成后自动回复
- `error` + `always`: A 出错时通知 B

### Data Flow

```
Config Graph (ClawDash UI)
    │
    │ edges stored in MySQL
    ▼
ClawDash Sync Button
    │
    │ generates AGENTS.md blocks
    ▼
OpenClaw Workspace Files
~/.openclaw/agents/{agentId}/AGENTS.md
    │
    │ OpenClaw loads on agent start
    ▼
Agent Behavior
```

### AGENTS.md Structured Blocks

To avoid full-file replacement, ClawDash uses `<!-- CLAWDASH:BLOCK -->` markers:

```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" type="task" -->
## [CLAWDASH] Route to: test

When: ${trigger_condition}
Send to: test
Message: ${message_template}
<!-- CLAWDASH:BLOCK END -->
```

Rules:
- Each block has a unique `id` (the edge UUID)
- ClawDash only manages blocks it created
- Manual edits outside blocks are preserved
- Deleting an edge removes its block

## Capabilities

### New Capabilities

- `config-graph-edge-routing`: Edit edge type and decision mode in Config Graph UI. Defines what message to send to which agent and how to decide.

- `agents-md-sync`: Sync Config Graph edges to OpenClaw `AGENTS.md` files. Generates structured CLAWDASH blocks, handles update/insert/delete per edge. Error edges are synced to the target agent's AGENTS.md.

- `edge-type-management`: CRUD for edge type values. Allows adding new edge types without code changes.

### Modified Capabilities

- `config-graph-agent-management`: Existing edge CRUD extended with edge_type, decision_mode, and message_template fields.

- `openclaw-agent-integration`: Extended to write `AGENTS.md` files via OpenClawService.

## Impact

### Frontend

- **Config Graph**: Add edge detail sidebar with trigger/message editing
- **Edge Detail Panel**: Trigger type selector (always/llm/error), message template textarea, variable hints
- **Sync Preview Dialog**: Show diff before writing to AGENTS.md
- **Trigger Type Admin**: Simple table UI for managing trigger types

### Backend

- **Database**: `config_graph_edges` table extended with `trigger_type`, `trigger_value`, `message_template`, `enabled`
- **New Table**: `trigger_types` (id, value, name, description)
- **New API**:
  - `PATCH /api/config-graphs/edges/{id}` — update edge with trigger
  - `POST /api/config-graphs/sync` — sync all edges to AGENTS.md
  - `GET/POST/PATCH/DELETE /api/trigger-types` — trigger type CRUD

### OpenClaw Integration

- OpenClawService gains `syncEdgesToAgentsMd()` method
- Reads all edges, groups by source agent, writes blocks to each agent's AGENTS.md
- Uses file write (not CLI) — accesses `~/.openclaw/agents/{id}/AGENTS.md`

## Open Questions

1. **Should edges support variable substitution in message templates?** (e.g., `{original_message}`, `{result_summary}`, `{error_message}`)
2. **Should there be a "dry run" mode for sync?** (validate without writing)
3. **How does `llm` trigger type instruct the agent to judge?** (prompt engineering in the AGENTS.md block)
