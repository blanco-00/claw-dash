## ADDED Requirements

### Requirement: ClawDash SHALL notify main agent when TaskGroup completes

When all subtasks in a TaskGroup are completed, ClawDash SHALL send a webhook to the main agent with a summary.

#### Scenario: TaskGroup completes successfully
- **WHEN** last subtask calls `POST /api/task-queue/tasks/{id}/complete`
- **AND** this was the last pending subtask in TaskGroup
- **THEN** ClawDash SHALL set TaskGroup status to "COMPLETED"
- **AND** ClawDash SHALL call `POST /hooks/agent` with:
  - `message`: "TaskGroup {title} completed successfully. {completedCount} subtasks finished."
  - `agentId`: main agent (from TaskGroup or system config)
  - `sessionKey`: `clawdash:taskgroup:complete:{taskGroupId}`
  - `deliver`: true

#### Scenario: TaskGroup completes with some failures
- **WHEN** TaskGroup reaches terminal state with some subtasks failed
- **THEN** ClawDash SHALL set TaskGroup status to "COMPLETED_WITH_ERRORS"
- **AND** webhook message SHALL include failure summary

### Requirement: Main agent receives structured completion report

The completion notification SHALL include structured data for the agent to formulate response.

#### Scenario: Completion webhook payload
- **WHEN** ClawDash sends completion webhook to main agent
- **THEN** message SHALL include:
  ```
  TaskGroup: {title}
  Status: COMPLETED
  Total Subtasks: {count}
  Completed: {completedCount}
  Failed: {failedCount}
  
  Summary: {auto-generated from subtask results}
  ```
- **AND** agent SHALL be able to call `GET /api/task-groups/{id}/detail` for full report

### Requirement: User can configure main agent for notification

System SHALL allow configuration of which agent receives TaskGroup completion notifications.

#### Scenario: Default main agent configured
- **WHEN** system is set up
- **THEN** default main agent SHALL be configured in ClawDash settings
- **AND** all TaskGroup completions SHALL notify this agent

#### Scenario: Per-TaskGroup main agent
- **WHEN** user creates TaskGroup
- **AND** specifies a different reportToAgent
- **THEN** completion notification SHALL be sent to that specific agent
- **AND** override SHALL persist for that TaskGroup

### Requirement: Fallback notification if main agent unavailable

If the designated main agent cannot be reached, system SHALL handle gracefully.

#### Scenario: Main agent webhook fails
- **WHEN** ClawDash tries to send completion webhook
- **AND** OpenClaw returns error or times out
- **THEN** ClawDash SHALL log the failure
- **AND** notification SHALL be marked as "pending"
- **AND** system SHALL retry on configured interval
- **AND** user SHALL be notified via UI that "main agent notification pending"
