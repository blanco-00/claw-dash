## ADDED Requirements

### Requirement: List OpenClaw agents in Config Graph

The system SHALL display all OpenClaw agents in the Config Graph by reading from the OpenClaw CLI (`openclaw agents list`).

#### Scenario: Load agents on page init
- **WHEN** user navigates to Config Graph page
- **THEN** system calls `openclaw agents list` and displays all agents as nodes

#### Scenario: Refresh agents list
- **WHEN** user clicks Refresh button
- **THEN** system re-fetches agents from OpenClaw and updates the display

### Requirement: Add agent to Config Graph via CLI

The system SHALL allow users to add a new agent to the Config Graph by calling `openclaw agents add <name>`.

#### Scenario: Add new agent
- **WHEN** user clicks "Add Node" and enters a new agent name
- **THEN** system calls `openclaw agents add <name>` to create the agent
- **AND** the new agent appears as a node in the Config Graph

#### Scenario: Add existing OpenClaw agent to graph
- **WHEN** user selects an existing OpenClaw agent from the picker
- **THEN** system adds a reference node to the Config Graph

### Requirement: Delete agent from OpenClaw

The system SHALL allow users to delete an agent by calling `openclaw agents delete <name>`.

#### Scenario: Delete agent confirmation
- **WHEN** user selects a node and clicks Delete
- **THEN** system shows confirmation dialog
- **AND** if confirmed, calls `openclaw agents delete <name>`
- **AND** removes the node from Config Graph

### Requirement: Visualize A2A relationships as edges

The system SHALL display agent-to-agent relationships as edges in the Config Graph, stored in ClawDash database.

#### Scenario: Draw edge between agents
- **WHEN** user drags from one node to another
- **THEN** system creates an edge representing A2A relationship
- **AND** edge is saved to ClawDash database

#### Scenario: Edge types
- **WHEN** user creates an edge
- **THEN** user can select edge type: Assigns, Reports To, or Communicates
- **AND** edge is styled according to type (different colors)

### Requirement: Node position persistence

The system SHALL save and restore node positions in the Config Graph.

#### Scenario: Drag and save position
- **WHEN** user drags a node to a new position
- **THEN** system saves the position to config_graph_nodes table

#### Scenario: Restore positions on load
- **WHEN** user refreshes the page
- **THEN** nodes appear at their saved positions

### Requirement: Detect missing OpenClaw agents

The system SHALL detect when a Config Graph node references a non-existent OpenClaw agent.

#### Scenario: Missing agent warning
- **WHEN** OpenClaw no longer has an agent that exists in Config Graph
- **THEN** system shows a warning indicator on the orphaned node
- **AND** prompts user to remove or recreate the node
