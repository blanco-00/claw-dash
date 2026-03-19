## ADDED Requirements

### Requirement: Backdrop blur utility

The system SHALL provide backdrop blur for glassmorphism effects.

#### Scenario: Glass header

- **WHEN** element has backdrop blur styling
- **THEN** it displays frosted glass effect over content behind it

### Requirement: Translucent backgrounds

The system SHALL provide semi-transparent background utilities.

#### Scenario: Floating toolbar

- **WHEN** toolbar has translucent background
- **THEN** it uses rgba white with 60% opacity (60/100 = 0.6) in light mode

### Requirement: Border transparency

The system SHALL support semi-transparent border colors.

#### Scenario: Subtle borders

- **WHEN** subtle borders are needed
- **THEN** use `oklch(1 0 0 / 10%)` format for 10% opacity white borders

### Requirement: Shadow utilities

The system SHALL provide shadow utilities for depth.

#### Scenario: Elevated card

- **WHEN** card is elevated
- **THEN** it uses subtle shadow for depth perception
