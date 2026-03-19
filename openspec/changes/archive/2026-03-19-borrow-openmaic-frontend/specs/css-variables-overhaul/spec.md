## ADDED Requirements

### Requirement: Primary color palette

The system SHALL use purple (#722ed1) as the primary brand color, matching OpenMAIC's aesthetic.

#### Scenario: Primary color in light mode

- **WHEN** light mode is active
- **THEN** `--primary: #722ed1` color is used for main actions and highlights

#### Scenario: Primary color in dark mode

- **WHEN** dark mode is active
- **THEN** `--primary: #8b47ea` (lighter purple) is used for better contrast

### Requirement: Background colors

The system SHALL define proper background colors for light and dark modes.

#### Scenario: Light mode background

- **WHEN** light mode is active
- **THEN** `--background: oklch(1 0 0)` (pure white) and `--bg-secondary: oklch(0.97 0 0)` are used

#### Scenario: Dark mode background

- **WHEN** dark mode is active
- **THEN** `--background: oklch(0.145 0 0)` (dark gray) and `--bg-secondary: oklch(0.205 0 0)` are used

### Requirement: Element Plus theme integration

The system SHALL override Element Plus CSS variables for visual consistency.

#### Scenario: Button primary color

- **WHEN** Element Plus button with type="primary" is rendered
- **THEN** it uses the purple primary color via `--el-color-primary`

#### Scenario: Input focus state

- **WHEN** Element Plus input receives focus
- **THEN** it uses the purple ring color via `--el-color-primary-light-3`

### Requirement: Border and radius tokens

The system SHALL define consistent border colors and radius values.

#### Scenario: Card borders

- **WHEN** cards are rendered
- **THEN** they use `--border: oklch(0.922 0 0)` in light mode

#### Scenario: Rounded corners

- **WHEN** UI elements are rendered
- **THEN** they use `--radius: 0.625rem` (10px) for consistent rounding

### Requirement: Text colors

The system SHALL define proper text color tokens for primary and secondary content.

#### Scenario: Primary text

- **WHEN** main content text is rendered
- **THEN** `--text-primary` is used for high contrast

#### Scenario: Secondary text

- **WHEN** supporting text is rendered
- **THEN** `--text-secondary` is used for lower contrast
