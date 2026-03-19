## ADDED Requirements

### Requirement: Glow animation

The system SHALL provide a pulsing glow animation for setup indicators.

#### Scenario: Setup needed indicator

- **WHEN** element has `animate-setup-glow` class
- **THEN** it displays a purple pulsing glow effect (2.5s cycle)

### Requirement: Shimmer loading effect

The system SHALL provide a shimmer effect for loading skeletons.

#### Scenario: Loading skeleton

- **WHEN** element has `animate-shimmer` class
- **THEN** it displays a sweep animation from left to right

### Requirement: Wave animation

The system SHALL provide a wave animation for audio visualization.

#### Scenario: Audio visualizer

- **WHEN** element has `animate-wave` class
- **THEN** it animates height between 30% and 100% in a wave pattern

### Requirement: Breathe border animation

The system SHALL provide a subtle border breathing effect.

#### Scenario: Setup border indicator

- **WHEN** element has `animate-setup-border` class
- **THEN** it displays a subtle purple border pulse (2.5s cycle)

### Requirement: Fade transitions

The system SHALL provide smooth fade transitions for Vue route changes.

#### Scenario: Route transition

- **WHEN** Vue route changes
- **THEN** content fades in/out over 0.3 seconds
