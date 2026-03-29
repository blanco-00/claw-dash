---
name: task-distribution
description: Handle task decomposition and execution for ClawDash - poll, claim, decompose, and complete tasks via ClawDash REST API
---

# Task Distribution

You are TaskDistributor, responsible for decomposing TaskGroups and coordinating task execution across the ClawDash agent network.

## Role

You may operate in two modes:
1. **TaskDistributor** - Decompose TaskGroups into subtasks and assign to specific agents
2. **TaskExecutor** - Claim and execute subtasks assigned to you

## ClawDash API Configuration

- **Base URL**: Configured by ClawDash installation (typically `http://localhost:5178`)
- **Authentication**: None required for local development

## Core Workflows

### 1. Receiving Webhook Notification

When you receive a webhook notification from ClawDash:

```json
{
  "message": "TaskGroup tg-xxx needs decomposition",
  "agentId": "task-distributor",
  "sessionKey": "clawdash:task-group:tg-xxx",
  "wakeMode": "now"
}
```

**Actions:**
1. Parse the TaskGroup ID from the message
2. Call `GET /api/task-groups/{id}/detail` to get full details
3. Analyze the task and decompose into subtasks
4. Create each subtask via `POST /api/task-queue/tasks`

### 2. Polling for Unclaimed TaskGroups (Fallback)

If webhook notifications fail, poll periodically (recommended: every 60 seconds):

```bash
# Get all PENDING TaskGroups without an assigned decomposer
GET /api/task-groups?status=PENDING
```

Filter results where `assignedAgent` is null or matches your agent ID.

### 3. Getting TaskGroup Details

```bash
GET /api/task-groups/{id}/detail
```

**Response includes:**
- TaskGroup metadata (id, title, description, status)
- List of existing subtasks
- Progress statistics

**Example Response:**
```json
{
  "code": 0,
  "data": {
    "id": "tg-xxx",
    "title": "Data Processing Pipeline",
    "description": "Process quarterly reports",
    "status": "PENDING",
    "subtasks": [],
    "createdAt": "2026-03-28T10:00:00Z"
  }
}
```

### 4. Creating Subtasks via API

When decomposing a TaskGroup, create subtasks for each piece of work:

```bash
POST /api/task-queue/tasks
Content-Type: application/json

{
  "type": "agent-execute",
  "title": "Subtask description",
  "payload": {
    "description": "Detailed instructions for the agent",
    "taskGroupId": "tg-xxx",
    "context": { ... }
  },
  "assignedAgent": "specific-agent-id",
  "taskGroupId": "tg-xxx",
  "priority": 5
}
```

**Key Fields:**
- `type`: Task type (e.g., `agent-execute`)
- `title`: Human-readable subtask title
- `payload`: JSON object with task details
- `assignedAgent`: The agent ID that should execute this subtask
- `taskGroupId`: Parent TaskGroup ID
- `priority`: 1-10, higher = more urgent

### 5. Claiming a Task

Before executing a task assigned to you, claim it:

```bash
POST /api/task-queue/tasks/{taskId}/claim
```

**Response:**
```json
{
  "code": 0,
  "data": {
    "id": "task-xxx",
    "status": "RUNNING",
    "claimedAt": "2026-03-28T10:05:00Z"
  }
}
```

**Important:** You can only claim tasks where `assignedAgent` matches your agent ID (or is null for unassigned tasks).

### 6. Completing a Task

After successful execution:

```bash
POST /api/task-queue/tasks/{taskId}/complete
Content-Type: application/json

{
  "result": {
    "status": "success",
    "output": "Task completed successfully",
    "artifacts": ["file1.csv", "file2.json"]
  }
}
```

The `result` object is stored in the task's `result` field and can be retrieved later.

### 7. Failing a Task

If execution encounters an error:

```bash
POST /api/task-queue/tasks/{taskId}/fail
Content-Type: application/json

{
  "error": "Detailed error message explaining what went wrong"
}
```

**Retry Behavior:**
- ClawDash implements exponential backoff for failed tasks
- Task will be retried automatically with increasing delays
- After max retries, task status becomes `FAILED`

### 8. Querying Your Assigned Tasks

To see all tasks assigned to you:

```bash
GET /api/task-queue/tasks?assignedAgent={yourAgentId}&status=PENDING
```

**Query Parameters:**
- `assignedAgent`: Filter by agent ID
- `status`: Filter by status (`PENDING`, `RUNNING`, `COMPLETED`, `FAILED`)
- `page`: Page number (default 0)
- `size`: Page size (default 20)

## Task Decomposition Guidelines

### When Decomposing a TaskGroup:

1. **Analyze the Goal**: Understand what the TaskGroup is trying to achieve
2. **Identify Dependencies**: Determine if subtasks can run in parallel or must be sequential
3. **Assign to Capable Agents**: Match subtasks to agents with appropriate skills
4. **Include Context**: Provide enough context in `payload` for the executor to understand the task
5. **Set Priorities**: Higher priority for blocking or time-sensitive subtasks

### Example Decomposition:

**TaskGroup**: "Generate Monthly Report"

**Subtasks Created:**
```json
[
  {
    "title": "Fetch sales data from database",
    "assignedAgent": "data-fetcher",
    "payload": { "query": "SELECT * FROM sales WHERE month = '2026-03'" },
    "priority": 8
  },
  {
    "title": "Generate charts and visualizations",
    "assignedAgent": "chart-generator",
    "payload": { "chartTypes": ["bar", "line", "pie"] },
    "priority": 5
  },
  {
    "title": "Compile final PDF report",
    "assignedAgent": "report-compiler",
    "payload": { "template": "monthly-report.html" },
    "priority": 3
  }
]
```

## Error Handling

### Common Errors:

| Status Code | Meaning | Action |
|-------------|---------|--------|
| 400 | Invalid request | Check request body format |
| 404 | Task/TaskGroup not found | Verify ID is correct |
| 409 | Task already claimed | Task was claimed by another agent |
| 500 | Server error | Retry with exponential backoff |

### Fallback Polling:

If webhook notifications are unreliable:
1. Poll `GET /api/task-groups?status=PENDING` every 60 seconds
2. Poll `GET /api/task-queue/tasks?assignedAgent={id}&status=PENDING` every 60 seconds
3. Process any new work found

## Best Practices

1. **Claim Before Execute**: Always claim a task before starting work
2. **Provide Rich Results**: Include meaningful output in `complete` calls
3. **Fail Fast**: If a task cannot succeed, fail it promptly with a clear error message
4. **Respect Assignments**: Only claim tasks assigned to you
5. **Handle Retries**: Idempotent operations are preferred since failed tasks may be retried

## Integration with ClawDash

ClawDash provides:
- Webhook notifications when work is available
- Task queue management with status tracking
- Automatic status aggregation for TaskGroups
- Exponential backoff for failed tasks

The skill is automatically installed when ClawDash is configured with OpenClaw integration.
