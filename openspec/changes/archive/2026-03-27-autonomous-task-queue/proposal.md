## Why

ClawDash needs autonomous task execution capability. Currently, users must manually assign tasks to agents, and there's no support for complex tasks that need to be broken into subtasks, executed by different agents, and aggregated into meaningful reports. The goal is a self-evolving system where agents can accept tasks, decompose them, execute in parallel, and report results automatically.

## What Changes

- **New task command syntax**: `#task <description>` as unified task entry point
- **Task decomposition**: MenxiaSheng (门下省) analyzes and decomposes complex tasks into atomic subtasks
- **Context-aware reporting**: Subtask results are aggregated before reporting to users (no information pollution)
- **Event-driven notification**: Subtask completion triggers notification via A2A messages (no polling)
- **Exception handling**: Exponential backoff retry with human intervention for persistent failures
- **Task hierarchy**: Task groups with parent-child relationships for decomposition tracking

## Capabilities

### New Capabilities
- `task-command-parser`: Parse `#task` commands, detect priority, determine if decomposition is needed
- `task-decomposer`: MenxiaSheng logic for analyzing complexity and breaking tasks into subtasks
- `task-aggregation`: MenxiaSheng collects subtask results and generates meaningful completion reports
- `task-assignment`: MenxiaSheng assigns tasks to best-fit agents based on capabilities and workload
- `task-exception-handling`: Retry with exponential backoff, UI highlighting for human intervention
- `task-group-view`: Extended TaskQueue UI showing task groups with progress and intervention status

### Modified Capabilities
- (none - this is a new capability set)

## Impact

- **Backend**: New `task_groups` table, extended `tasks` table with group/parent references
- **Database**: New indexes for task_group_id, parent_task_id, needs_intervention
- **Frontend**: Extended TaskQueue.vue with group view and intervention highlighting
- **Agent**: MenxiaSheng prompt updates for decomposition and aggregation logic
- **API**: New endpoints for task groups, decomposition, reassignment
