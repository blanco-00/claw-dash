## ADDED Requirements

### Requirement: TaskQueue UI SHALL display task groups
The TaskQueue view SHALL show task groups as parent items with their subtasks nested.

#### Scenario: Task group card display
- **WHEN** task group has in-progress subtasks
- **THEN** UI SHALL display group card with title, progress (X/Y completed), and status badge

#### Scenario: Task group status badges
- **WHEN** task group status is "in_progress"
- **THEN** UI SHALL show blue badge
- **AND** status "completed" SHALL show green badge
- **AND** status "failed" SHALL show red badge

### Requirement: Task group detail view SHALL show all subtasks
Selecting a task group SHALL open a drawer showing full details.

#### Scenario: Subtask list in drawer
- **WHEN** user clicks task group
- **THEN** drawer SHALL display list of all subtasks with their assigned agents and statuses

#### Scenario: Context display
- **WHEN** subtask is selected in drawer
- **THEN** drawer SHALL show: totalGoal, overallDesign, and this subtask's specific objective

### Requirement: UI SHALL highlight tasks needing intervention
Tasks with status "needs_intervention" SHALL be visually distinct.

#### Scenario: Exception highlighting
- **WHEN** task has retry_count >= max_retries
- **THEN** UI SHALL show red background/border
- **AND** UI SHALL display retry_count and last_error message

### Requirement: Intervention actions SHALL be accessible from UI
For tasks needing intervention, UI SHALL provide quick action buttons.

#### Scenario: Action buttons display
- **WHEN** task needs intervention
- **THEN** UI SHALL show buttons: [重新指派] (reassign), [查看详情] (details), [放弃] (abandon)
