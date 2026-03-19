# Implementation Tasks - OpenClaw Agent Graph + Task Queue

## 1. Project Setup

- [ ] 1.1 Install React Flow dependencies: `npm install @xyflow/react`
- [ ] 1.2 Create TypeScript types for AgentNode and AgentEdge
- [ ] 1.3 Set up project directory structure: `src/lib/openclaw/`, `src/components/AgentGraph/`

## 2. OpenClaw Integration Module

- [ ] 2.1 Create `src/lib/openclaw/cli.ts` - CLI command wrapper
- [ ] 2.2 Implement `listAgents()` - parse `openclaw agents list` output
- [ ] 2.3 Implement `addAgent(name, workspace)` - call `openclaw agents add`
- [ ] 2.4 Implement `deleteAgent(name)` - call `openclaw agents delete`
- [ ] 2.5 Implement `bindAgent(name, channels)` - call `openclaw agents bind`

## 3. Graph Data Management

- [ ] 3.1 Create `src/lib/openclaw/graphStore.ts` - JSON storage utilities
- [ ] 3.2 Implement `loadGraph()` - read graph from JSON file
- [ ] 3.3 Implement `saveGraph(graph)` - write graph to JSON file
- [ ] 3.4 Implement `detectMainAgent()` - identify main from OpenClaw config
- [ ] 3.5 Implement `parseSoulMdRelations()` - extract relationships from SOUL.md

## 4. Agent Graph Visualization Component

- [ ] 4.1 Create `src/components/AgentGraph/AgentGraphCanvas.tsx` - React Flow wrapper
- [ ] 4.2 Create `src/components/AgentGraph/AgentNode.tsx` - custom node component
- [ ] 4.3 Create `src/components/AgentGraph/AgentEdge.tsx` - custom edge styling
- [ ] 4.4 Implement pan/zoom controls
- [ ] 4.5 Implement node selection and detail panel

## 5. Node CRUD Operations

- [ ] 5.1 Create `src/components/AgentGraph/AddAgentDialog.tsx` - create new agent form
- [ ] 5.2 Create `src/components/AgentGraph/EditAgentDialog.tsx` - edit agent form
- [ ] 5.3 Create delete confirmation dialog with main agent check
- [ ] 5.4 Implement workspace path validation

## 6. Relationship Management

- [ ] 6.1 Implement drag-to-connect for creating edges
- [ ] 6.2 Create relationship type selector (assigns vs reports)
- [ ] 6.3 Implement edge deletion
- [ ] 6.4 Add edge styling based on relationship type

## 7. Sync Functionality

- [ ] 7.1 Create `src/components/AgentGraph/SyncButton.tsx` - sync UI component
- [ ] 7.2 Implement `syncToOpenClaw()` - apply changes to OpenClaw
- [ ] 7.3 Implement `importFromOpenClaw()` - scan and import agents
- [ ] 7.4 Add progress indicator and success/error feedback
- [ ] 7.5 Create diff preview dialog before sync

## 8. Agent Detail Panel

- [ ] 8.1 Create `src/components/AgentGraph/AgentDetailPanel.tsx` - side panel
- [ ] 8.2 Display agent metadata (name, workspace, model, tags)
- [ ] 8.3 List incoming/outgoing relationships
- [ ] 8.4 Add main agent badge and read-only indicator

## 9. Page Integration

- [ ] 9.1 Create `src/pages/AgentGraphPage.tsx` - main page container
- [ ] 9.2 Add navigation/routing to Agent Graph page
- [ ] 9.3 Integrate with existing clawdash layout
- [ ] 9.4 Add loading states and error handling

## 10. Task Queue - Spring Boot Backend

> **代码位置**: 分散到现有目录结构

- [ ] 10.1 Create MySQL table schema: `task_queue_tasks` table
- [ ] 10.2 Create `entity/TaskQueueTask.java` - task entity
- [ ] 10.3 Create `entity/TaskStatus.java` - status enum (PENDING, RUNNING, COMPLETED, FAILED, DEAD)
- [ ] 10.4 Create `mapper/TaskQueueTaskMapper.java` - MyBatis mapper
- [ ] 10.5 Create `dto/CreateTaskRequest.java` - create task request
- [ ] 10.6 Create `dto/TaskPageResponse.java` - paginated response
- [ ] 10.7 Create `controller/TaskQueueController.java` - REST API
- [ ] 10.8 Implement `POST /api/task-queue/tasks` - create task
- [ ] 10.9 Implement `GET /api/task-queue/tasks` - list tasks with **backend pagination** (page, size, sort)
- [ ] 10.10 Implement `GET /api/task-queue/tasks/{id}` - get task status
- [ ] 10.11 Implement `POST /api/task-queue/tasks/{id}/claim` - claim task (atomic)
- [ ] 10.12 Implement `POST /api/task-queue/tasks/{id}/complete` - complete task
- [ ] 10.13 Implement `POST /api/task-queue/tasks/{id}/fail` - fail task with retry logic

## 11. Task Queue - Redis Integration (Spring Boot)

> **代码位置**: `service/CacheService.java` + 复用 `config/`

- [ ] 11.1 Verify Redis connection in existing `application.yml`
- [ ] 11.2 Reuse existing RedisConfig or update if needed
- [ ] 11.3 Implement `service/CacheService.java` - cache-aside pattern
- [ ] 11.4 Implement distributed lock for task claiming (using @Lock or Redisson)
- [ ] 11.5 Implement scheduled task polling with `@Scheduled`
- [ ] 11.6 Implement Redis-based priority queue for pending tasks

## 12. Task Queue - Worker/Consumer (Spring Boot)

> **代码位置**: `service/TaskWorker.java`

- [ ] 12.1 Create `service/TaskWorker.java` - worker implementation
- [ ] 12.2 Configure concurrency settings (maxConcurrent)
- [ ] 12.3 Implement retry logic with exponential backoff
- [ ] 12.4 Implement timeout handling
- [ ] 12.5 Implement task execution result callback
- [ ] 12.6 Configure logging (Slf4j + existing Logback config)

## 13. Task Queue - Frontend (Display Only)

> ⚠️ 前端仅负责显示，不包含业务逻辑

- [ ] 13.1 Create `src/api/taskQueue.ts` - frontend API client (call Spring Boot API)
- [ ] 13.2 Create `src/components/TaskQueue/TaskList.tsx` - task list UI with **backend pagination**
- [ ] 13.3 Create `src/components/TaskQueue/TaskDetail.tsx` - task detail panel (display only)
- [ ] 13.4 Create `src/pages/TaskQueuePage.tsx` - task queue dashboard with pagination controls
- [ ] 13.5 Implement polling/websocket for real-time status updates (display)

## 14. Testing & Polish

- [ ] 14.1 Write unit tests for OpenClaw CLI wrapper
- [ ] 14.2 Test graph CRUD operations with mock data
- [ ] 14.3 Test sync operations with local OpenClaw instance
- [ ] 14.4 Write unit tests for Task Queue API
- [ ] 14.5 Test concurrent task claiming
- [ ] 14.6 Add responsive styling for tablet/desktop
- [ ] 14.7 Add keyboard shortcuts (delete, escape to close dialogs)

---

_Tasks version: 1.1_
_Created: 2026-03-19_
_Updated: 2026-03-19_
