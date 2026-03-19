# OpenClaw Agent Graph Specification

## Overview

This specification defines the requirements for the OpenClaw Agent Graph Management feature in clawdash.

## ADDED Requirements

### Requirement: User can view agent graph

The system SHALL display all OpenClaw agents as nodes in an interactive graph visualization.

#### Scenario: Display empty graph when no agents exist

- **WHEN** user navigates to the Agent Graph page with no OpenClaw agents configured
- **THEN** system displays an empty graph canvas with a "No agents found" message

#### Scenario: Display agents as nodes

- **WHEN** user navigates to the Agent Graph page with OpenClaw agents present
- **THEN** system displays each agent as a node in a force-directed graph
- **AND** each node shows the agent name and status indicator

#### Scenario: Graph supports pan and zoom

- **WHEN** user interacts with the graph
- **THEN** user can pan (drag canvas) and zoom (scroll/pinch)
- **AND** the graph renders smoothly during interaction

---

### Requirement: User can create new agent node

The system SHALL allow users to create new agent nodes in the graph with metadata.

#### Scenario: Create agent with required fields

- **WHEN** user clicks "Add Agent" button
- **AND** fills in required fields (name, workspace path)
- **AND** clicks "Create"
- **THEN** system creates a new node in the local graph
- **AND** persists the node to local JSON storage

#### Scenario: Validate workspace path

- **WHEN** user enters a workspace path that does not exist
- **AND** attempts to create the agent
- **THEN** system displays validation error message

---

### Requirement: User can edit agent node

The system SHALL allow users to edit metadata of existing agent nodes.

#### Scenario: Edit agent metadata

- **WHEN** user clicks on an agent node
- **AND** clicks "Edit" button in the detail panel
- **THEN** system opens edit form with current values
- **AND** user can modify description, tags, model, and other metadata

#### Scenario: Cannot edit main agent

- **WHEN** user attempts to edit the main agent node
- **THEN** system disables edit functionality
- **AND** shows a tooltip explaining "Main agent is read-only"

---

### Requirement: User can delete agent node

The system SHALL allow users to delete non-main agent nodes.

#### Scenario: Delete agent with confirmation

- **WHEN** user selects a non-main agent node
- **AND** clicks "Delete" button
- **THEN** system shows confirmation dialog
- **AND** user confirms deletion
- **AND** system removes the node from graph and local storage

#### Scenario: Cannot delete main agent

- **WHEN** user attempts to delete the main agent
- **THEN** system prevents deletion
- **AND** shows error message "Main agent cannot be deleted"

---

### Requirement: User can create agent relationships

The system SHALL allow users to define directed edges between agents representing task assignment or reporting relationships.

#### Scenario: Create assigns relationship

- **WHEN** user drags from one agent node to another
- **AND** selects "Assigns" relationship type
- **THEN** system creates a directed edge from source to target
- **AND** edge is styled with arrow indicating direction

#### Scenario: Create reports relationship

- **WHEN** user drags from one agent node to another
- **AND** selects "Reports" relationship type
- **THEN** system creates a directed edge with distinct styling

#### Scenario: Prevent self-referencing relationship

- **WHEN** user attempts to create a relationship from an agent to itself
- **THEN** system prevents the action
- **AND** shows validation error

---

### Requirement: User can sync graph to OpenClaw

The system SHALL synchronize the agent graph metadata to OpenClaw via CLI commands.

#### Scenario: Sync new agent to OpenClaw

- **WHEN** user clicks "Sync to OpenClaw" button
- **AND** there are new agents in the graph not in OpenClaw
- **THEN** system executes `openclaw agents add <name> --workspace <path>`
- **AND** displays progress indicator
- **AND** shows success/failure message after completion

#### Scenario: Sync updates to existing agents

- **WHEN** user has modified agent metadata
- **AND** clicks "Sync to OpenClaw"
- **THEN** system updates OpenClaw configuration files
- **AND** preserves existing sessions and state

