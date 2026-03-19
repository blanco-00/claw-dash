## ADDED Requirements

### Requirement: Token usage tracking

The system SHALL track token consumption per agent and task.

#### Scenario: Track token usage

- **WHEN** session completes
- **THEN** system parses token usage from session data

#### Scenario: View token stats

- **WHEN** user views token dashboard
- **THEN** system shows total tokens per agent and time period

### Requirement: Token cost calculation

The system SHALL calculate cost based on token usage and model pricing.

#### Scenario: Calculate cost

- **WHEN** tokens are recorded
- **THEN** system calculates cost using configured model prices

### Requirement: Token history

The system SHALL display historical token usage.

#### Scenario: View history

- **WHEN** user selects date range
- **THEN** system shows token usage chart
