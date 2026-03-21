# Multi-Agent Routing — Implementation Tasks

## Phase 1: Database Migration

- [ ] 1.1 Add `edge_type` column to `config_graph_edges` table (VARCHAR(20), default 'task')
- [ ] 1.2 Add `decision_mode` column to `config_graph_edges` table (VARCHAR(20), default 'always')
- [ ] 1.3 Add `message_template` column to `config_graph_edges` table (TEXT, default '')
- [ ] 1.4 Ensure `enabled` column exists (should already exist)
- [ ] 1.5 Create `edge_types` table with seed data (always, task, reply, error)
- [ ] 1.6 Add database migration script

## Phase 2: Backend API

### Edge Type Management
- [ ] 2.1 Add `EdgeType` entity
- [ ] 2.2 Add `EdgeTypeMapper` (MyBatis)
- [ ] 2.3 Add `EdgeTypeService` with CRUD methods
- [ ] 2.4 Add `EdgeTypeController` with GET/POST/PATCH/DELETE endpoints

### Edge Routing Fields
- [ ] 2.5 Update `ConfigGraphEdge` entity with new fields (edgeType, decisionMode, messageTemplate, enabled)
- [ ] 2.6 Update `ConfigGraphEdgeMapper` for new fields
- [ ] 2.7 Update `ConfigGraphService` edge CRUD methods
- [ ] 2.8 Update `ConfigGraphController` edge endpoints

### AGENTS.md Sync
- [ ] 2.9 Add `AgentsMdSyncService` class
- [ ] 2.10 Implement `parseExistingBlocks()` method (regex-based CLAWDASH block extraction)
- [ ] 2.11 Implement `generateBlock()` method for each edge type + decision mode combination
- [ ] 2.12 Implement `syncToAgent()` method with atomic write (temp file + rename)
- [ ] 2.13 Implement `syncPreview()` method (returns diff without writing)
- [ ] 2.14 Implement `syncAll()` method (full sync)
- [ ] 2.15 Add `SyncResult` DTO with agentsUpdated, edgesSynced, blocksAdded/Modified/Removed, errors
- [ ] 2.16 Add `SyncPreviewResult` DTO with per-agent diff
- [ ] 2.17 Add `GET /api/config-graphs/{id}/sync-preview` endpoint
- [ ] 2.18 Add `POST /api/config-graphs/{id}/sync` endpoint
- [ ] 2.19 Add error edge routing: error edges sync to **source** agent, not target

## Phase 3: Frontend - Edge Detail Panel

- [ ] 3.1 Add edge type selector (radio buttons: 始终/任务/回复/错误)
- [ ] 3.2 Add decision mode selector (radio buttons: 直接发送/AI 判断)
- [ ] 3.3 Add message template textarea
- [ ] 3.4 Add variable hints display (contextual based on edge type)
- [ ] 3.5 Add enable/disable toggle checkbox
- [ ] 3.6 Add "预览 Sync 效果" button with preview dialog
- [ ] 3.7 Wire up Save button to PATCH endpoint
- [ ] 3.8 Update edge visual in Config Graph based on edge_type (icons) and enabled (dashed for disabled)

## Phase 4: Frontend - Sync UI

- [ ] 4.1 Add "Sync to OpenClaw" button to Config Graph toolbar
- [ ] 4.2 Add Sync Preview dialog with diff display (+/-/~ markers)
- [ ] 4.3 Wire up "执行 Sync" button to POST endpoint
- [ ] 4.4 Show success/error toast with sync result summary

## Phase 5: Testing

- [ ] 5.1 Unit test: EdgeType CRUD
- [ ] 5.2 Unit test: CLAWDASH block parsing (existing blocks)
- [ ] 5.3 Unit test: Block generation for each edge_type + decision_mode combination
- [ ] 5.4 Unit test: Sync algorithm (add/update/remove blocks)
- [ ] 5.5 Manual test: Create edge, set fields, verify in database
- [ ] 5.6 Manual test: Sync preview shows correct diff
- [ ] 5.7 Manual test: Sync writes correct AGENTS.md blocks
- [ ] 5.8 Manual test: Verify OpenClaw agent respects new routing rules

## Phase 6: Edge Type Management UI (P2)

- [ ] 6.1 Add EdgeType list page
- [ ] 6.2 Add "Add Edge Type" modal
- [ ] 6.3 Add "Edit Edge Type" functionality
- [ ] 6.4 Add "Delete Edge Type" with usage check (cannot delete if in use)

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
