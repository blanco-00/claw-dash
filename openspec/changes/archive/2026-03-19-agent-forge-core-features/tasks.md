## 1. Database Schema

- [x] 1.1 Add agents metadata table (id, name, title, role, description, parent_id)
- [x] 1.2 Add agent relationships table (parent_id, child_id, relationship_type)
- [x] 1.3 Add token_usage table (agent_id, task_id, input_tokens, output_tokens, cost)
- [x] 1.4 Add transaction_log table (timestamp, operation, entity_type, entity_id, details)
- [x] 1.5 Add model_pricing table (model_name, price_per_1k_input, price_per_1k_output)

## 2. Backend API

- [x] 2.1 Create POST /api/agents endpoint
- [x] 2.2 Create PUT /api/agents/:id endpoint
- [x] 2.3 Create DELETE /api/agents/:id endpoint
- [x] 2.4 Create GET /api/agents/:id/relationships endpoint
- [x] 2.5 Create POST /api/tasks/:id/start endpoint
- [x] 2.6 Create POST /api/tasks/:id/stop endpoint
- [x] 2.7 Create POST /api/tasks/:id/retry endpoint
- [x] 2.8 Create GET /api/tokens/stats endpoint
- [x] 2.9 Create GET /api/transactions endpoint

## 3. Frontend - Agent Management

- [x] 3.1 Add "New Agent" button to agent list page
- [x] 3.2 Create agent form dialog (name, title, role, description)
- [x] 3.3 Add edit/delete actions to agent table
- [x] 3.4 Create agent relationship editor

## 4. Frontend - Task Control

- [x] 4.1 Add Start/Stop/Retry buttons to task list
- [x] 4.2 Add task status indicator with real-time updates
- [x] 4.3 Create task detail modal with logs

## 5. Frontend - Token/Finance

- [x] 5.1 Create token usage dashboard
- [x] 5.2 Add charts for token consumption over time
- [x] 5.3 Create cost summary by agent
- [x] 5.4 Add transaction log viewer

## 6. Project Rename

- [x] 6.1 Update page titles from "女儿国" to "AgentForge"
- [x] 6.2 Update menu labels
- [x] 6.3 Update README and documentation

## 7. Integration

- [x] 7.1 Integrate OpenClaw CLI for agent creation
- [x] 7.2 Integrate OpenClaw CLI for task control
- [x] 7.3 Parse session output for token usage
- [x] 7.4 Test end-to-end flow
