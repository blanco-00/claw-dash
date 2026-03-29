# Autonomous Task Queue — Technical Design

## Context

### Background

ClawDash currently has a basic task queue (`TaskQueue`) with:
- Priority-based pending queue
- Redis distributed locking for safe claiming
- Retry logic with configurable max attempts
- Status tracking: PENDING → RUNNING → COMPLETED/FAILED

Missing capabilities for autonomous operation:
1. **Task decomposition** — Complex tasks need to be broken into atomic subtasks
2. **Unified task entry** — Multiple trigger sources (user command, agent discovery, scheduled) should funnel into one system
3. **Context-aware reporting** — Subtask results need aggregation before reporting to users
4. **Decomposer agent** — A designated agent (MenxiaSheng/门下省) handles analysis, decomposition, and aggregation
5. **Event-driven notification** — Subtask completion should trigger notification, not polling

### Goals

Enable ClawDash agents to:
1. Accept tasks via `#task` command in chat
2. Have MenxiaSheng analyze and decompose tasks when needed
3. Assign subtasks to appropriate agents with dependencies
4. Aggregate results and report meaningful completion messages to users
5. Handle failures with exponential backoff and human intervention when needed

---

## Decisions

### Decision 1: Command Syntax — `#task`

**Choice**: Use `#task` prefix to trigger task creation, avoiding conflict with OpenClaw's slash commands.

**Format**:
```
#task 用户登录一直报错
#task 修一下支付接口，太慢了
#task 做一个用户登录注册流程
```

**LLM Parsing Output**:
```typescript
interface ParsedTask {
  rawInput: string;           // Original user input
  title: string;             // Extracted task title
  description: string;        // Extended description
  priority: 'low' | 'medium' | 'high' | 'urgent';  // Auto-detected
  explicitDecomposeRequested: boolean;  // User said "拆解"
}
```

**Priority Detection**:
- `urgent` / `挂了` / `崩了` → urgent
- `一直` / `总是` / `紧急` → high
- Default → medium

### Decision 2: Task Data Model

**Choice**: Extend existing `tasks` table with group/parent references. Use `task_groups` table for grouping.

**ER Diagram**:
```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   task_groups                                              │
│   ├── id (UUID)                                           │
│   ├── title                                               │
│   ├── description                                         │
│   ├── total_goal                                          │
│   ├── overall_design                                      │
│   ├── decomposer_agent_id  ──────────┐                     │
│   ├── status                        │                     │
│   ├── created_at                    │                     │
│   └── completed_at                  │                     │
│                                      │                     │
│                                      │ 1:N                │
│                                      ▼                     │
│   tasks ◄────────────────────────────────────             │
│   ├── id / taskId (UUID)                                │
│   ├── task_group_id ──────────────────────────────────────┤
│   ├── parent_task_id (nullable, for multi-level)          │
│   ├── title                                               │
│   ├── description                                         │
│   ├── assigned_agent                                      │
│   ├── priority                                            │
│   ├── status                                              │
│   ├── retry_count                                        │
│   ├── max_retries                                        │
│   ├── last_error                                          │
│   ├── depends_on (JSON array)                            │
│   ├── result (JSON)                                       │
│   ├── report_to_agent                                     │
│   ├── context (JSON)                                      │
│   │       {                                               │
│   │         totalGoal: string,     // From task_groups   │
│   │         overallDesign: string, // From decomposition │
│   │         subtaskDescription: string  // This subtask  │
│   │       }                                               │
│   ├── created_at                                          │
│   ├── updated_at                                          │
│   └── completed_at                                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

**Simple vs Complex Task**:
- Simple task: `decomposer_agent_id = NULL`, single task in group, direct assignment
- Complex task: `decomposer_agent_id = "menxiasheng"`, multiple subtasks after decomposition

### Decision 3: Unified Entry — MenxiaSheng (门下省) as Single Entry Point

**Choice**: ALL tasks flow through MenxiaSheng, regardless of complexity.

MenxiaSheng responsibilities:
1. **Analyze** incoming task (parse, detect complexity)
2. **Route**:
   - Simple → Direct assignment + monitor + report
   - Complex → Decompose into subtasks + assign + monitor + aggregate + report
3. **Monitor** progress and handle exceptions
4. **Report** meaningful completion/failure messages

**Flow Diagram**:
```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│   #task 用户登录报错                                         │
│        │                                                    │
│        ▼                                                    │
│   ┌─────────────────────────────────────────────────────┐   │
│   │              MenxiaSheng (Decomposer)               │   │
│   │                                                     │   │
│   │   1. Parse task via LLM                            │   │
│   │   2. Analyze complexity                            │   │
│   │                                                     │   │
│   │   ┌─────────────────────────────────────────────┐   │   │
│   │   │  Simple Task?                               │   │   │
│   │   │                                             │   │   │
│   │   │  YES ──► Assign to best-fit agent           │   │   │
│   │   │         Monitor completion                  │   │   │
│   │   │         Report to user                      │   │   │
│   │   │                                             │   │   │
│   │   │  NO ───► Decompose into subtasks            │   │   │
│   │   │         Assign each subtask                  │   │   │
│   │   │         Collect results via A2A             │   │   │
│   │   │         Aggregate & Report                  │   │   │
│   │   │                                             │   │   │
│   │   └─────────────────────────────────────────────┘   │   │
│   └─────────────────────────────────────────────────────┘   │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Decision 4: Task Decomposition

