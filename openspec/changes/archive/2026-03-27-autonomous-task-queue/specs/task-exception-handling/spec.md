## ADDED Requirements

### Requirement: Task retry SHALL use exponential backoff
When a task fails, the system SHALL retry with exponentially increasing delays.

#### Scenario: First retry delay
- **WHEN** task fails with retry_count = 0
- **THEN** system SHALL reschedule with delay = base_delay * 2^0 (e.g., 5 seconds)

#### Scenario: Second retry delay
- **WHEN** task fails with retry_count = 1
- **THEN** system SHALL reschedule with delay = base_delay * 2^1 (e.g., 10 seconds)

#### Scenario: Exponential growth
- **WHEN** task fails multiple times
- **THEN** delay SHALL grow exponentially: 5s, 10s, 20s, 40s...

### Requirement: System SHALL detect when human intervention is needed
When retry_count exceeds max_retries, task SHALL be marked for human intervention.

#### Scenario: Intervention threshold reached
- **WHEN** task fails and retry_count >= max_retries
- **THEN** task.status SHALL be set to "needs_intervention"
- **AND** UI SHALL highlight this task in exception view

### Requirement: UI SHALL display tasks needing intervention
TaskQueue UI SHALL show tasks that require human intervention with clear actions.

#### Scenario: Exception task display
- **WHEN** task has status "needs_intervention"
- **THEN** UI SHALL display task in separate "需要介入" (needs intervention) section
- **AND** UI SHALL show retry_count, last_error, and assigned_agent

### Requirement: Human SHALL be able to reassign failed tasks
Users SHALL be able to manually reassign tasks that exceeded retry limits.

#### Scenario: Reassignment action
- **WHEN** user clicks "重新指派" (reassign) on failed task
- **THEN** system SHALL prompt for new agent selection
- **AND** upon confirmation, assigned_agent SHALL be updated
- **AND** task SHALL be re-queued for execution

### Requirement: Human SHALL be able to abandon tasks
Users SHALL be able to explicitly mark intervention-needed tasks as abandoned.

#### Scenario: Abandon task action
- **WHEN** user clicks "放弃" (abandon) on failed task
- **THEN** task_groups.status SHALL be set to "failed"
- **AND** task SHALL be removed from active queue
