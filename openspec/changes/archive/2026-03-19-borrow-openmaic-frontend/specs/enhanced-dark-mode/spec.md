## ADDED Requirements

### Requirement: Theme store with persistence

The system SHALL provide a theme store that persists user preference.

#### Scenario: Theme saved to localStorage

- **WHEN** user changes theme
- **THEN** preference is saved to localStorage under key "theme"

#### Scenario: Theme loaded on startup

- **WHEN** app initializes
- **THEN** it reads theme from localStorage or defaults to "system"

### Requirement: Theme toggle UI

The system SHALL provide a UI toggle for theme switching.

#### Scenario: Light mode toggle

- **WHEN** user clicks light mode option
- **THEN** theme switches to light mode immediately

#### Scenario: Dark mode toggle

- **WHEN** user clicks dark mode option
- **THEN** theme switches to dark mode immediately

#### Scenario: System preference toggle

- **WHEN** user clicks system mode option
- **THEN** theme follows system preference (prefers-color-scheme)

### Requirement: CSS class-based dark mode

The system SHALL apply dark mode via CSS classes on the root element.

#### Scenario: Dark mode active

- **WHEN** dark mode is active
- **THEN** `<html>` or `<body>` element has "dark" class

#### Scenario: Light mode active

- **WHEN** light mode is active
- **THEN** no dark class is present on root element

### Requirement: Real-time theme switching

The system SHALL switch themes without page reload.

#### Scenario: Theme switch speed

- **WHEN** user changes theme
- **THEN** transition completes within 0.3 seconds
