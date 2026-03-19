# Jiner Modules Specification

## ADDED Requirements

### Requirement: Finance management

The system SHALL allow users to track income, expenses, budgets, and generate monthly reports.

#### Scenario: Record income

- **WHEN** user adds income entry with amount, category, date, description
- **THEN** system stores income record and updates balance

#### Scenario: Record expense

- **WHEN** user adds expense entry with amount, category, date, description
- **THEN** system stores expense record and updates balance

#### Scenario: View monthly report

- **WHEN** user views monthly financial report
- **THEN** system displays income, expenses, savings rate by category

#### Scenario: Set budget

- **WHEN** user sets monthly budget for category
- **THEN** system tracks spending against budget and shows warnings when exceeded

### Requirement: Health tracking

The system SHALL allow users to log water intake and exercise activities, with smart reminders based on progress.

#### Scenario: Set daily goal

- **WHEN** user sets daily water goal (e.g., 2000ml)
- **THEN** system stores goal and tracks progress

#### Scenario: Log water intake

- **WHEN** user logs water consumption (e.g., "500ml")
- **THEN** system records intake and updates daily total

#### Scenario: View water progress

- **WHEN** user views water intake progress
- **THEN** system shows current intake vs goal with percentage

#### Scenario: Smart reminder - goal not met

- **WHEN** water intake is below goal at reminder time
- **THEN** system sends reminder notification

#### Scenario: Smart reminder - goal exceeded

- **WHEN** water intake exceeds goal at reminder time
- **THEN** system skips reminder (no notification needed)

#### Scenario: Log exercise

- **WHEN** user logs exercise activity with type, duration, calories
- **THEN** system records exercise and updates daily summary

#### Scenario: Set exercise goal

- **WHEN** user sets daily exercise goal (e.g., 30 minutes)
- **THEN** system stores goal and tracks progress

#### Scenario: Smart exercise reminder - goal not met

- **WHEN** exercise time is below goal at reminder time
- **THEN** system sends reminder notification

#### Scenario: Smart exercise reminder - goal exceeded

- **WHEN** exercise time exceeds goal at reminder time
- **THEN** system skips reminder (no notification needed)
