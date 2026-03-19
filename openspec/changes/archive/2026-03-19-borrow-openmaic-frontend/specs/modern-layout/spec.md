## ADDED Requirements

### Requirement: Floating header bar

The system SHALL provide a floating header with controls.

#### Scenario: Header positioning

- **WHEN** app is loaded
- **THEN** header floats at top with backdrop blur and translucent background

#### Scenario: Header controls

- **WHEN** header is rendered
- **THEN** it contains theme toggle, language selector, and settings button

### Requirement: Theme toggle dropdown

The system SHALL provide a dropdown for theme selection.

#### Scenario: Theme dropdown open

- **WHEN** user clicks theme toggle
- **THEN** dropdown shows light/dark/system options with icons

#### Scenario: Theme dropdown close

- **WHEN** user clicks outside dropdown
- **THEN** dropdown closes without changing theme

### Requirement: Language selector

The system SHALL provide language toggle (CN/EN).

#### Scenario: Language switch

- **WHEN** user toggles language
- **THEN** UI text updates to selected language

### Requirement: Smooth transitions

The system SHALL provide smooth transitions for all interactive elements.

#### Scenario: Button hover

- **WHEN** button is hovered
- **THEN** background color transitions smoothly (0.2s)

#### Scenario: Icon rotation

- **WHEN** settings icon is hovered
- **THEN** it rotates 90 degrees over 0.5 seconds

### Requirement: Professional styling

The system SHALL match OpenMAIC's professional aesthetic.

#### Scenario: Font styling

- **WHEN** text is rendered
- **THEN** it uses system font stack with proper line height
