## ADDED Requirements

### Requirement: TaskGroup creation SHALL trigger decomposition workflow

When a user creates a TaskGroup, the system SHALL automatically initiate the decomposition process by notifying the TaskDistributorAgent.

#### Scenario: User creates TaskGroup via UI
- **WHEN** user fills in TaskGroup form (title, description, totalGoal)
- **AND** clicks "Create"
- **THEN** TaskGroup SHALL be saved with status "PENDING"
- **AND** decomposerAgentId SHALL be set to configured TaskDistributor
- **AND** webhook SHALL be sent to TaskDistributorAgent

#### Scenario: TaskGroup requires decomposition
- **WHEN** TaskGroup is created with multiple goals or complex description
- **AND** user did not explicitly mark as "simple"
- **THEN** TaskDistributorAgent SHALL be notified to decompose
- **AND** TaskGroup status SHALL remain PENDING until decomposition complete

#### Scenario: TaskGroup marked as simple (no decomposition needed)
- **WHEN** user explicitly marks TaskGroup as "simple"
- **THEN** TaskGroup SHALL be decomposed into single subtask immediately
- **AND** subtask SHALL be assigned to appropriate agent based on task type

### Requirement: Decomposition result SHALL update TaskGroup

After TaskDistributorAgent decomposes, the TaskGroup SHALL reflect the decomposition.

#### Scenario: Decomposition creates subtasks
- **WHEN** TaskDistributorAgent calls `POST /api/task-queue/tasks` for each subtask
- **AND** each subtask has taskGroupId set
- **THEN** TaskGroup task count SHALL be updated
- **AND** TaskGroup status SHALL transition to "IN_PROGRESS"

#### Scenario: Decomposition fails
- **WHEN** TaskDistributorAgent cannot decompose TaskGroup
- **THEN** agent SHALL report failure via task result
- **AND** TaskGroup status SHALL be set to "NEEDS_INTERVENTION"
- **AND** UI SHALL show warning to user

### Requirement: User can add subtasks to existing TaskGroup

User can manually add subtasks to an existing TaskGroup at any time.

#### Scenario: User adds subtask to existing TaskGroup
- **WHEN** user opens TaskGroup detail and clicks "Add Subtask"
- **AND** fills in subtask details and specifies assignedAgent
- **THEN** subtask SHALL be created with taskGroupId set
- **AND** webhook SHALL be sent to the specified assignedAgent
- **AND** TaskGroup status SHALL remain as-is (not reset to PENDING)

#### Scenario: User adds subtask without specifying agent
- **WHEN** user adds subtask without assignedAgent
- **THEN** system SHALL treat as needing TaskDistributor decomposition
- **AND** webhook SHALL be sent to TaskDistributorAgent