---

### Requirement: User can import from OpenClaw

The system SHALL scan OpenClaw configuration and import existing agents into the graph.

#### Scenario: Import existing agents

- **WHEN** user clicks "Import from OpenClaw"
- **THEN** system executes `openclaw agents list`
- **AND** parses output to extract agent metadata
- **AND** creates corresponding nodes in the graph

#### Scenario: Handle duplicate import

- **WHEN** imported agent already exists in graph
- **THEN** system updates existing node with latest data
- **AND** does not create duplicate

---

### Requirement: User can view agent details

The system SHALL display detailed information when user clicks on an agent node.

#### Scenario: Show agent detail panel

- **WHEN** user clicks on an agent node
- **THEN** system opens a side panel with:
  - Agent name and ID
  - Workspace path
  - Model configuration
  - Tags and description
  - List of relationships (incoming and outgoing)

#### Scenario: Main agent shows read-only badge

- **WHEN** user views details of main agent
- **THEN** system displays a "Main Agent" badge
- **AND** indicates it is managed by OpenClaw

---

## Data Model

### AgentNode

| Field       | Type     | Required | Description                |
| ----------- | -------- | -------- | -------------------------- |
| id          | string   | Yes      | OpenClaw agent ID          |
| name        | string   | Yes      | Display name               |
| description | string   | No       | Business description       |
| workspace   | string   | Yes      | OpenClaw workspace path    |
| model       | string   | No       | Model configuration        |
| tags        | string[] | No       | Business tags              |
| isMain      | boolean  | Yes      | Whether this is main agent |
| createdAt   | ISO8601  | Yes      | Creation timestamp         |
| updatedAt   | ISO8601  | Yes      | Last update timestamp      |

### AgentEdge

| Field  | Type   | Required | Description            |
| ------ | ------ | -------- | ---------------------- |
| id     | string | Yes      | Unique edge ID         |
| source | string | Yes      | Source node ID         |
| target | string | Yes      | Target node ID         |
| type   | enum   | Yes      | "assigns" or "reports" |
| label  | string | No       | Optional edge label    |

---

## ADDED Requirements - Task Queue

### Requirement: Task list supports backend pagination

The system SHALL return paginated results from the backend.

#### Scenario: Paginated task list request

- **WHEN** user requests task list with page=0, size=20
- **THEN** backend returns 20 tasks
- **AND** returns totalElements (total count)
- **AND** returns totalPages
- **AND** returns current page number

#### Scenario: Paginated response format

- **WHEN** backend returns paginated task list
- **THEN** response includes:
  ```json
  {
    "content": [...],
    "totalElements": 150,
    "totalPages": 8,
    "size": 20,
    "number": 0,
    "first": true,
    "last": false
  }
  ```

#### Scenario: Task list with filters and pagination

- **WHEN** user filters by status AND requests pagination
- **THEN** backend applies filter first, then paginates

---

### Requirement: Task can be created with priority

The system SHALL allow creating tasks with different priority levels.

#### Scenario: Create task with high priority

- **WHEN** user creates a task with priority "high"
- **THEN** task is placed before medium/low priority tasks in queue

#### Scenario: Create task with dependencies

- **WHEN** user creates a task with `dependsOn` field
- **THEN** task remains PENDING until all dependencies are COMPLETED

---

### Requirement: Task can be claimed without duplicate processing

The system SHALL prevent multiple workers from processing the same task.

#### Scenario: Atomic claim

- **WHEN** two workers attempt to claim the same PENDING task simultaneously
- **THEN** only one worker succeeds
- **AND** the other worker receives null/empty response

#### Scenario: Claim only pending tasks

- **WHEN** worker attempts to claim a task that is RUNNING
- **THEN** claim fails, task remains with current worker

---

### Requirement: Worker concurrency is configurable

The system SHALL limit the number of concurrent tasks per worker.

#### Scenario: Default concurrency

