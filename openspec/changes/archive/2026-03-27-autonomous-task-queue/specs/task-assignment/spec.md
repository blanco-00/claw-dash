## ADDED Requirements

### Requirement: Assigner SHALL select best-fit agent based on capabilities
MenxiaSheng SHALL analyze agent specialties and select the most appropriate agent for each task.

#### Scenario: Domain matching
- **WHEN** task relates to engineering/development
- **THEN** Assigner SHALL prefer gongbu (工部)
- **AND** task relating to finance SHALL prefer hubu (户部)
- **AND** task relating to security SHALL prefer bingbu (兵部)

#### Scenario: Unknown domain
- **WHEN** task does not clearly match any agent specialty
- **THEN** Assigner SHALL use default routing based on agent availability

### Requirement: Assigner SHALL set assignedAgent on subtasks
For both simple and decomposed tasks, Assigner SHALL set the assignedAgent field.

#### Scenario: Simple task assignment
- **WHEN** task is marked as simple
- **THEN** Assigner SHALL directly set assignedAgent to best-fit agent
- **AND** task SHALL enter queue for agent to claim

#### Scenario: Complex task - multiple assignments
- **WHEN** task is decomposed into multiple subtasks
- **THEN** Assigner SHALL set different assignedAgent for each subtask

### Requirement: Assigner SHALL use MenxiaSheng as unified entry
All tasks SHALL flow through MenxiaSheng regardless of complexity.

#### Scenario: Simple task flow
- **WHEN** user submits simple task via #task
- **THEN** task SHALL be routed to MenxiaSheng
- **AND** MenxiaSheng SHALL assign directly without decomposition

#### Scenario: Complex task flow
- **WHEN** user submits complex task via #task
- **THEN** task SHALL be routed to MenxiaSheng
- **AND** MenxiaSheng SHALL decompose and assign subtasks
