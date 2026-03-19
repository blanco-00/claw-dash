## ADDED Requirements

### Requirement: Agent creation form

The system SHALL provide a GUI form to create new agents with custom properties.

#### Scenario: Create new agent

- **WHEN** user fills form with name, title, role, description and clicks "Create"
- **THEN** system creates agent directory in OpenClaw and saves metadata to database

#### Scenario: Validation error

- **WHEN** user submits form with empty required fields
- **THEN** system shows validation error messages

#### Scenario: Agent ID conflict

- **WHEN** user creates agent with existing ID
- **THEN** system shows error "Agent ID already exists"

### Requirement: Agent edit form

The system SHALL allow editing existing agent properties.

#### Scenario: Edit agent

- **WHEN** user clicks edit button on agent row
- **THEN** form pre-populates with current values

### Requirement: Agent deletion

The system SHALL allow deleting agents with confirmation.

#### Scenario: Delete agent

- **WHEN** user clicks delete and confirms
- **THEN** system removes agent from database (not OpenClaw files)