**Decomposition Trigger**:
- LLM semantic analysis determines complexity
- Explicit user request (`#task 拆解一下...`)
- Task contains multiple distinct goals
- Task requires different agent capabilities

**Decomposition Output**:
```typescript
interface DecomposedTask {
  totalGoal: string;          // "修复用户登录问题"
  overallDesign: string;      // "分三步：1.收集日志 2.分析原因 3.修复bug"
  subtasks: Subtask[];
}

interface Subtask {
  title: string;
  description: string;
  assignedAgent: string;      // Selected based on agent capabilities
  dependsOn: string[];        // Execution order
  context: {
    totalGoal: string;
    overallDesign: string;
    subtaskDescription: string;  // "请收集登录相关日志"
  };
}
```

**Agent Selection Logic**:
MenxiaSheng knows all agent capabilities from the agent graph. Selection criteria:
- Task domain matches agent specialty (gongbu=engineering, hubu=finance, etc.)
- Current workload
- Historical success rate with similar tasks

### Decision 5: Event-Driven Notification via A2A Messages

**Choice**: Use existing A2A Message infrastructure + sessions_send API for notifications.

**Flow**:
```
Subtask completes
    │
    ├── Agent calls task_complete → DB updated
    │
    └── Agent calls sessions_send to MenxiaSheng
        {
          to: "menxiasheng",
          type: "SUBTASK_COMPLETED",
          content: {
            taskId: "xxx",
            taskGroupId: "yyy",
            summary: "bug已修复"
          }
        }
```

**MenxiaSheng Response to Notification**:
1. Query `tasks` table for complete subtask details (result, timing, etc.)
2. Query all subtasks in `task_group_id` for aggregation
3. Make decision:
   - All complete → Generate full report → Notify user
   - Partial with failures → Report partial success
   - All failed → Report failure with suggestions

### Decision 6: Exception Handling — Exponential Backoff

**Choice**: Implement exponential backoff with human intervention threshold.

**Retry Logic**:
```
retry_count < max_retries
    │
    ├── delay = base_delay * 2^retry_count
    │       (e.g., 5s, 10s, 20s, 40s...)
    │
    └── Increment retry_count, reschedule

retry_count >= max_retries
    │
    ├── Mark task as "needs_human_intervention"
    │
    └── UI highlights in Task Management view
```

**UI for Human Intervention**:
```
┌─────────────────────────────────────────────────────────────┐
│  🔴 任务组: 用户登录报错                                    │
│     ├── [gongbu] bug修复 - 超时4次 - 需要人工介入           │
│     │                   └── [重新指派] [查看详情] [放弃]     │
│     ├── [hubu] 日志收集 - completed ✓                        │
│     └── [xingbu] 原因分析 - completed ✓                      │
└─────────────────────────────────────────────────────────────┘
```

### Decision 7: Reporting Aggregation

**Aggregation Rules**:

MenxiaSheng aggregates results when:
1. All subtasks completed successfully → Full success report
2. Some subtasks failed but recoverable → Partial success + failure analysis
3. All subtasks failed → Failure report with root cause analysis

**Report Content**:
```typescript
interface AggregatedReport {
  taskGroupId: string;
  status: 'success' | 'partial' | 'failed';
  
  summary: string;           // "登录问题已修复"
  
  details: {
    completed: SubtaskResult[];
    failed: SubtaskResult[];
  };
  
  recommendations?: string;  // For failures
}
```

**Example Report**:
```
✅ 任务完成：用户登录问题已修复

执行步骤：
1. ✅ 日志收集 (hubu) - 收集到登录日志
2. ✅ 原因分析 (xingbu) - 发现session超时问题
3. ✅ Bug修复 (gongbu) - 已修复并验证

用户可以正常登录了。
```

