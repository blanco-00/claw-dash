## ADDED Requirements

### Requirement: MCP Server Endpoint

ClawDash Backend SHALL expose an MCP Server endpoint at `/mcp` that conforms to the Model Context Protocol specification.

#### Scenario: MCP endpoint responds to health check
- **WHEN** a client sends a GET request to `/mcp`
- **THEN** the server returns a valid MCP protocol response

#### Scenario: MCP endpoint accepts tool calls
- **WHEN** a client sends a valid MCP tool call request
- **THEN** the server processes the request and returns the tool result

### Requirement: Task Queue Tools

The MCP Server SHALL expose the following Task Queue tools for OpenClaw agents to use:

#### Scenario: Create task via MCP
- **WHEN** an agent calls `task_create` tool with valid parameters (type, payload, priority)
- **THEN** a new task is created in the ClawDash task queue and task ID is returned

#### Scenario: List tasks via MCP
- **WHEN** an agent calls `task_list` tool with optional filters (status, page, size)
- **THEN** a paginated list of tasks is returned

#### Scenario: Get task status via MCP
- **WHEN** an agent calls `task_status` tool with a task ID
- **THEN** the current status of the task is returned

#### Scenario: Complete task via MCP
- **WHEN** an agent calls `task_complete` tool with task ID and result
- **THEN** the task is marked as COMPLETED with the provided result

#### Scenario: Fail task via MCP
- **WHEN** an agent calls `task_fail` tool with task ID and error message
- **THEN** the task is marked as FAILED with the error message

#### Scenario: Get task statistics via MCP
- **WHEN** an agent calls `task_stats` tool
- **THEN** the statistics (counts by status) are returned

### Requirement: Frontend MCP Configuration

The ClawDash Frontend SHALL provide a one-click MCP configuration feature in the OpenClaw management page.

#### Scenario: User clicks "Configure MCP"
- **WHEN** user clicks the "Configure MCP" button
- **THEN** the system generates the OpenClaw configuration JSON and displays it to the user

#### Scenario: User copies MCP configuration
- **WHEN** user clicks the copy button
- **THEN** the configuration JSON is copied to the clipboard with a success notification
