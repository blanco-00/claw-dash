# Agent Runtime Graph

## ADDED Requirements

### Requirement: Runtime Graph shows actual channel bindings

The system SHALL display the actual OpenClaw agent bindings as a graph visualization derived from `openclaw agents bindings --json`.

#### Scenario: Display current bindings
- **WHEN** user navigates to Runtime Graph tab
- **THEN** system fetches `openclaw agents bindings --json`
- **AND** system renders nodes for each bound agent
- **AND** system renders edges for each channel subscription

#### Scenario: Node status from bindings
- **WHEN** agent has active bindings
- **THEN** node displays as `active` (green indicator)

#### Scenario: Node status when no bindings
- **WHEN** agent has no bindings
- **THEN** node displays as `disconnected` (gray indicator)

### Requirement: Runtime Graph shows connection health

The system SHALL show communication health metrics per edge when available.

#### Scenario: Show message count
- **WHEN** A2A Gateway Plugin provides message stats
- **THEN** edge label shows message count (e.g., "12 msg/h")

#### Scenario: Show latency
- **WHEN** A2A Gateway Plugin provides latency stats
- **THEN** edge displays average latency in milliseconds

#### Scenario: Show success rate
- **WHEN** A2A Gateway Plugin provides delivery stats
- **THEN** edge color indicates success rate (green >90%, yellow 50-90%, red <50%)

### Requirement: Runtime Graph supports manual refresh

The system SHALL refresh data on user action.

#### Scenario: Manual refresh
- **WHEN** user clicks refresh button
- **THEN** system re-fetches bindings from OpenClaw
- **AND** system updates graph visualization

#### Scenario: Time range filter
- **WHEN** user selects time range (1h, 24h, 7d)
- **THEN** system filters message stats to that range

### Requirement: Runtime Graph is separate from TaskQueue

The system SHALL NOT include TaskQueue task delegation data in Runtime Graph.

#### Scenario: Task delegation not shown
- **WHEN** agents communicate via TaskQueue (async tasks)
- **THEN** Runtime Graph does NOT display these as edges
- **AND** TaskQueue remains a separate system for async task management

#### Scenario: Channel-based A2A shown
- **WHEN** agents communicate via OpenClaw channels
- **THEN** Runtime Graph displays these as edges
