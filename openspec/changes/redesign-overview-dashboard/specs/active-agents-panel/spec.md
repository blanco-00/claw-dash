## ADDED Requirements

### Requirement: Active Agents Panel
The dashboard SHALL display a panel showing all agents with their online/offline status.

#### Scenario: Agents exist
- **WHEN** agents are configured in the system
- **THEN** panel displays agent list with: name, title, and status indicator (green dot = online, gray dot = offline)

#### Scenario: No agents configured
- **WHEN** there are no agents configured
- **THEN** panel shows empty state with "暂无 Agent" message

### Requirement: Agent Status Indicators
The status SHALL be clearly indicated with colored dots.

#### Scenario: Agent is online
- **WHEN** agent status is "online" or "active"
- **THEN** green dot indicator is shown next to the agent name

#### Scenario: Agent is offline
- **WHEN** agent status is not "online" or "active"
- **THEN** gray dot indicator is shown next to the agent name

### Requirement: Click Opens Agent Detail
Clicking an agent SHALL navigate to the agent detail view.

#### Scenario: Click agent row
- **WHEN** user clicks on an agent row
- **THEN** system opens agent detail panel or navigates to agent page

### Requirement: Online Count Display
The panel SHALL show the count of online vs total agents.

#### Scenario: Display agent counts
- **WHEN** agents exist
- **THEN** panel header shows "活跃 Agent: X/Y" where X is online count and Y is total count
