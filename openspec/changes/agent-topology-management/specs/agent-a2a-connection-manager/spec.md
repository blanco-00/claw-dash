# Agent A2A Connection Manager

## ADDED Requirements

### Requirement: View A2A connections per agent

The system SHALL display all A2A connections for a selected agent in the Agent Detail Panel.

#### Scenario: Expand agent row in list
- **WHEN** user clicks expand icon on agent row
- **THEN** system shows outbound connections (where agent is source)
- **AND** system shows inbound connections (where agent is target)

#### Scenario: Connection details shown
- **WHEN** user views agent connections
- **THEN** system shows: remote agent name, edge type, enabled status

### Requirement: Add A2A connection

The system SHALL allow creating new Config Graph edges via the UI.

#### Scenario: Add outbound connection
- **WHEN** user clicks "+ Add Connection" for outbound
- **THEN** system shows agent picker dropdown
- **AND** user selects target agent
- **AND** user selects edge type
- **AND** system creates ConfigEdge in database

#### Scenario: Prevent duplicate edges
- **WHEN** user attempts to create duplicate edge (same source, target, type)
- **THEN** system shows error message
- **AND** system does not create duplicate

### Requirement: Remove A2A connection

The system SHALL allow removing Config Graph edges.

#### Scenario: Remove connection
- **WHEN** user clicks "Remove" on a connection
- **THEN** system deletes ConfigEdge from database
- **AND** UI updates to reflect removal

### Requirement: View OpenClaw bindings for agent

The system SHALL display actual OpenClaw channel bindings for an agent.

#### Scenario: Show OpenClaw bindings
- **WHEN** user opens Agent Detail Panel
- **THEN** system calls `openclaw agents bindings --agent <name>`
- **AND** system displays bound channels list

### Requirement: Create OpenClaw binding

The system SHALL call OpenClaw CLI to create channel binding.

#### Scenario: Bind agent to channel
- **WHEN** user clicks "Bind" and selects channel
- **THEN** system calls `openclaw agents bind --agent <name> --bind <channel>`
- **AND** system updates UI to reflect new binding

### Requirement: Remove OpenClaw binding

The system SHALL call OpenClaw CLI to remove channel binding.

#### Scenario: Unbind agent from channel
- **WHEN** user clicks "Unbind" on a channel
- **THEN** system calls `openclaw agents unbind --agent <name> --bind <channel>`
- **AND** system updates UI to reflect removal