---

## Database Schema

### Extended `task_groups` Table

```sql
CREATE TABLE task_groups (
  id VARCHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  total_goal TEXT,
  overall_design TEXT,
  decomposer_agent_id VARCHAR(100),
  status ENUM('pending', 'in_progress', 'completed', 'failed') DEFAULT 'pending',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP,
  
  INDEX idx_decomposer (decomposer_agent_id),
  INDEX idx_status (status)
);
```

### Extended `tasks` Table

```sql
ALTER TABLE tasks ADD COLUMN (
  task_group_id VARCHAR(36),
  parent_task_id VARCHAR(36),
  assigned_agent VARCHAR(100),
  retry_count INT DEFAULT 0,
  max_retries INT DEFAULT 3,
  report_to_agent VARCHAR(100),
  context JSON,
  task_group_id VARCHAR(36),
  
  FOREIGN KEY (task_group_id) REFERENCES task_groups(id),
  FOREIGN KEY (parent_task_id) REFERENCES tasks(id),
  
  INDEX idx_task_group (task_group_id),
  INDEX idx_parent (parent_task_id),
  INDEX idx_assigned (assigned_agent),
  INDEX idx_needs_intervention (retry_count, max_retries)
);
```

### New `task_group_tasks` View (for querying)

```sql
CREATE VIEW v_task_group_details AS
SELECT 
  tg.id AS task_group_id,
  tg.title,
  tg.status AS group_status,
  t.id AS task_id,
  t.title AS task_title,
  t.assigned_agent,
  t.status AS task_status,
  t.result,
  t.error
FROM task_groups tg
LEFT JOIN tasks t ON t.task_group_id = tg.id;
```

---

## API Design

### Task Group Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/task-groups` | Create task group (via MenxiaSheng) |
| `GET` | `/api/task-groups/{id}` | Get group with all subtasks |
| `GET` | `/api/task-groups/{id}/progress` | Get completion progress |
| `PATCH` | `/api/task-groups/{id}/status` | Update group status |

### Task Management (extends existing)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create task (simple or via MenxiaSheng) |
| `POST` | `/api/tasks/{id}/claim` | Agent claims task |
| `POST` | `/api/tasks/{id}/complete` | Mark task complete (triggers A2A) |
| `POST` | `/api/tasks/{id}/fail` | Mark task failed (triggers retry or intervention) |

### Task Creation Flow

```typescript
// POST /api/tasks
// Request for simple task (MenxiaSheng assigns directly)
{
  title: "修一下登录bug",
  description: "用户登录一直报错",
  priority: "high",
  source: "user_command",      // or "agent_discovery", "scheduled"
  raw_input: "#task 用户登录一直报错"
}

// Response
{
  taskGroupId: "xxx",
  taskId: "yyy",
  status: "assigned",
  assignedAgent: "gongbu",      // MenxiaSheng's decision
  message: "任务已分配给 gongbu"
}
```

### MenxiaSheng Decomposition API

```typescript
// Internal API called by MenxiaSheng agent
// POST /api/task-groups/{id}/decompose
{
  taskGroupId: "xxx",
  action: "decompose"
}

// Response
{
  subtasks: [
    {
      title: "收集登录日志",
      assignedAgent: "hubu",
      dependsOn: [],
      context: {...}
    },
    {
      title: "分析登录问题",
      assignedAgent: "xingbu", 
      dependsOn: ["subtask-1"],
      context: {...}
    },
    {
      title: "修复登录bug",
      assignedAgent: "gongbu",
      dependsOn: ["subtask-2"],
      context: {...}
    }
  ]
}
```

### Exception Handling API

```typescript
// GET /api/tasks/needs-intervention
// Returns tasks that exceeded max_retries
{
  tasks: [
    {
      taskId: "xxx",
      taskGroupId: "yyy",
      title: "bug修复",
      assignedAgent: "gongbu",
      retryCount: 4,
      maxRetries: 3,
      lastError: "Connection timeout"
    }
  ]
}

// PATCH /api/tasks/{id}/reassign
{
  newAssignedAgent: "bingbu",  // Human chooses new agent
  reason: "gongbu 不可用"
}
```

---

## MenxiaSheng Agent Prompt Design

MenxiaSheng operates with the following core prompt:

