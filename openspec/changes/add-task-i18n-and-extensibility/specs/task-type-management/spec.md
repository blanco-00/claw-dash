## ADDED Requirements

### Requirement: Task Types are stored in database
The system SHALL store Task Types in a database table, allowing dynamic configuration.

#### Scenario: Task types table exists
- **WHEN** application starts
- **THEN** a `task_type` table exists with columns: id, name, display_name, description, enabled, created_at, updated_at

#### Scenario: Default task types are seeded
- **WHEN** application initializes for the first time
- **THEN** default task types are created: agent-execute, data-sync, notification, cleanup

### Requirement: Task Types API provides CRUD operations
The system SHALL expose REST API endpoints for managing Task Types.

#### Scenario: List all task types
- **WHEN** client calls GET /api/task-types
- **THEN** returns all task types with their details

#### Scenario: Create new task type
- **WHEN** client calls POST /api/task-types with valid data
- **THEN** new task type is created and returned

#### Scenario: Update task type
- **WHEN** client calls PUT /api/task-types/{id} with valid data
- **THEN** task type is updated and returns updated object

#### Scenario: Delete task type
- **WHEN** client calls DELETE /api/task-types/{id}
- **THEN** task type is deleted (or soft-deleted if tasks reference it)

### Requirement: Task Type dropdown fetches from API
The system SHALL fetch Task Types from the API for the dropdown, not use hardcoded values.

#### Scenario: Create Task dropdown populates from API
- **WHEN** Create Task dialog opens
- **THEN** task type dropdown options are fetched from GET /api/task-types

#### Scenario: Disabled task types are not selectable
- **WHEN** a task type has enabled=false
- **THEN** it does not appear in the Create Task dropdown

### Requirement: Task Type management UI exists
The system SHALL provide a UI for administrators to manage Task Types.

#### Scenario: Admin can view all task types
- **WHEN** admin navigates to Task Type management page
- **THEN** all task types are displayed in a table

#### Scenario: Admin can create new task type
- **WHEN** admin fills form and clicks Create
- **THEN** new task type is created via API

#### Scenario: Admin can edit existing task type
- **WHEN** admin clicks edit on a task type
- **THEN** form appears with current values, can be saved

#### Scenario: Admin can delete task type
- **WHEN** admin clicks delete on a task type
- **THEN** confirmation appears, then task type is deleted
