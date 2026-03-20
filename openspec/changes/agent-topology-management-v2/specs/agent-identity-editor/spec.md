## ADDED Requirements

### Requirement: Display agent identity information

The system SHALL display agent identity information from OpenClaw including name and available metadata.

#### Scenario: View agent list with details
- **WHEN** user navigates to Agent List page
- **THEN** system displays all OpenClaw agents with available information
- **AND** shows name, workspace for each agent

#### Scenario: View agent details
- **WHEN** user clicks on an agent in the list
- **THEN** system shows agent details panel with available information

### Requirement: Edit agent identity via CLI

The system SHALL allow users to edit agent identity by calling `openclaw agents set-identity`.

#### Scenario: Update agent name
- **WHEN** user edits agent name in the details panel
- **THEN** system calls `openclaw agents set-identity --agent <name> --name <newName>`

#### Scenario: Update agent emoji
- **WHEN** user edits agent emoji in the details panel
- **THEN** system calls `openclaw agents set-identity --agent <name> --emoji <emoji>`

### Requirement: Agent workspace display

The system SHALL display the workspace path for each OpenClaw agent.

#### Scenario: Show workspace path
- **WHEN** agent details are displayed
- **THEN** system shows the workspace directory path

### Requirement: Sync agent list from OpenClaw

The system SHALL refresh the agent list by calling `openclaw agents list`.

#### Scenario: Manual refresh
- **WHEN** user clicks Refresh button
- **THEN** system re-fetches agent list from OpenClaw
- **AND** updates the display

#### Scenario: Show last sync time
- **WHEN** agent list is displayed
- **THEN** system shows when the list was last synced
