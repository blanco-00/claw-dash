# Agent Files Editor

## ADDED Requirements

### Requirement: View agent workspace files

The system SHALL list and display SOUL.md, AGENT.md, and USER.md files from agent's workspace.

#### Scenario: List workspace files
- **WHEN** user opens Agent Detail Panel
- **THEN** system reads files from agent's workspace directory
- **AND** system displays file list: SOUL.md, AGENT.md, USER.md

#### Scenario: File not found
- **WHEN** a file (e.g., USER.md) does not exist in workspace
- **THEN** system shows file as "Not created" with option to create

### Requirement: Edit agent files in UI

The system SHALL provide in-browser text editing for workspace files.

#### Scenario: Edit SOUL.md
- **WHEN** user clicks "Edit" on SOUL.md
- **THEN** system opens Monaco editor with file content
- **AND** user can modify content
- **AND** user clicks "Save" to persist changes

#### Scenario: Create new file
- **WHEN** user clicks "Create" on a non-existent file
- **THEN** system creates empty file in workspace
- **AND** system opens editor for new file

### Requirement: File changes persist to filesystem

The system SHALL write edited content back to the agent's workspace directory.

#### Scenario: Save file changes
- **WHEN** user saves edited content
- **THEN** system writes content to `<workspace>/SOUL.md`
- **AND** system shows success notification

#### Scenario: Save failure
- **WHEN** filesystem write fails (permissions)
- **THEN** system shows error message
- **AND** system does not lose user's edits

### Requirement: Open file in external editor

The system SHALL provide option to open file in system default editor.

#### Scenario: Open in external editor
- **WHEN** user clicks "Open in Editor"
- **THEN** system opens file using OS file association
- **AND** user can edit in their preferred editor
