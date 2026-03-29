# OpenClaw Agent Task Distribution — Technical Design

## Context

### Background

ClawDash currently has:
- **TaskQueueTask** entity with `assignedAgent` and `reportToAgent` fields (unused by Java TaskWorker)
- **TaskWorker** Java thread that polls ALL PENDING tasks regardless of `assignedAgent`
- **OpenClawService** that installs a ClawDash skill into OpenClaw workspace

The problem: Java TaskWorker claims everything, OpenClaw agents never get a chance.

### Current Flow (Broken)

```
User creates task → PENDING → Java TaskWorker claims (worker-xxx)
                                        ↑
                        OpenClaw agent can't compete
```

### Target Flow

```
User creates TaskGroup
        ↓
ClawDash webhook → Wake TaskDistributorAgent
        ↓
Agent decomposes → Creates subtasks with assignedAgent
        ↓
ClawDash webhook → Wake specific agent per subtask
        ↓
Agent claims + executes → Complete/fail (exponential backoff)
        ↓
TaskGroup complete → ClawDash webhook → Main agent notification
```

### Constraints

- **OpenClaw cron is unstable** — do not rely on it for polling
- **OpenClaw agents are reactive** — they don't poll by default, they respond to events
- **ClawDash has no LLM** — cannot understand task semantics
- **Boundary**: Rule-driven → ClawDash; Semantic understanding → OpenClaw agents

---

## Goals / Non-Goals

**Goals:**
1. OpenClaw agents can poll, claim, and execute tasks from ClawDash
2. TaskDistributorAgent decomposes TaskGroups into subtasks with assignedAgent
3. ClawDash notifies agents via webhook when there's work
4. Progress tracking in ClawDash UI
5. Exponential backoff for failed tasks (already implemented in TaskQueueService)

**Non-Goals:**
1. OpenClaw cron for scheduling (unstable)
2. Agent-to-agent A2A communication (we use webhooks via ClawDash)
3. Java TaskWorker removal (can coexist, ignores assignedAgent tasks)

---

## Decisions

### Decision 1: Trigger via TaskGroup Creation, Not Cron

**Choice**: When a TaskGroup is created (or modified with new subtasks), ClawDash immediately sends a webhook to notify the relevant agent.

**Rationale**: 
- Precise trigger moment, no polling waste
- TaskGroup creation is a clear semantic boundary
- Webhook is event-driven, matches OpenClaw's reactive model

**Alternative considered**: ClawDash cron polling for new PENDING tasks with null assignedAgent.
- Rejected: Still polling, just moved to ClawDash. We prefer event-driven.

### Decision 2: OpenClaw Skill for Task Distribution

**Choice**: Create a `task-distribution` skill that teaches TaskDistributorAgent how to:
1. Receive webhook notifications
2. Query ClawDash API for TaskGroup details
3. Decompose tasks into subtasks
4. Call ClawDash API to create subtasks with assignedAgent

**Skill Location**: `~/.openclaw/workspace/skills/task-distribution/SKILL.md`

**Rationale**:
- Skills are the standard way to extend OpenClaw agents
- Skill can be updated without code deployment
- Follows OpenClaw's skill loading precedence

### Decision 3: API Endpoints for Agent Interaction

**New Endpoints**:

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/task-queue/notify-agent` | Webhook trigger for agent notification |
| `GET` | `/api/task-queue/tasks?assignedAgent={id}&status=PENDING` | Query tasks for specific agent |
| `GET` | `/api/task-groups/{id}/detail` | Get TaskGroup with all subtasks |

**Notify Agent Request**:
```json
{
  "agentId": "task-distributor",
  "taskGroupId": "tg-xxx",
  "action": "decompose" | "execute",
  "webhookUrl": "http://host.docker.internal:18789/hooks/agent"
}
```

### Decision 4: Webhook via OpenClaw's `/hooks/agent` Endpoint

**Choice**: Use OpenClaw's native webhook endpoint for agent notification.

**Endpoint**: `POST /hooks/agent`

**Request Format**:
```json
{
  "message": "TaskGroup tg-xxx needs decomposition. Call GET /api/task-groups/xxx/detail to see details.",
  "agentId": "task-distributor",
  "sessionKey": "clawdash:task-group:tg-xxx",
  "wakeMode": "now"
}
```

**Rationale**:
- Native OpenClaw feature, no custom endpoint needed
- Supports session binding so agent can resume context
- `wakeMode: now` ensures immediate execution

### Decision 5: Frontend UI — Agent Task Configuration Page

**Choice**: Add a new page `AgentsConfig.vue` or extend existing for:
- Binding task types to specific agents
- Viewing agent workload (pending/running/completed tasks)
- Configuring which agent is the TaskDistributor

**Location**: Under Agents menu, alongside AgentsList

**Design**: Follow existing ClawDash patterns:
- ElTable for task list per agent
- ElDrawer for detail view
- ElCard for stat summaries

---

## Data Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                           User Action                                │
│                   Create TaskGroup via UI/API                       │
└─────────────────────────────────┬───────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        ClawDash Backend                             │
│                                                                      │
│  1. TaskGroup created (assignedAgent = null)                        │
│  2. Check: is this for TaskDistributor?                             │
│  3. Yes → POST to OpenClaw /hooks/agent                             │
│     { message, agentId: "task-distributor", sessionKey }            │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     OpenClaw Agent (TaskDistributor)                 │
│                                                                      │
│  4. Receives webhook → LLM understands task                         │
│  5. Decomposes TaskGroup into subtasks                              │
│  6. For each subtask:                                               │
│     POST /api/task-queue/tasks                                      │
│     { title, payload, assignedAgent: "specific-agent" }            │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        ClawDash Backend                             │
│                                                                      │
│  7. Subtasks created with assignedAgent                              │
│  8. For each subtask → POST /hooks/agent                            │
│     { message, agentId: "specific-agent" }                          │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                  OpenClaw Agent (Specific Agent)                     │
│                                                                      │
│  9. Receives webhook → claims task via API                          │
│     POST /api/task-queue/tasks/{id}/claim                           │
│  10. Executes → completes/fails                                     │
│      POST /api/task-queue/tasks/{id}/complete                      │
└─────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        ClawDash Backend                             │
│                                                                      │
│  11. TaskGroup status updated via syncTaskGroupStatus()             │
│  12. If all complete → POST /hooks/agent to main agent              │
└─────────────────────────────────────────────────────────────────────┘
```

