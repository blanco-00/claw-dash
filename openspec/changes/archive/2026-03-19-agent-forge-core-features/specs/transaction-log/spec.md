## ADDED Requirements

### Requirement: Immutable transaction log

The system SHALL maintain an immutable log of all operations.

#### Scenario: Log operation

- **WHEN** agent or task operation occurs
- **THEN** system appends entry with timestamp, operation, result

### Requirement: Audit trail

The system SHALL provide audit trail for financial records.

#### Scenario: View audit trail

- **WHEN** user navigates to audit log
- **THEN** system shows chronological list of all operations

### Requirement: Export transaction log

The system SHALL allow exporting transaction data.

#### Scenario: Export to CSV

- **WHEN** user clicks "Export"
- **THEN** system downloads CSV file with all records
