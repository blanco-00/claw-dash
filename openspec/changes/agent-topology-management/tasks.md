## 1. Database Migration

- [x] 1.1 Create `config_graphs` table (JPA auto-creates via MyBatis-Plus)
- [x] 1.2 Create `config_graph_nodes` table (JPA auto-creates)
- [x] 1.3 Create `config_graph_edges` table (JPA auto-creates)
- [x] 1.4 Create `a2a_messages` table (JPA auto-creates)
- [ ] 1.5 Add foreign key constraints and indexes (manual DB script if needed)
- [ ] 1.6 Create Flyway/Liquibase migration script (optional)

## 2. Backend - Config Graph API

- [x] 2.1 Create `ConfigGraph` JPA entity
- [x] 2.2 Create `ConfigGraphNode` JPA entity
- [x] 2.3 Create `ConfigGraphEdge` JPA entity
- [x] 2.4 Create `A2AMessage` JPA entity
- [x] 2.5 Create `ConfigGraphRepository` JPA repository
- [x] 2.6 Create `ConfigGraphService` with CRUD operations
- [x] 2.7 Create `ConfigGraphController` REST endpoints
  - [x] GET/POST /api/config-graphs
  - [x] GET/PUT/DELETE /api/config-graphs/:id
  - [x] POST /api/config-graphs/:id/nodes
  - [x] DELETE /api/config-graphs/:id/nodes/:nodeId
  - [x] POST /api/config-graphs/:id/edges
  - [x] PUT /api/config-graphs/:id/edges/:edgeId
  - [x] DELETE /api/config-graphs/:id/edges/:edgeId
  - [x] POST /api/config-graphs/:id/sync

## 3. Backend - Runtime Graph API

- [ ] 3.1 Create `RuntimeGraphService` to aggregate OpenClaw bindings
- [ ] 3.2 Create `RuntimeGraphController` REST endpoint
  - [ ] GET /api/runtime-graphs/current
  - [ ] GET /api/runtime-graphs/history

## 4. Backend - A2A Message API

- [x] 4.1 Create `A2AMessageRepository` with query methods
- [x] 4.2 Create `A2AMessageService`
- [x] 4.3 Create `A2AMessageController` REST endpoints
  - [x] GET /api/a2a/messages (with filters: from, to, since)
  - [x] POST /api/a2a/send (send test message)

## 5. Backend - OpenClaw Integration Enhancement

- [ ] 5.1 Extend `OpenClawService.bindAgent()` to return binding details
- [ ] 5.2 Add `getBindings()` method to `OpenClawService`
- [ ] 5.3 Add `unbindAgent()` method to `OpenClawService`
- [x] 5.4 Create sync logic to compare Config edges vs OpenClaw bindings (in ConfigGraphController)

## 6. Frontend - Agent Management View

- [ ] 6.1 Create tabbed `AgentsView.vue` with Config Graph / Runtime Graph / List tabs
- [ ] 6.2 Create `AgentListTab.vue` component with search/filter
- [ ] 6.3 Create `ConfigGraphTab.vue` @vue-flow topology editor
- [ ] 6.4 Create `RuntimeGraphTab.vue` visualization
- [ ] 6.5 Add expand/collapse to AgentList rows for A2A details

## 7. Frontend - Config Graph Editor

- [ ] 7.1 Integrate @vue-flow with custom node/edge components
- [ ] 7.2 Implement node drag-and-drop positioning
- [ ] 7.3 Implement edge creation (click source → click target → select type)
- [ ] 7.4 Implement edge enable/disable toggle
- [ ] 7.5 Persist layout positions to backend
- [ ] 7.6 Add "Sync to OpenClaw" button with diff display

## 8. Frontend - Agent Detail Panel

- [ ] 8.1 Create `AgentDetailPanel.vue` slide-out panel
- [ ] 8.2 Display agent identity and workspace info
- [ ] 8.3 Display OpenClaw bindings list
- [ ] 8.4 Implement bind/unbind channel operations
- [ ] 8.5 Display Config Graph edges for this agent
- [ ] 8.6 Implement add/remove connection UI

## 9. Frontend - File Editor

- [ ] 9.1 Create `FileEditor.vue` component (Monaco editor)
- [ ] 9.2 Implement SOUL.md file viewing/editing
- [ ] 9.3 Implement AGENT.md file viewing/editing
- [ ] 9.4 Implement USER.md file viewing/editing
- [ ] 9.5 Implement save to filesystem via backend API
- [ ] 9.6 Add "Open in External Editor" functionality

## 10. Frontend - API Integration

- [ ] 10.1 Create `configGraphApi.ts` service
- [ ] 10.2 Create `runtimeGraphApi.ts` service
- [ ] 10.3 Create `a2aMessageApi.ts` service
- [ ] 10.4 Integrate APIs with Vue components
- [ ] 10.5 Add loading states and error handling

## 11. Testing

- [ ] 11.1 Write unit tests for ConfigGraphService
- [ ] 11.2 Write unit tests for RuntimeGraphService
- [ ] 11.3 Write unit tests for A2AMessageService
- [ ] 11.4 Write unit tests for OpenClawService sync logic
- [ ] 11.5 Manual testing of Config Graph UI
- [ ] 11.6 Manual testing of Runtime Graph refresh
- [ ] 11.7 Manual testing of OpenClaw sync operation

## 12. Polish

- [ ] 12.1 Add empty states for no agents / no connections
- [ ] 12.2 Add loading skeletons
- [ ] 12.3 Add sync status indicator (last synced, divergence warning)
- [ ] 12.4 Add keyboard shortcuts for graph editing
- [ ] 12.5 Responsive layout for smaller screens