---

## API Design

### Notify Agent (Webhook Trigger)

```java
// TaskQueueController.java
@PostMapping("/notify-agent")
public Result<Void> notifyAgent(@RequestBody NotifyAgentRequest request) {
    // Call OpenClaw webhook
    String openclawUrl = openClawConfig.getApiUrl(); // e.g., http://localhost:18789
    String webhookUrl = openclawUrl + "/hooks/agent";
    
    HttpEntity<NotifyAgentRequest> entity = new HttpEntity<>(request, headers);
    restTemplate.postForObject(webhookUrl, entity, String.class);
    
    return Result.success();
}
```

### Query Tasks by Agent

```java
// Extend TaskQueueController
@GetMapping("/tasks")
public Result<TaskPageResponse> listTasks(
    @RequestParam(required = false) String assignedAgent,
    @RequestParam(required = false) String status,
    // ... existing params
) {
    // Filter by assignedAgent if provided
}
```

---

## Skill Design

### SKILL.md Structure

```
~/.openclaw/workspace/skills/task-distribution/
├── SKILL.md
└── (optional scripts)
```

**SKILL.md Content Outline**:
```markdown
---
name: task-distribution
description: Handle task decomposition and execution for ClawDash
---

# Task Distribution

You are TaskDistributor, responsible for decomposing TaskGroups...

## When you receive a webhook notification...

## How to decompose a task...

## How to create subtasks via API...

## How to report completion...
```

### Skill Teaches Agent:

| Action | API Call |
|--------|----------|
| Get TaskGroup details | `GET /api/task-groups/{id}/detail` |
| Create subtask | `POST /api/task-queue/tasks` |
| Complete subtask | `POST /api/task-queue/tasks/{id}/complete` |
| Fail subtask | `POST /api/task-queue/tasks/{id}/fail` |

---

## Frontend Design

### Agent Task Configuration Page

```
┌─────────────────────────────────────────────────────────────────────┐
│  Agent Task Configuration                                            │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  TaskDistributor Agent: [Dropdown: Select Agent          ▼]          │
│                                                                      │
│  ──────────────────────────────────────────────────────────────     │
│                                                                      │
│  Agent Bindings:                                                    │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Agent        │ Task Types      │ Pending │ Running │ Done  │   │
│  ├──────────────┼─────────────────┼─────────┼─────────┼───────┤   │
│  │ gongbu       │ bug-fix, feature│    3    │    1    │  12    │   │
│  │ hubu         │ data-sync       │    1    │    0    │   5    │   │
│  │ xingbu       │ analysis         │    2    │    1    │   8    │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  [+ Add Binding]                                                     │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

**Styling**: Follow existing ClawDash design system — Element Plus components, same color palette, consistent header pattern.

---

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| OpenClaw webhook endpoint down | Agent never notified | Agent falls back to periodic polling (skill-instructed) |
| Agent decomposition quality | Bad subtasks = failed execution | Start conservative, allow manual reassignment |
| Concurrent task overload | Agent overwhelmed | Set per-agent concurrency limit in skill |
| Webhook notification failure | Silent task stuck | ClawDash logs failures, UI shows "notification failed" |

---

## Open Questions

1. **Polling fallback interval**: If webhook fails, how often should agent poll as fallback? (建议: 60s)

2. **Decomposition depth**: Support subtasks of subtasks, or flat only? (建议: flat first, nested later)

3. **Main agent notification**: Should main agent be an OpenClaw agent or just the user? (建议: configurable)

---

## Related Archives

This design builds upon and complements:
- `2026-03-27-autonomous-task-queue` — Internal Java-based task queue (different approach)
- `2026-03-19-openclaw-agent-graph-management` — Agent topology (agents we're assigning to)
