# Unified Dashboard Specification

## ADDED Requirements

### Requirement: System overview dashboard

The system SHALL display a comprehensive overview of all system components.

#### Scenario: View overview

- **WHEN** user visits dashboard overview page
- **THEN** system shows summary cards: active tasks, agents online, system health

### Requirement: Gateway status monitoring

The system SHALL monitor and display OpenClaw Gateway status.

#### Scenario: View gateway details

- **WHEN** user views gateway status panel
- **THEN** system shows connection status, request rate, error rate

#### Scenario: Gateway offline

- **WHEN** gateway becomes unreachable
- **THEN** system displays warning alert with last known state

### Requirement: Session management

The system SHALL allow viewing and managing OpenClaw sessions.

#### Scenario: List active sessions

- **WHEN** user views session list
- **THEN** system shows all active sessions with duration, messages, agent

#### Scenario: End session

- **WHEN** user requests to end a session
- **THEN** system terminates session and updates status

### Requirement: Token management

The system SHALL allow viewing and managing API tokens.

#### Scenario: List tokens

- **WHEN** user views token list
- **THEN** system shows all tokens with name, created date, last used

#### Scenario: Create token

- **WHEN** user creates new token with name and permissions
- **THEN** system generates token and displays once (never shown again)

#### Scenario: Revoke token

- **WHEN** user revokes a token
- **THEN** system marks token as invalid immediately

### Requirement: Agent list and monitoring

The system SHALL display all registered agents with their status.

#### Scenario: View agent list

- **WHEN** user visits agents page
- **THEN** system shows all agents with status, role, last activity

#### Scenario: View agent details

- **WHEN** user clicks on specific agent
- **THEN** system shows detailed agent information and metrics

### Requirement: Agent configuration

The system SHALL allow configuring agent parameters.

#### Scenario: Update agent config

- **WHEN** user modifies agent configuration
- **THEN** system saves config and applies to agent
