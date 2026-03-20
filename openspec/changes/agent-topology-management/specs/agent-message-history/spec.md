# Agent Message History

## ADDED Requirements

### Requirement: Query A2A message history

The system SHALL store and provide queryable history of inter-agent messages.

#### Scenario: Store outgoing message
- **WHEN** agent sends message to another agent via channel
- **THEN** system logs message to a2a_messages table with from, to, content, timestamp

#### Scenario: Query messages by sender
- **WHEN** user queries with `from=<agentId>`
- **THEN** system returns all messages sent by that agent

#### Scenario: Query messages by receiver
- **WHEN** user queries with `to=<agentId>`
- **THEN** system returns all messages received by that agent

#### Scenario: Query messages by time range
- **WHEN** user queries with `since=<timestamp>`
- **THEN** system returns messages sent after that time

### Requirement: Message history shows delivery status

The system SHALL track whether messages were delivered.

#### Scenario: Mark delivered
- **WHEN** receiving agent acknowledges message
- **THEN** system updates message status to `delivered`
- **AND** system records delivered_at timestamp

#### Scenario: Mark failed
- **WHEN** message delivery fails
- **THEN** system updates message status to `failed`
- **AND** system records failure reason if available

### Requirement: Message history is separate from TaskQueue logs

The system SHALL NOT mix A2A messages with TaskQueue task records.

#### Scenario: Task records separate
- **WHEN** TaskQueue creates task delegation record
- **THEN** system does NOT create a2a_messages entry
- **AND** TaskQueue maintains its own task_* tables

### Requirement: Send test message

The system SHALL allow sending a test message between agents.

#### Scenario: Send test message
- **WHEN** user opens Agent Detail Panel and clicks "Send Test Message"
- **THEN** system prompts for target agent selection
- **AND** user enters message content
- **AND** system sends via OpenClaw channel mechanism
- **AND** system records sent message in a2a_messages
