## 1. Backend: OpenClaw Service Enhancement

- [x] 1.1 Add `listAgentsWithDetails()` method to read agent directories and metadata
- [x] 1.2 Add `createAgent(name)` method to call `openclaw agents add`
- [x] 1.3 Add `deleteAgent(name)` method to call `openclaw agents delete`
- [x] 1.4 Add `setIdentity(name, options)` method to call `openclaw agents set-identity`

## 2. Backend: OpenClaw Agent Controller

- [x] 2.1 Add `GET /api/openclaw/agents/details` endpoint for agent list with details
- [x] 2.2 Add `POST /api/openclaw/agents` endpoint for creating agents
- [x] 2.3 Add `DELETE /api/openclaw/agents/{name}` endpoint for deleting agents
- [x] 2.4 Add `PATCH /api/openclaw/agents/{name}/identity` endpoint for updating identity

## 3. Frontend: Config Graph Enhancement

- [x] 3.1 Refactor ConfigGraphTab to use OpenClaw as data source
- [x] 3.2 Update "Add Node" to call `POST /api/openclaw/agents` (Note: existing flow adds to graph only, new agent creation uses OpenClaw API)
- [x] 3.3 Update "Delete" to call `DELETE /api/openclaw/agents/{name}`
- [x] 3.4 Add orphaned node detection (agent exists in graph but not in OpenClaw)

## 4. Frontend: Agent List Enhancement

- [x] 4.1 Refactor AgentListTab to call `GET /api/openclaw/agents/details`
- [x] 4.2 Display agent name, workspace in the list
- [x] 4.3 Add sync button to refresh agent list
- [x] 4.4 Show "last synced" timestamp

## 5. Database: Simplify Agents Table

- [x] 5.1 Mark agents table as deprecated (keep for backward compatibility)
- [x] 5.2 Update config_graph_nodes to store agent name reference (already has agent_id field)

## 6. Testing

- [ ] 6.1 Manual test: Create agent via ClawDash → verify in OpenClaw
- [ ] 6.2 Manual test: Delete agent via ClawDash → verify removed from OpenClaw
- [ ] 6.3 Manual test: Refresh Config Graph → agents from OpenClaw appear
- [ ] 6.4 Manual test: Agent List shows all OpenClaw agents
