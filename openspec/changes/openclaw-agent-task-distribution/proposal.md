## Why

Currently, ClawDash uses a Java-based TaskWorker that polls and claims all PENDING tasks automatically. OpenClaw agents have no way to participate in task execution - they only interact with ClawDash through a skill that provides API access. The "worker-xxx" labels shown in task monitoring are from Java threads, not OpenClaw agents.

We need to enable **OpenClaw agents to poll, claim, and execute tasks** from ClawDash, with proper task decomposition and assignment. This enables:
- Natural language task understanding by LLM agents
- Dynamic task decomposition by a TaskDistributorAgent
- Intelligent task assignment to specific agents
- Human-readable progress tracking in ClawDash UI

## What Changes

1. **OpenClaw Task Distribution Skill**: A new skill installed into OpenClaw that teaches agents how to poll ClawDash for tasks, claim them, decompose TaskGroups, and complete subtasks via API calls.

2. **Webhook Notification System**: When a TaskGroup is created (or a subtask is added), ClawDash proactively notifies the appropriate OpenClaw agent via webhook instead of relying on polling.

3. **TaskDistributorAgent Pattern**: A designated OpenClaw agent receives webhook notifications, analyzes TaskGroups, decomposes them into subtasks with assignedAgent fields, and writes them back via ClawDash API.

4. **Agent-Specific Task Assignment**: Subtasks can be assigned to specific OpenClaw agents via the `assignedAgent` field. Only that agent will be notified to claim them.

5. **TaskGroup Completion Notification**: When all subtasks in a TaskGroup complete, ClawDash notifies the main agent via webhook.

6. **Frontend UI for Agent-Task Binding**: A ClawDash page where users can configure which OpenClaw agent handles which task types, and view agent task status.

## Capabilities

### New Capabilities

- `task-distribution-skill`: OpenClaw skill that teaches agents to poll, claim, decompose, and complete tasks via ClawDash REST API
- `webhook-agent-notification`: System for ClawDash to send webhook notifications to OpenClaw agents when tasks need attention
- `task-group-decomposition-trigger`: Automatic trigger when TaskGroup is created, notifying TaskDistributorAgent to decompose
- `subtask-assignment`: Ability to create subtasks with specific `assignedAgent` values
- `agent-progress-tracking`: ClawDash tracks and displays progress of tasks assigned to each OpenClaw agent
- `main-agent-completion-notification`: Webhook notification to main agent when TaskGroup completes

### Modified Capabilities

- `task-queue-core`: TaskQueueTask already has `assignedAgent` and `reportToAgent` fields - we enable this flow instead of Java Worker claiming all tasks

## Impact

- **New API**: `POST /api/task-queue/notify-agent` - webhook trigger endpoint
- **New API**: `GET /api/task-queue/tasks?assignedAgent={agentId}&status=PENDING` - agent-specific task query
- **New Frontend Page**: Agent Task Configuration - bind task types to agents, view agent workload
- **New OpenClaw Skill**: `task-distribution` skill in `~/.openclaw/workspace/skills/`
- **TaskWorker Behavior**: Should NOT claim tasks that have `assignedAgent` set (or can be disabled entirely for agent-managed tasks)
- **Database**: No schema changes needed - `assignedAgent` and `reportToAgent` fields already exist
