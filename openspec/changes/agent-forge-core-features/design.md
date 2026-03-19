## Context

AgentForge is built on OpenClaw for multi-agent orchestration. Currently OpenClaw operates via CLI/text with no GUI. This design addresses three gaps: 1) visual agent management, 2) precise task control, 3) transactional accounting.

## Goals / Non-Goals

**Goals:**

1. GUI for creating agents with custom properties (name, title, role, description)
2. Visual agent hierarchy/relationship editor
3. Task control panel: start, stop, retry, pause, resume with real-time status
4. Token accounting: per-agent and per-task usage tracking
5. Immutable transaction log for auditing

**Non-Goals:**

- Replace OpenClaw core (remains CLI-based)
- Implement complex AI workflows (OpenClaw handles this)
- Full accounting/ERP system (track tokens only)
- User authentication (single-user local)

## Decisions

### 1. Agent Storage

**Decision:** Store agent metadata in SQLite alongside OpenClaw's data
**Rationale:** Leverages existing infrastructure, maintains consistency
**Alternative:** Separate database → Rejected (data fragmentation)

### 2. Task Control Method

**Decision:** Use OpenClaw CLI + database polling for status
**Rationale:** Direct CLI integration is reliable; polling provides real-time updates
**Alternative:** WebSocket → Added complexity, not needed for MVP

### 3. Token Tracking

**Decision:** Hook into OpenClaw session parsing
**Rationale:** Sessions already contain token usage data; parse and store
**Alternative:** API middleware → Requires OpenClaw API modifications

### 4. Project Name

**Decision:** Rename to "AgentForge"
**Rationale:** Professional, descriptive, no cultural reference issues
**Alternative:** MultiCrew, TaskPilot → Less descriptive

## Risks / Trade-offs

[Risk] OpenClaw CLI changes → Mitigation: Version check on startup, fallback parsing
[Risk] Token parsing breaks → Mitigation: Log parsing errors, show "unknown"
[Risk] Database race conditions → Mitigation: SQLite transactions for critical ops

## Migration Plan

1. Add agent metadata table to existing SQLite
2. Create API endpoints for agent CRUD
3. Build agent management UI page
4. Add task control panel to existing Tasks page
5. Implement token parsing from session logs
6. Create finance/token dashboard
7. Rename UI elements from "女儿国" to "AgentForge"