```
## 你是任务调度专家 (MenxiaSheng / 门下省)

### 你的职责
1. 分析用户任务，判断复杂度
2. 简单任务：直接分配给最合适的 agent，监控完成，汇报结果
3. 复杂任务：拆解成原子性子任务，分配执行，收集结果，聚合汇报

### 任务分析原则
- 字数多、目标多、需要多 agent 协作 = 复杂
- 单一目标、明确执行路径 = 简单

### 拆解原则
- 每个子任务应该是原子性的，可独立执行
- 子任务之间如果有依赖，必须明确标注
- 选择 agent 时考虑：技能匹配、当前负载、历史表现

### 汇报原则
- 汇报给用户时必须有完整上下文
- 成功汇报：总结做了什么、结果如何
- 失败汇报：说明问题、尝试了什么、建议

### 可用工具
- task_create: 创建任务
- task_list: 查看任务列表
- task_complete: 标记任务完成
- task_fail: 标记任务失败
- sessions_send: 向其他 agent 发消息
```

---

## Frontend Design

### Task Group View (extends TaskQueue.vue)

```
┌─────────────────────────────────────────────────────────────┐
│  🔵 进行中 (5)    🔴 需介入 (2)    🟢 已完成 (12)           │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🔵 任务组: 用户登录报错                    [查看详情 ▼]    │
│     ├── [gongbu] bug修复         running     ...           │
│     ├── [hubu] 日志收集           completed ✓               │
│     └── [xingbu] 原因分析         completed ✓               │
│                                                             │
│     进度: 2/3 完成                                        │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🔴 任务组: 支付接口优化                    [需人工介入]   │
│     ├── [gongbu] 性能优化         pending     超时4次!     │
│     │                               └── [重新指派] [放弃]   │
│     └── [hubu] 数据分析             failed ✓                │
│                                                             │
│     上次错误: Connection timeout                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Task Group Detail Drawer

```
┌─────────────────────────────────────────────────────────────┐
│  任务组详情                                    [×]          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  总目标:                                                    │
│  修复用户登录问题                                            │
│                                                             │
│  整体设计:                                                  │
│  1. hubu 收集登录日志                                       │
│  2. xingbu 分析登录失败原因                                  │
│  3. gongbu 修复发现的 bug                                   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  子任务:                                                    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [hubu] 日志收集                            ✅ 完成   │   │
│  │ 耗时: 2分钟                                              │   │
│  │ 结果: 收集到50条登录日志，发现3次session超时           │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [xingbu] 原因分析                          ✅ 完成   │   │
│  │ 耗时: 5分钟                                              │   │
│  │ 结果: session配置过期，导致每次登录都失败               │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ [gongbu] Bug修复                            🔄 进行中 │   │
│  │ 当前步骤: 修改 session 配置...                       │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  📋 MenxiaSheng 汇报:                                      │
│  "登录问题正在修复中，预计还需2分钟完成。"                    │
└─────────────────────────────────────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Core Infrastructure
1. Extend `task_groups` table and `tasks` table
2. Implement `#task` command parsing
3. Create TaskGroup API endpoints
4. Add MenxiaSheng as default decomposer agent

### Phase 2: Task Decomposition
5. Implement MenxiaSheng decomposition logic
6. Add agent capability matching
7. Implement subtask creation with dependencies
8. Connect to A2A message system

### Phase 3: Result Aggregation
9. Implement A2A notification on task completion
10. Build MenxiaSheng result collection logic
11. Create aggregation and reporting
12. Add user-facing completion messages

### Phase 4: Exception Handling
13. Implement retry with exponential backoff
14. Add "needs intervention" detection
15. Build UI for human intervention
16. Add reassignment functionality

### Phase 5: UI & Polish
17. Extend TaskQueue.vue with group view
18. Add task group detail drawer
19. Add intervention task highlighting
20. Docker one-click deployment

---

## Risks / Trade-offs

| Risk | Impact | Mitigation |
|------|--------|------------|
| MenxiaSheng decomposition quality | Bad decomposition = failed tasks | Start with conservative decomposition, iterate |
| Agent capability mismatch | Wrong agent assigned | Allow manual reassignment, track success rate |
| A2A message delivery failure | MenxiaSheng misses notifications | Fallback to polling after timeout |
| Cycle in task dependencies | Deadlock | Validate DAG before creating subtasks |

---

## Open Questions

1. **Multi-level decomposition**: Should we support subtasks of subtasks? Or flat decomposition only?

2. **Timeout values**: What should the base delay be for exponential backoff? (5s? 10s?)

3. **MenxiaSheng persistence**: Should MenxiaSheng's state be persisted across restarts? Or recomputed from DB?

4. **Concurrent task limits**: Should we limit how many tasks a single agent can have pending?
