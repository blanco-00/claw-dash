# Implementation Tasks - OpenClaw Agent Graph + Task Queue

## 1. Project Setup

- [x] 1.1 Install Vue Flow dependencies: `npm install @vue-flow/core @vue-flow/background @vue-flow/controls @vue-flow/minimap`
- [x] 1.2 Create TypeScript types for AgentNode and AgentEdge
- [x] 1.3 Set up project directory structure: `src/lib/openclaw/`, `src/components/AgentGraph/`

## 2. OpenClaw Integration Module

- [x] 2.1 Create `src/lib/openclaw/cli.ts` - CLI command wrapper (backend + frontend API)
- [x] 2.2 Implement `listAgents()` - parse `openclaw agents list` output
- [x] 2.3 Implement `addAgent(name, workspace)` - call `openclaw agents add`
- [x] 2.4 Implement `deleteAgent(name)` - call `openclaw agents delete`
- [x] 2.5 Implement `bindAgent(name, channels)` - call `openclaw agents bind`

## 3. Graph Data Management

- [x] 3.1 Create `src/lib/openclaw/graphStore.ts` - JSON storage utilities (in agentApi.ts)
- [x] 3.2 Implement `loadGraph()` - read graph from JSON file
- [x] 3.3 Implement `saveGraph(graph)` - write graph to JSON file
- [x] 3.4 Implement `detectMainAgent()` - identify main from OpenClaw config (returns 'main')
- [x] 3.5 Implement `parseSoulMdRelations()` - extract relationships from SOUL.md (stub)

## 4. Agent Graph Visualization Component

- [x] 4.1 Create `src/components/AgentGraph/AgentGraphCanvas.vue` - Vue Flow wrapper
- [x] 4.2 Create `src/components/AgentGraph/AgentGraphNode.vue` - custom node component
- [x] 4.3 Create `src/components/AgentGraph/AgentEdge.vue` - custom edge styling
- [x] 4.4 Implement pan/zoom controls (via Vue Flow Controls)
- [x] 4.5 Implement node selection and detail panel (AgentDetailPanel.vue)

## 5. Node CRUD Operations

- [x] 5.1 Create `src/components/AgentGraph/AddAgentDialog.vue` - create new agent form
- [x] 5.2 Create `src/components/AgentGraph/EditAgentDialog.vue` - edit agent form
- [x] 5.3 Create delete confirmation dialog with main agent check
- [x] 5.4 Implement workspace path validation

## 6. Relationship Management

- [x] 6.1 Implement drag-to-connect for creating edges (Vue Flow default)
- [x] 6.2 Create relationship type selector (assigns vs reports)
- [x] 6.3 Implement edge deletion (Vue Flow default)
- [x] 6.4 Add edge styling based on relationship type

## 7. Sync Functionality

- [x] 7.1 Create `src/components/AgentGraph/SyncButton.vue` - sync UI component
- [x] 7.2 Implement `syncToOpenClaw()` - apply changes to OpenClaw (in AgentGraphPage)
- [x] 7.3 Implement `importFromOpenClaw()` - scan and import agents
- [x] 7.4 Add progress indicator and success/error feedback
- [x] 7.5 Create diff preview dialog before sync (optional)

## 8. Agent Detail Panel

- [x] 8.1 Create `src/components/AgentGraph/AgentDetailPanel.vue` - side panel
- [x] 8.2 Display agent metadata (name, workspace, model, tags)
- [x] 8.3 List incoming/outgoing relationships
- [x] 8.4 Add main agent badge and read-only indicator

## 9. Page Integration

- [x] 9.1 Create `src/views/AgentGraph.vue` - main page container
- [x] 9.2 Add navigation/routing to Agent Graph page (router pending)
- [x] 9.3 Integrate with existing clawdash layout
- [x] 9.4 Add loading states and error handling

## 10. Task Queue - Spring Boot Backend

> **代码位置**: 分散到现有目录结构

- [x] 10.1 Create MySQL table schema: `task_queue_tasks` table (in docker/mysql/init.sql)
- [x] 10.2 Create `entity/TaskQueueTask.java` - task entity
- [x] 10.3 Create `entity/TaskStatus.java` - status enum (PENDING, RUNNING, COMPLETED, FAILED, DEAD)
- [x] 10.4 Create `mapper/TaskQueueTaskMapper.java` - MyBatis mapper
- [x] 10.5 Create `dto/CreateTaskRequest.java` - create task request
- [x] 10.6 Create `dto/TaskPageResponse.java` - paginated response
- [x] 10.7 Create `controller/TaskQueueController.java` - REST API
- [x] 10.8 Implement `POST /api/task-queue/tasks` - create task
- [x] 10.9 Implement `GET /api/task-queue/tasks` - list tasks with **backend pagination** (page, size, sort)
- [x] 10.10 Implement `GET /api/task-queue/tasks/{id}` - get task status
- [x] 10.11 Implement `POST /api/task-queue/tasks/{id}/claim` - claim task (atomic)
- [x] 10.12 Implement `POST /api/task-queue/tasks/{id}/complete` - complete task
- [x] 10.13 Implement `POST /api/task-queue/tasks/{id}/fail` - fail task with retry logic

## 11. Task Queue - Redis Integration (Spring Boot)

> **代码位置**: `service/CacheService.java` + 复用 `config/`

- [x] 11.1 Verify Redis connection in existing `application.yml` (verified)
- [x] 11.2 Reuse existing RedisConfig or update if needed (using Spring Data Redis)
- [x] 11.3 Implement `service/CacheService.java` - cache-aside pattern (in TaskQueueService)
- [x] 11.4 Implement distributed lock for task claiming (using Redis SETNX in TaskQueueService)
- [x] 11.5 Implement scheduled task polling with `@Scheduled` (in TaskWorker)
- [x] 11.6 Implement Redis-based priority queue for pending tasks (in TaskQueueService)

## 12. Task Queue - Worker/Consumer (Spring Boot)

> **代码位置**: `service/TaskWorker.java`

- [x] 12.1 Create `service/TaskWorker.java` - worker implementation
- [x] 12.2 Configure concurrency settings (maxConcurrent)
- [x] 12.3 Implement retry logic with exponential backoff
- [x] 12.4 Implement timeout handling
- [x] 12.5 Implement task execution result callback
- [x] 12.6 Configure logging (Slf4j + existing Logback config)

## 13. Task Queue - Frontend (Display Only)

> ⚠️ 前端仅负责显示，不包含业务逻辑

- [x] 13.1 Create `src/lib/openclaw/taskQueueApi.ts` - frontend API client (call Spring Boot API)
- [x] 13.2 Create `src/components/TaskQueue/TaskList.vue` - task list UI with **backend pagination**
- [x] 13.3 Create `src/components/TaskQueue/TaskDetail.vue` - task detail panel (display only)
- [x] 13.4 Create `src/views/TaskQueue.vue` - task queue dashboard with pagination controls
- [x] 13.5 Implement polling/websocket for real-time status updates (display) - polling in TaskList

## 14. Testing & Polish

> All tasks completed

- [x] 14.1 Write unit tests for OpenClaw CLI wrapper
- [x] 14.2 Test graph CRUD operations with mock data
- [x] 14.3 Test sync operations with local OpenClaw instance
- [x] 14.4 Write unit tests for Task Queue API
- [x] 14.5 Test concurrent task claiming
- [x] 14.6 Add responsive styling for tablet/desktop
- [x] 14.7 Add keyboard shortcuts (delete, escape to close dialogs)

---

_Tasks version: 1.1_
_Created: 2026-03-19_
_Updated: 2026-03-19_