- **WHEN** worker starts without concurrency configuration
- **THEN** worker processes 1 task at a time (default)

#### Scenario: Custom concurrency

- **WHEN** worker is configured with concurrency=3
- **THEN** worker processes up to 3 tasks simultaneously

---

### Requirement: Task status is trackable

The system SHALL provide detailed status information for each task.

#### Scenario: Status transitions

- **WHEN** task is created
- **THEN** status is PENDING
- **WHEN** task is claimed
- **THEN** status transitions to RUNNING
- **WHEN** task completes successfully
- **THEN** status transitions to COMPLETED
- **WHEN** task fails and can retry
- **THEN** status remains PENDING, retryCount increments
- **WHEN** task fails and exceeds max retries
- **THEN** status transitions to FAILED or DEAD

#### Scenario: Status query

- **WHEN** user queries task status
- **THEN** system returns: id, status, retryCount, createdAt, startedAt, completedAt, error, result

---

### Requirement: Task result is stored

The system SHALL store the execution result of completed tasks.

#### Scenario: Store successful result

- **WHEN** worker completes task with result data
- **THEN** result is stored in JSON format
- **AND** can be retrieved via API

#### Scenario: Store error information

- **WHEN** worker fails task with error message
- **THEN** error message is stored
- **AND** can be retrieved via API

---

### Requirement: Task can be scheduled for future execution

The system SHALL support delayed task execution.

#### Scenario: Schedule task

- **WHEN** user creates task with `scheduledAt` in the future
- **THEN** task remains PENDING until scheduled time
- **AND** is not claimable before scheduled time

---

## Task Queue Data Model

### Task Entity

| Field        | Type     | Required | Description                               |
| ------------ | -------- | -------- | ----------------------------------------- |
| id           | string   | Yes      | Unique task ID                            |
| type         | string   | Yes      | Task type identifier                      |
| payload      | JSON     | Yes      | Task data                                 |
| priority     | integer  | Yes      | Priority: HIGH=10, MEDIUM=5, LOW=0        |
| status       | enum     | Yes      | PENDING, RUNNING, COMPLETED, FAILED, DEAD |
| retry_count  | integer  | Yes      | Current retry count                       |
| max_retries  | integer  | Yes      | Maximum retry attempts                    |
| claimed_by   | string   | No       | Worker ID that claimed the task           |
| created_at   | datetime | Yes      | Creation timestamp                        |
| started_at   | datetime | No       | When task started executing               |
| completed_at | datetime | No       | When task completed                       |
| scheduled_at | datetime | No       | Scheduled execution time                  |
| result       | JSON     | No       | Execution result                          |
| error        | string   | No       | Error message if failed                   |
| depends_on   | JSON     | No       | Array of dependency task IDs              |

### Pagination Parameters

| Parameter | Type    | Required | Description                        |
| --------- | ------- | -------- | ---------------------------------- |
| page      | integer | No       | Page number (0-based), default: 0  |
| size      | integer | No       | Page size, default: 20             |
| sort      | string  | No       | Sort field, e.g., "createdAt,desc" |
| status    | string  | No       | Filter by status                   |
| type      | string  | No       | Filter by task type                |

---

## Acceptance Criteria

1. Graph renders within 2 seconds for up to 50 agents
2. All CRUD operations persist correctly to local JSON
3. Sync operation completes without data loss
4. Main agent is never deletable or editable
5. Relationship edges display correct directionality
6. Responsive layout works on screens 1024px and wider
7. Concurrent task claiming does not result in duplicate processing
8. Task status transitions are correct and atomic
9. Task results and errors are properly stored and retrievable
10. Scheduled tasks execute at the specified time
11. **Task list uses backend pagination** (page, size, totalElements, totalPages)
12. **List API supports filtering + pagination together**
13. Frontend displays pagination controls correctly

---

_Spec version: 1.1_
_Last updated: 2026-03-19_
