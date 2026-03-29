## ADDED Requirements

### Requirement: TaskDistributorAgent SHALL use ClawDash skill to interact with task queue

The TaskDistribution skill SHALL be installed in OpenClaw workspace at `~/.openclaw/workspace/skills/task-distribution/SKILL.md` and SHALL teach agents how to poll, claim, decompose, and complete tasks via ClawDash REST API.

#### Scenario: Agent receives webhook to decompose TaskGroup
- **WHEN** TaskDistributorAgent receives a webhook notification for TaskGroup decomposition
- **THEN** agent SHALL call `GET /api/task-groups/{id}/detail` to retrieve TaskGroup details
- **AND** agent SHALL analyze the task and decompose into subtasks
- **AND** agent SHALL call `POST /api/task-queue/tasks` for each subtask with assignedAgent set

#### Scenario: Agent creates subtask via API
- **WHEN** agent needs to create a subtask
- **THEN** agent SHALL call `POST /api/task-queue/tasks` with payload:
  ```json
  {
    "type": "agent-execute",
    "payload": { "description": "...", "taskGroupId": "tg-xxx" },
    "assignedAgent": "specific-agent-id",
    "taskGroupId": "tg-xxx",
    "parentTaskId": "parent-subtask-id"
  }
  ```

#### Scenario: Agent completes a subtask
- **WHEN** agent finishes executing a subtask
- **THEN** agent SHALL call `POST /api/task-queue/tasks/{taskId}/complete` with result
- **AND** result SHALL be stored in task.result field

#### Scenario: Agent fails a subtask
- **WHEN** agent encounters an error executing a subtask
- **THEN** agent SHALL call `POST /api/task-queue/tasks/{taskId}/fail` with error message
- **AND** TaskQueueService SHALL handle retry with exponential backoff per existing logic

### Requirement: Skill SHALL support polling fallback

If webhook notification fails, TaskDistributorAgent SHALL periodically poll for new tasks.

#### Scenario: Agent polls for unclaimed TaskGroups
- **WHEN** webhook notification fails or as fallback
- **THEN** agent SHALL call `GET /api/task-groups?status=PENDING&assignedAgent=null`
- **AND** agent SHALL process any TaskGroups not yet assigned a decomposer

#### Scenario: Specific agent polls for assigned tasks
- **WHEN** webhook notification fails for a specific agent
- **THEN** agent SHALL call `GET /api/task-queue/tasks?assignedAgent={agentId}&status=PENDING`
- **AND** agent SHALL claim and execute tasks from the list
