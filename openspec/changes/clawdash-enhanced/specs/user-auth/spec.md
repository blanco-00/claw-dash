# User Authentication Specification

## ADDED Requirements

### Requirement: User registration

The system SHALL allow new users to register with username and password.

#### Scenario: Register new user

- **WHEN** user submits registration with username, email, password
- **THEN** system creates user account and sends verification email

#### Scenario: Username already exists

- **WHEN** user registers with existing username
- **THEN** system returns error message

### Requirement: User login

The system SHALL authenticate users and issue session tokens.

#### Scenario: Login with valid credentials

- **WHEN** user logs in with correct username and password
- **THEN** system creates session and returns success with token

#### Scenario: Login with invalid credentials

- **WHEN** user logs in with wrong password
- **THEN** system returns authentication error

### Requirement: Session management

The system SHALL maintain user sessions with Redis.

#### Scenario: Session persists

- **WHEN** authenticated user makes request within session lifetime
- **THEN** system validates session and allows access

#### Scenario: Session expires

- **WHEN** session exceeds lifetime
- **THEN** system requires re-authentication

### Requirement: Role-based access control

The system SHALL support different user roles with varying permissions.

#### Scenario: Admin access

- **WHEN** admin user accesses system
- **THEN** system grants full access to all features

#### Scenario: Regular user access

- **WHEN** regular user accesses system
- **THEN** system restricts to user-specific features

### Requirement: Password management

The system SHALL allow password changes and recovery.

#### Scenario: Change password

- **WHEN** user changes password with current password verification
- **THEN** system updates password and invalidates old sessions

#### Scenario: Reset password

- **WHEN** user requests password reset
- **THEN** system sends reset link to registered email

### Requirement: Local/Production mode switching

The system SHALL support switching between local mode (no auth) and production mode (auth required) via environment variable.

#### Scenario: Local mode - no auth required

- **WHEN** AUTH_ENABLED=false (default)
- **THEN** system allows all requests without authentication

#### Scenario: Production mode - auth required

- **WHEN** AUTH_ENABLED=true
- **THEN** system requires valid JWT token for all API requests

#### Scenario: Invalid token in production mode

- **WHEN** production mode is enabled and request has invalid/missing token
- **THEN** system returns 401 Unauthorized
