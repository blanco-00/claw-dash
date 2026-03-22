# Multi-Agent Routing — Implementation Tasks

## Phase 1: Database Migration

- [x] 1.1 Add `edge_type` column to `config_graph_edges` table (VARCHAR(20), default 'task')
- [x] 1.2 Add `decision_mode` column to `config_graph_edges` table (VARCHAR(20), default 'always')
- [x] 1.3 Add `message_template` column to `config_graph_edges` table (TEXT, default '')
- [x] 1.4 Ensure `enabled` column exists (should already exist)
- [x] 1.5 Create `edge_types` table with seed data (always, task, reply, error)
- [x] 1.6 Add database migration script

## Phase 2: Backend API

### Edge Type Management
- [x] 2.1 Add `EdgeType` entity
- [x] 2.2 Add `EdgeTypeMapper` (MyBatis)
- [x] 2.3 Add `EdgeTypeService` with CRUD methods
- [x] 2.4 Add `EdgeTypeController` with GET/POST/PATCH/DELETE endpoints

### Edge Routing Fields
- [x] 2.5 Update `ConfigGraphEdge` entity with new fields (edgeType, decisionMode, messageTemplate, enabled)
- [x] 2.6 Update `ConfigGraphEdgeMapper` for new fields
- [x] 2.7 Update `ConfigGraphService` edge CRUD methods
- [x] 2.8 Update `ConfigGraphController` edge endpoints

### AGENTS.md Sync
- [x] 2.9 Add `AgentsMdSyncService` class
- [x] 2.10 Implement `parseExistingBlocks()` method (regex-based CLAWDASH block extraction)
- [x] 2.11 Implement `generateBlock()` method for each edge type + decision mode combination
- [x] 2.12 Implement `syncToAgent()` method with atomic write (temp file + rename)
- [x] 2.13 Implement `syncPreview()` method (returns diff without writing)
- [x] 2.14 Implement `syncAll()` method (full sync)
- [x] 2.15 Add `SyncResult` DTO with agentsUpdated, edgesSynced, blocksAdded/Modified/Removed, errors
- [x] 2.16 Add `SyncPreviewResult` DTO with per-agent diff
- [x] 2.17 Add `GET /api/config-graphs/{id}/sync-preview` endpoint
- [x] 2.18 Add `POST /api/config-graphs/{id}/sync` endpoint
- [x] 2.19 Add error edge routing: error edges sync to **source** agent, not target

## Phase 3: Frontend - Edge Detail Panel
- [x] 3.1 Add edge type selector (radio buttons: 始终/任务/回复/错误)
- [x] 3.2 Add decision mode selector (radio buttons: 直接发送/AI 判断)
- [x] 3.3 Add message template textarea
- [x] 3.4 Add variable hints display (contextual based on edge type)
- [x] 3.5 Add enable/disable toggle checkbox
- [x] 3.6 Add "预览 Sync 效果" button with preview dialog
- [x] 3.7 Wire up Save button to PATCH endpoint
- [x] 3.8 Update edge visual in Config Graph based on edge_type (icons) and enabled (dashed for disabled)

## Phase 4: Frontend - Sync UI
- [x] 4.1 Add "Sync to OpenClaw" button to Config Graph toolbar
- [x] 4.2 Add Sync Preview dialog with diff display (+/-/~ markers)
- [x] 4.3 Wire up "执行 Sync" button to POST endpoint
- [x] 4.4 Show success/error toast with sync result summary

## Phase 5: Testing (Manual - Run After Deployment)

- [ ] 5.1 Run V3 migration: `mysql -u root -p clawdash < V3__edge_routing_tables.sql`
- [ ] 5.2 Start backend: `cd backend && mvn spring-boot:run`
- [ ] 5.3 Start frontend: `cd frontend && npm run dev`
- [ ] 5.4 Navigate to `/edge-types` — verify EdgeType CRUD
- [ ] 5.5 Navigate to `/agents-config-graph` — create edge with routing, click Sync

## Phase 6: Edge Type Management UI

- [x] 6.1 Add EdgeType list page (EdgeTypes.vue)
- [x] 6.2 Add "Add Edge Type" modal
- [x] 6.3 Add "Edit Edge Type" functionality
- [x] 6.4 Add "Delete Edge Type" with usage check

---

## Task Dependencies

```
Phase 1 (Database)
    ↓
Phase 2 (Backend API) ──────────┐
    ↓                          │
Phase 3 (Frontend - Panel)     │
    ↓                          │
Phase 4 (Frontend - Sync) ─────► Phase 5 (Testing)
                                   │
                              Phase 6 (P2)
```

## Notes

- Agent AGENTS.md file path: `~/.openclaw/agents/{agentId}/workspace/AGENTS.md`
- If workspace subdir doesn't exist, fallback to: `~/.openclaw/agents/{agentId}/AGENTS.md`
- Use Java's `Files.writeString()` with StandardOpenOption.CREATE, StandardOpenOption.TRANSACTIONaL
