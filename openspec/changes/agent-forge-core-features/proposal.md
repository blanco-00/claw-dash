## Why

OpenClaw provides powerful multi-agent orchestration via CLI, but lacks: 1) GUI for agent management and relationship configuration, 2) precise task control (start/stop/retry/status), and 3) transactional features like financial tracking. This project adds a management layer to solve these problems.

## What Changes

1. **GUI Agent Management** - Visual interface to add/edit/delete agents with custom properties
2. **Agent Relationship Configuration** - Define agent hierarchies and interaction patterns
3. **Precise Task Control** - Start, stop, retry, monitor tasks with exact state management
4. **Financial/Token Tracking** - Record and visualize token consumption per agent/task
5. **Project Rename** - Rename from "女儿国" to "AgentForge" for broader appeal

## Capabilities

### New Capabilities

- `agent-creation-ui`: GUI form to create new agents with name, role, description
- `agent-relationship`: Visual hierarchy editor for agent parent-child relationships
- `task-control`: Precise task operations (start, stop, retry, pause, resume)
- `token-accounting`: Token usage tracking per agent and task with statistics
- `transaction-log`: Immutable log for financial/operational auditing

### Modified Capabilities

- (None - initial feature set)

## Impact

- Frontend: New pages for agent management, task control, finance
- Backend: New API endpoints for agent CRUD, task operations, accounting
- Database: New tables for relationships, token logs, transaction records
- OpenClaw Integration: CLI command wrappers for agent operations
