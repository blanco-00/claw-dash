## Context

### Background

ClawDash manages OpenClaw agents via CLI wrapper. OpenClaw supports multi-agent workflows with channel-based A2A communication. Currently, agents are listed but there's no visual topology or A2A management. Agent relationships are configured via `openclaw agents bind` commands with no unified view.

### Current State

- `Agent` entity stored in MySQL with basic fields (id, name, workspace, status)
- `OpenClawService` wraps CLI: `listAgents`, `addAgent`, `deleteAgent`, `bindAgent`
- `AgentOrgChart.vue` displays hardcoded hierarchy (to be replaced by Config Graph)
- `TaskQueueService` handles task-based A2A via MySQL/Redis
- MCP Server ready but blocked by OpenClaw Issue #43509 (no mcpServers config support)

### Constraints

- Single local OpenClaw instance (no remote distributed agents)
- 10-100 agent scale
- Config Graph is independent from OpenClaw state (may diverge)
- Runtime Graph data depends on OpenClaw CLI polling or TaskQueue

### Stakeholders

- Agent operators who configure agent hierarchies
- Developers debugging A2A communication issues
- System administrators monitoring agent health

---

## Goals / Non-Goals

**Goals:**

- Provide visual topology editor for designing agent relationships (Config Graph)
- Show real A2A communication patterns (Runtime Graph)
- Manage OpenClaw agent bindings from UI (sync Config → OpenClaw)
- Support 10-100 agents with search, filter, and grouping
- Edit agent files (SOUL.md, AGENT.md, USER.md) from UI

**Non-Goals:**

- Running agents (activation = `openclaw agents add` registration only)
- Cross-network A2A via A2A Gateway Plugin (future consideration)
- Full OpenClaw configuration management (only A2A bindings)
- Real-time WebSocket updates (polling-based for MVP)
- Distributed multi-OpenClaw instances (single local instance)

---

## Decisions

### Decision 1: Dual-Graph Architecture (Config + Runtime)

**Choice:** Separate Config Graph (intention) from Runtime Graph (actual)

**Rationale:**
- Config Graph represents "desired state" — what relationships should exist
- Runtime Graph represents "observed state" — what actually happened
- Allows Config to exist independently of OpenClaw state
- Supports debugging: configured but not communicating? Check Runtime.

**Alternatives Considered:**
- Single unified graph: Rejected — mixes design intent with observed behavior, harder to debug divergence
- Runtime only: Rejected — no way to configure/design relationships

### Decision 2: Config Graph Storage

**Choice:** Store Config Graph in ClawDash MySQL, not in OpenClaw workspace

**Rationale:**
- OpenClaw workspace files (SOUL.md) don't have structured relationship format yet
- `parseSoulMdRelations()` is stub — parsing is unreliable
- DB storage enables easy querying, versioning, and API access
- Can sync to OpenClaw bindings via CLI when available

**Alternatives Considered:**
- Store in workspace SOUL.md: Rejected — parsing unreliable, no standard format
- Store in localStorage only: Rejected — no API access, can't sync between clients

### Decision 3: Runtime Graph Data Source

**Choice:** OpenClaw CLI bindings + A2A Gateway Plugin (future)

**Rationale:**
- `openclaw agents bindings --json` provides current channel binding state
- A2A Gateway Plugin (community plugin) provides peer communication stats
- These represent actual A2A communication, separate from TaskQueue (async task delegation)

**Note:** TaskQueue is for async task delegation (task_create, task_complete), NOT for A2A communication. Runtime Graph intentionally excludes TaskQueue data.

**Alternatives Considered:**
- TaskQueue as A2A source: Rejected — TaskQueue is task management, not agent messaging
- A2A Gateway Plugin JSON-RPC: Future — requires plugin installation
- Direct agent instrumentation: Too invasive, requires OpenClaw changes

### Decision 4: @vue-flow for Topology Visualization

**Choice:** Use @vue-flow for Config and Runtime graphs

**Rationale:**
- Provides built-in interactive edge creation/deletion (via `@connect` event)
- Built-in drag-and-drop for nodes, zoom/pan, selection
- Vue 3 native integration with reactive v-model
- Custom node/edge components via slots
- Already installed in project (`@vue-flow/core`, `@vue-flow/minimap`, `@vue-flow/controls`)
- Minimap and Controls components available out-of-box

