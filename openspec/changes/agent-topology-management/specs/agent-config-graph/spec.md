# Agent Config Graph

## ADDED Requirements

### Requirement: Config Graph stores agent relationship intentions

The system SHALL store one Config Graph per deployment containing nodes (agents) and edges (relationships). The Config Graph represents intended/design relationships independent of OpenClaw's actual state.

#### Scenario: Create new Config Graph
- **WHEN** user navigates to Agents view for the first time
- **THEN** system creates a default empty Config Graph with unique ID

#### Scenario: Add agent to Config Graph
- **WHEN** user adds an existing Agent to the Config Graph
- **THEN** system creates a ConfigNode linking agentId to the graph
- **AND** node position defaults to center (0, 0) for manual layout

#### Scenario: Remove agent from Config Graph
- **WHEN** user removes a ConfigNode from the graph
- **THEN** system deletes the node
- **AND** system cascades deletion to all edges referencing that node

### Requirement: Config Graph supports three edge types

The system SHALL support three relationship types: `assigns` (task delegation), `reports_to` (hierarchical), and `communicates` (peer-to-peer).

#### Scenario: Create assigns edge
- **WHEN** user creates edge from Agent A to Agent B with type `assigns`
- **THEN** system stores edge with source=A, target=B, type=`assigns`

#### Scenario: Create reports_to edge
- **WHEN** user creates edge from Agent B to Agent A with type `reports_to`
- **THEN** system stores edge with source=B, target=A, type=`reports_to`

#### Scenario: Create communicates edge
- **WHEN** user creates bidirectional communication link between Agent A and Agent B
- **THEN** system stores two directed edges: A→B and B→A with type=`communicates`

### Requirement: Config Graph positions persist

The system SHALL persist node X/Y positions for layout continuity.

#### Scenario: Save node position
- **WHEN** user drags a node to new position
- **THEN** system persists x, y coordinates to database

#### Scenario: Restore layout
- **WHEN** user reloads the page
- **THEN** Config Graph renders with saved positions

### Requirement: Config Graph edges can be enabled/disabled

The system SHALL allow toggling edges without deletion.

#### Scenario: Disable edge
- **WHEN** user toggles edge enabled=false
- **THEN** edge visually appears as dashed/dimmed
- **AND** edge is excluded from OpenClaw sync

#### Scenario: Enable edge
- **WHEN** user toggles edge enabled=true
- **THEN** edge visually appears as solid
- **AND** edge is included in OpenClaw sync

### Requirement: Config Graph syncs to OpenClaw bindings

The system SHALL provide a sync operation to apply Config Graph edges to OpenClaw.

#### Scenario: Sync to OpenClaw
- **WHEN** user clicks "Sync to OpenClaw"
- **THEN** system compares Config edges with OpenClaw bindings
- **AND** system runs `openclaw agents bind` for new edges
- **AND** system runs `openclaw agents unbind` for removed edges
- **AND** system updates `lastSyncedAt` timestamp

#### Scenario: Sync shows divergence
- **WHEN** Config Graph differs from OpenClaw state
- **THEN** system shows warning indicator
- **AND** system displays count of differences
