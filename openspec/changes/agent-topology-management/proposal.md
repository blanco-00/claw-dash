## Why

As we run multiple agents in OpenClaw, there's no unified way to visualize and manage agent-to-agent (A2A) relationships. Currently, agent connections are configured via CLI (`openclaw agents bind`) with no visual overview. We need a topology view to design agent hierarchies and a runtime view to monitor actual communication patterns. This enables better orchestration, debugging, and understanding of multi-agent workflows.

## What Changes

### New Features

- **Config Graph View**: Visual topology editor for defining intended agent relationships (assigns, reports_to, communicates). Independent from OpenClaw's internal state — acts as a design/intention layer.

- **Runtime Graph View**: Real-time visualization of actual A2A communication derived from OpenClaw data (TaskQueue, bindings). Shows message counts, latency, and connection status.

- **Agent List Enhancement**: Full CRUD for agents with expandable A2A connection details per agent. Searchable, filterable list supporting 10-100 agents.

- **Agent Detail Panel**: Slide-out panel for managing individual agent configuration including SOUL.md/AGENT.md/USER.md files, A2A bindings, and OpenClaw sync.

- **Config-to-OpenClaw Sync**: Button to synchronize Config Graph edges to OpenClaw channel bindings (`openclaw agents bind`).

- **A2A Message History**: Queryable log of inter-agent messages with delivery status and latency.

### Database Changes

- `config_graphs` table: Stores graph configurations
- `config_graph_nodes` table: Graph node-to-agent mappings
- `config_graph_edges` table: Edge definitions with type (assigns, reports_to, communicates)
- `a2a_messages` table: Message history log

### API Changes

- `GET/POST /api/config-graphs` — Config graph CRUD
- `POST /api/config-graphs/:id/sync` — Sync to OpenClaw bindings
- `GET /api/runtime-graphs/current` — Current runtime state
- `GET /api/a2a/messages` — Message history
- `POST /api/a2a/send` — Send test message

## Capabilities

### New Capabilities

- `agent-config-graph`: Visual topology editor for defining agent relationship configurations. Stores intended relationships separately from OpenClaw state.

- `agent-runtime-graph`: Aggregated view of actual A2A communication derived from OpenClaw TaskQueue and bindings. Shows real message flow.

- `agent-a2a-connection-manager`: Manage A2A connections per agent. View/add/remove edges. Supports bind/unbind operations via OpenClaw CLI.

- `agent-message-history`: Queryable message log for A2A communication. Supports filtering by sender, receiver, time range.

- `agent-files-editor`: Edit SOUL.md, AGENT.md, USER.md files within the agent detail panel.

### Modified Capabilities

- `openclaw-agent-integration`: Extend existing OpenClaw integration to support binding operations and agent status synchronization.

## Impact

### Frontend

- New `Agents.vue` view with tabbed interface: Config Graph / Runtime Graph / Agent List
- New `ConfigGraphEditor.vue` component using @vue-flow interactive topology editor
- New `RuntimeGraph.vue` component showing live communication patterns
- New `AgentDetailPanel.vue` slide-out panel
- Extend `AgentList.vue` with expandable A2A rows

### Backend

- New `ConfigGraphController.java` — REST API for config graphs
- New `RuntimeGraphController.java` — REST API for runtime data
- New `A2AMessageController.java` — Message history API
- New `ConfigGraphService.java` — Business logic
- Database migrations for new tables

### External Integrations

- OpenClaw CLI: `openclaw agents bind/unbind` for sync operations
- OpenClaw A2A Gateway Plugin: Future support for cross-network agent communication
