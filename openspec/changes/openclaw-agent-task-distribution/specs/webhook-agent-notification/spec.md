## ADDED Requirements

### Requirement: ClawDash SHALL send webhook notification when TaskGroup is created

When a user creates a TaskGroup via UI or API, ClawDash SHALL send a webhook notification to OpenClaw to wake the TaskDistributorAgent.

#### Scenario: TaskGroup created via UI
- **WHEN** user clicks "Create TaskGroup" and submits
- **THEN** ClawDash SHALL create TaskGroup with status PENDING
- **AND** ClawDash SHALL call `POST /hooks/agent` on OpenClaw with:
  - `message`: "New TaskGroup created. Call GET /api/task-groups/{id}/detail to decompose."
  - `agentId`: configured TaskDistributor agent ID
  - `sessionKey`: `clawdash:task-group:{taskGroupId}`
  - `wakeMode`: "now"

#### Scenario: TaskGroup created via API
- **WHEN** external system calls `POST /api/task-groups`
- **THEN** same webhook notification flow SHALL be triggered

### Requirement: ClawDash SHALL send webhook when subtask is ready for specific agent

When a subtask is created with assignedAgent set, ClawDash SHALL notify that specific OpenClaw agent.

#### Scenario: Subtask created with assignedAgent
- **WHEN** TaskDistributorAgent creates a subtask via `POST /api/task-queue/tasks`
- **AND** assignedAgent is set to a specific agent ID
- **THEN** ClawDash SHALL immediately call `POST /hooks/agent` with:
  - `message`: "New task assigned to you. TaskId: {taskId}, Title: {title}"
  - `agentId`: the assignedAgent value
  - `sessionKey`: `clawdash:task:{taskId}`
  - `wakeMode`: "now"

#### Scenario: Webhook notification fails
- **WHEN** ClawDash cannot reach OpenClaw webhook endpoint
- **THEN** ClawDash SHALL log the failure with error details
- **AND** ClawDash SHALL mark task as "notification_pending" status
- **AND** ClawDash SHALL retry notification on next poll cycle (60s interval)

### Requirement: Webhook payload format

Webhook notifications SHALL use OpenClaw's native `/hooks/agent` endpoint format.

#### Scenario: Valid webhook payload
- **WHEN** ClawDash sends webhook to OpenClaw
- **THEN** payload SHALL be:
  ```json
  {
    "message": "string (required) - Human-readable instruction",
    "agentId": "string (optional) - Override default agent",
    "sessionKey": "string (required) - Unique session for this task",
    "wakeMode": "now" | "next-heartbeat",
    "deliver": true
  }
  ```
