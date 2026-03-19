## ADDED Requirements

### Requirement: Start task

The system SHALL allow starting a task for a specific agent.

#### Scenario: Start task manually

- **WHEN** user selects agent and clicks "Start Task"
- **THEN** task is submitted to OpenClaw queue

### Requirement: Stop task

The system SHALL allow stopping a running task.

#### Scenario: Stop running task

- **WHEN** user clicks "Stop" on running task
- **THEN** system sends termination signal to task

### Requirement: Retry failed task

The system SHALL allow retrying a failed task.

#### Scenario: Retry task

- **WHEN** user clicks "Retry" on failed task
- **THEN** new task instance is created with same parameters

### Requirement: Pause/Resume task

The system SHALL allow pausing and resuming tasks.

#### Scenario: Pause task

- **WHEN** user clicks "Pause" on running task
- **THEN** task state is saved and execution suspends

#### Scenario: Resume task

- **WHEN** user clicks "Resume" on paused task
- **THEN** task continues from saved state

### Requirement: Task status monitoring

The system SHALL display real-time task status.

#### Scenario: View task status

- **WHEN** task is running
- **THEN** status updates every 5 seconds via polling
