## ADDED Requirements

### Requirement: Aggregator SHALL collect subtask results via A2A messages
MenxiaSheng SHALL receive notifications when subtasks complete via A2A message system.

#### Scenario: Subtask completion notification received
- **WHEN** MenxiaSheng receives A2A message with type "SUBTASK_COMPLETED"
- **THEN** MenxiaSheng SHALL query database for full subtask details

#### Scenario: Partial result collection
- **WHEN** some subtasks have completed and others are still pending
- **THEN** MenxiaSheng SHALL track completion status without generating final report

### Requirement: Aggregator SHALL detect task group completion
MenxiaSheng SHALL determine when all subtasks in a task group have completed.

#### Scenario: All subtasks completed successfully
- **WHEN** all subtasks in task group have status "completed"
- **THEN** MenxiaSheng SHALL generate full success report

#### Scenario: Some subtasks failed
- **WHEN** some subtasks have status "failed" and others "completed"
- **THEN** MenxiaSheng SHALL generate partial success report with failure analysis

### Requirement: Aggregator SHALL generate meaningful reports
Reports SHALL provide complete context and meaningful summaries, not raw subtask outputs.

#### Scenario: Success report format
- **WHEN** task group completes successfully
- **THEN** report SHALL include: what was done, outcome summary, actionable next steps

#### Scenario: Partial failure report format
- **WHEN** task group has failed subtasks
- **THEN** report SHALL include: what succeeded, what failed, root cause analysis, recommendations

### Requirement: Aggregator SHALL update task group status
MenxiaSheng SHALL update task group status based on subtask completion.

#### Scenario: Task group completion
- **WHEN** all subtasks complete successfully
- **THEN** MenxiaSheng SHALL update task_groups.status to "completed"

#### Scenario: Task group failure
- **WHEN** all subtasks fail or task group is abandoned
- **THEN** MenxiaSheng SHALL update task_groups.status to "failed"
