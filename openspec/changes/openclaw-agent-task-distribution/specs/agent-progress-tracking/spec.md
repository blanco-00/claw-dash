## ADDED Requirements

### Requirement: ClawDash SHALL track progress of all subtasks in TaskGroup

The system SHALL provide real-time progress tracking for TaskGroups and their subtasks.

#### Scenario: Progress updates on subtask completion
- **WHEN** a subtask is completed via `POST /api/task-queue/tasks/{id}/complete`
- **THEN** ClawDash SHALL call `syncTaskGroupStatus(taskGroupId)`
- **AND** TaskGroup progress SHALL be recalculated
- **AND** UI SHALL reflect updated progress (e.g., "2/5 completed")

#### Scenario: Progress updates on subtask failure
- **WHEN** a subtask fails after max retries
- **THEN** TaskGroup status SHALL be set to "NEEDS_INTERVENTION"
- **AND** UI SHALL highlight the failed subtask
- **AND** user SHALL be prompted to reassign or abandon

### Requirement: Frontend SHALL display agent workload

ClawDash UI SHALL show each agent's current task load.

#### Scenario: Agent workload shown in configuration page
- **WHEN** user navigates to Agent Task Configuration
- **THEN** table SHALL display for each agent:
  - Pending task count
  - Running task count
  - Completed task count (today/this week)
  - Current workload percentage

#### Scenario: TaskGroup detail shows per-agent progress
- **WHEN** user opens TaskGroup detail drawer
- **THEN** subtasks SHALL be grouped by assignedAgent
- **AND** each agent's subtasks SHALL show individual status
- **AND** overall TaskGroup progress bar SHALL be displayed

### Requirement: TaskGroup status reflects aggregate state

TaskGroup status SHALL be derived from its subtasks' statuses.

#### Scenario: All subtasks complete
- **WHEN** last subtask in TaskGroup is completed
- **THEN** ClawDash SHALL set TaskGroup status to "COMPLETED"
- **AND** TaskGroup.completedAt SHALL be set
- **AND** completion webhook SHALL be sent to main agent

#### Scenario: Any subtask needs intervention
- **WHEN** any subtask retry count exceeds maxRetries
- **THEN** TaskGroup status SHALL be set to "NEEDS_INTERVENTION"
- **AND** UI SHALL show warning banner

#### Scenario: TaskGroup partially complete
- **WHEN** some subtasks complete and others are still pending/running
- **THEN** TaskGroup status SHALL be "IN_PROGRESS"
- **AND** progress percentage SHALL be shown

### Requirement: Statistics API for agent task metrics

System SHALL provide API endpoint for agent task statistics.

#### Scenario: Get agent task stats
- **WHEN** frontend calls `GET /api/task-queue/agent-stats?agentId={id}`
- **THEN** response SHALL include:
  ```json
  {
    "agentId": "gongbu",
    "pending": 3,
    "running": 1,
    "completedToday": 12,
    "completedThisWeek": 45,
    "failed": 2,
    "averageCompletionTimeMinutes": 15.5
  }
  ```