**Layout Strategy:**
- Node positions stored in DB (from Config Graph)
- D3 force used only for initial layout computation (optional)
- User drag → position saved to DB → subsequent loads use saved positions

**Alternatives Considered:**
- D3.js Force: Rejected — no built-in edge editing UX, requires ~300-500 LOC vs ~50-100 LOC
- ECharts Graph: Rejected — poor edge editing support, node drag snaps back in force layout
- Cytoscape.js: Steeper learning curve, less Vue 3 integration
- GoJS: Commercial license concerns

### Decision 5: Edge Types

**Choice:** Three edge types — `assigns`, `reports_to`, `communicates`

**Rationale:**
- `assigns`: Task delegation (A assigns work to B)
- `reports_to`: Hierarchical reporting (B reports to A)
- `communicates`: Peer-to-peer communication (A and B exchange messages)
- Covers typical organizational + multi-agent patterns

**Alternatives Considered:**
- Only `communicates`: Too simple, no hierarchy representation
- More granular types: Risk over-engineering before understanding real usage

### Decision 6: Sync Strategy

**Choice:** Manual sync button — Config Graph → OpenClaw bindings

**Rationale:**
- Allows Config to diverge from OpenClaw (intentional or not)
- No automatic reconciliation — user controls when to apply
- Clear feedback: "last synced at X", "12 differences found"

**Alternatives Considered:**
- Auto-sync on Config change: Risky — can't rollback, may disrupt running agents
- Real-time bidirectional sync: Too complex for MVP

---

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| Config Graph drifts from OpenClaw state | User confusion | Show sync status, highlight divergence |
| Runtime data polling overhead | Performance | Cache aggressively, configurable refresh rate |
| SOUL.md parsing unreliable | Incomplete auto-population | Keep as manual-only for MVP |
| Agent count scales beyond 100 | UI overwhelm | Clustering, pagination, workspace filtering |
| OpenClaw CLI rate limiting | Bind/unbind failures | Add retry logic, queue operations |
| A2A Gateway Plugin not installed | Cross-agent comm won't work | Detect plugin presence, show setup guide |

---

## Migration Plan

### Phase 1: Database Migration
1. Create new tables: `config_graphs`, `config_graph_nodes`, `config_graph_edges`, `a2a_messages`
2. Add indexes for performance
3. No data migration needed (new tables)

### Phase 2: Backend API
1. Implement `ConfigGraphController` and `ConfigGraphService`
2. Implement `RuntimeGraphController` (aggregates TaskQueue data)
3. Implement `A2AMessageController`
4. Test all endpoints

### Phase 3: Frontend UI
1. Create tabbed `Agents.vue` (Config Graph / Runtime Graph / List)
2. Migrate existing `AgentOrgChart.vue` to Config Graph tab
3. Create Runtime Graph tab with polling
4. Enhance Agent List with A2A expandable rows
5. Create Agent Detail slide-out panel

### Phase 4: Integration
1. Wire Config Graph edges → OpenClaw `bind` calls
2. Wire Runtime Graph → TaskQueue queries
3. Add sync status indicators
4. End-to-end testing

### Rollback
- Revert database migrations (drop new tables)
- Revert frontend to previous AgentOrgChart
- Config Graph data is additive, no rollback needed for existing agents

---

## Open Questions

1. **Should Config Graph support multiple graphs?** (e.g., "Dev Topology" vs "Prod Topology")
   - Decision: Single graph for MVP, multi-graph as P2 feature

2. **How to represent SOUL.md/AGENT.md/USER.md in Config Graph?**
   - Decision: Files edited per-agent in detail panel, not as graph nodes

3. **What triggers Runtime Graph refresh?**
   - Decision: Manual refresh button + auto-refresh every 30s for MVP, WebSocket as P2

4. **How to handle agent deletion when it's referenced in Config Graph?**
   - Decision: Cascade delete nodes, orphan edges prevented by DB constraint

5. **Should Runtime Graph show failed/disconnected edges?**
   - Decision: Yes, show all edges with status indicator (active/idle/failed)
