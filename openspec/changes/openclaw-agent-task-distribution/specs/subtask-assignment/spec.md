## ADDED Requirements

### Requirement: Subtasks SHALL support assignedAgent field

TaskQueueTask entity already has assignedAgent field. The system SHALL enforce assignment and notification based on this field.

#### Scenario: TaskDistributor creates subtask with assignedAgent
- **WHEN** TaskDistributorAgent calls `POST /api/task-queue/tasks` with assignedAgent
- **THEN** assignedAgent field SHALL be stored in database
- **AND** task status SHALL remain PENDING
- **AND** ClawDash SHALL send webhook to notify the assigned agent

#### Scenario: Task created without assignedAgent
- **WHEN** API call does not include assignedAgent
- **THEN** task SHALL be created with assignedAgent = null
- **AND** task SHALL be processed by Java TaskWorker (existing behavior)
- **OR** task SHALL be picked up by TaskDistributorAgent poll fallback

### Requirement: Only assigned agent SHALL be notified

When a subtask has assignedAgent set, only that specific OpenClaw agent SHALL be notified.

#### Scenario: Specific agent notified for their task
- **WHEN** subtask is created with assignedAgent = "gongbu"
- **THEN** webhook SHALL be sent with agentId = "gongbu"
- **AND** other agents SHALL NOT be notified

#### Scenario: Agent claims their assigned task
- **WHEN** gongbu agent receives webhook notification
- **AND** calls `POST /api/task-queue/tasks/{id}/claim`
- **THEN** claim SHALL succeed if task.assignedAgent equals "gongbu"
- **AND** task status SHALL transition to RUNNING

#### Scenario: Wrong agent tries to claim
- **WHEN** hubu agent tries to claim a task assigned to gongbu
- **THEN** claim SHALL fail with error "Task assigned to different agent"
- **AND** task status SHALL remain unchanged

### Requirement: Agent can query their assigned tasks

Each agent SHALL be able to query tasks specifically assigned to them.

#### Scenario: Agent queries assigned tasks
- **WHEN** agent calls `GET /api/task-queue/tasks?assignedAgent={agentId}&status=PENDING`
- **THEN** API SHALL return only tasks where assignedAgent matches
- **AND** tasks SHALL be ordered by priority ASC, createdAt ASC

#### Scenario: Agent gets task details
- **WHEN** agent claims a task successfully
- **THEN** agent SHALL call `GET /api/task-queue/tasks/{id}` for full details
- **AND** payload SHALL contain task context including parent TaskGroup info
