# Implementation Tasks

## 1. Backend API Changes

- [x] 1.1 Add `NotifyAgentRequest` DTO class ✅
- [x] 1.2 Add `POST /api/task-queue/notify-agent` endpoint in TaskQueueController ✅
- [x] 1.3 Add `assignedAgent` parameter to `GET /api/task-queue/tasks` endpoint ✅
- [x] 1.4 Add `GET /api/task-queue/agent-stats` endpoint for agent workload metrics ✅
- [x] 1.5 Add `GET /api/task-groups/{id}/detail` endpoint (if not exists) ✅ (via TaskGroupController)
- [x] 1.6 Inject OpenClawConfig into TaskQueueService for webhook URL ✅

## 2. TaskQueueService Changes

- [x] 2.1 Modify `claimTask()` to respect assignedAgent - reject if caller is not the assigned agent ✅
- [x] 2.2 Add `notifyAssignedAgent(task)` method that calls OpenClaw webhook ✅
- [x] 2.3 Call `notifyAssignedAgent()` after `createTask()` when assignedAgent is set ✅
- [x] 2.4 Modify `completeTask()` to check if last subtask and trigger completion notification ✅
- [x] 2.5 Add `syncTaskGroupStatus()` call after subtask complete/fail ✅
- [x] 2.6 Add `notifyMainAgentOnComplete(taskGroupId)` method ✅

## 3. OpenClaw Skill Creation

- [x] 3.1 Create `~/.openclaw/workspace/skills/task-distribution/` directory structure ✅
- [x] 3.2 Write SKILL.md with task decomposition instructions, API call examples ✅
- [x] 3.3 Add skill to ClawDash skill installation flow in OpenClawService ✅
- [x] 3.4 Update OpenClawService to include new skill in skill list ✅

## 4. Frontend UI - Agent Task Configuration

- [x] 4.1 Create new page `frontend/src/views/AgentTaskConfig.vue`
- [x] 4.2 Add route for `/agent-task-config` in router
- [x] 4.3 Add menu item under Agents menu
- [x] 4.4 Implement agent dropdown selector for TaskDistributor
- [x] 4.5 Implement agent binding table (agent → task types)
- [x] 4.6 Implement workload stats per agent (pending/running/completed)
- [x] 4.7 Add "Add Binding" dialog

## 5. Frontend UI - TaskGroup Enhancements

- [x] 5.1 Extend TaskGroup detail drawer to show subtasks grouped by agent
- [x] 5.2 Add progress bar showing completed/total subtasks
- [x] 5.3 Add per-agent subtask status indicators
- [x] 5.4 Style components following existing ClawDash Design System

## 6. Frontend API Client

- [x] 6.1 Add `notifyAgent(data)` function in taskQueueApi.ts
- [x] 6.2 Add `getAgentStats(agentId)` function in taskQueueApi.ts
- [x] 6.3 Add types for NotifyAgentRequest and AgentStats

## 7. Backend Tests

- [x] 7.1 Write TaskQueueControllerTest for notify-agent endpoint ✅
- [x] 7.2 Write TaskQueueControllerTest for assignedAgent filtering ✅
- [x] 7.3 Write TaskQueueServiceTest for claimTask with assignedAgent ✅
- [x] 7.4 Write TaskQueueServiceTest for webhook notification on task create ✅
- [x] 7.5 Write TaskQueueServiceTest for completion notification flow ✅

## 8. Browser Tests (Playwright)

- [x] 8.1 Test: Create TaskGroup and verify webhook notification sent ✅
- [x] 8.2 Test: Verify subtask created with assignedAgent triggers correct webhook ✅
- [x] 8.3 Test: Agent cannot claim task assigned to different agent ✅
- [x] 8.4 Test: Agent Task Configuration page loads with correct data ✅
- [x] 8.5 Test: TaskGroup detail shows per-agent progress ✅
- [x] 8.6 Test: Main agent receives completion webhook when TaskGroup completes ✅

## 9. Integration Tests

- [x] 9.1 Test full flow: Create TaskGroup → Decompose → Subtask execute → Complete ✅
- [x] 9.2 Test exponential backoff on subtask failure ✅
- [x] 9.3 Test notification retry when webhook fails ✅

## 10. Documentation

- [x] 10.1 Update USER_GUIDE.md with new agent task configuration ✅
- [x] 10.2 Document OpenClaw skill installation process ✅
- [x] 10.3 Add API documentation for new endpoints ✅
