## ADDED Requirements

### Requirement: Command Parser SHALL parse `#task` prefix
The system SHALL recognize messages starting with `#task` as task creation requests. The parser SHALL strip the prefix and extract the task description.

#### Scenario: Basic task command
- **WHEN** user sends message "#task 修一下登录bug"
- **THEN** system SHALL extract task description as "修一下登录bug"

#### Scenario: Task with extra whitespace
- **WHEN** user sends message "#task   修一下登录bug  "
- **THEN** system SHALL extract task description as "修一下登录bug" (trimmed)

### Requirement: Parser SHALL detect priority from natural language
The parser SHALL analyze task description and detect urgency level based on keywords.

#### Scenario: Urgent keywords detected
- **WHEN** task description contains "挂了", "崩了", "urgent", "紧急"
- **THEN** system SHALL set priority to "urgent"

#### Scenario: High priority keywords detected
- **WHEN** task description contains "一直", "总是", "critical"
- **THEN** system SHALL set priority to "high"

#### Scenario: Default priority
- **WHEN** task description contains no urgency keywords
- **THEN** system SHALL set priority to "medium"

### Requirement: Parser SHALL detect explicit decomposition request
The parser SHALL recognize when user explicitly requests task decomposition.

#### Scenario: Explicit decomposition request
- **WHEN** task description contains "拆解", "分解", "分步骤"
- **THEN** system SHALL set requiresDecompose to true
