# OpenClaw Integration

> English | [中文](#openclaw-集成)

ClawDash integrates with OpenClaw agents for distributed task execution through webhooks and a task distribution skill.

## Task Distribution Skill

ClawDash provides a Task Distribution Skill for OpenClaw agents.

### Features

- **TaskDistributor Mode**: Receives TaskGroup and decomposes into subtasks
- **TaskExecutor Mode**: Claims and executes assigned tasks

### Installation

The skill is automatically installed by OpenClawService to:
```
~/.openclaw/workspace/skills/task-distribution/SKILL.md
```

### Configuration

1. In ClawDash, navigate to "Agent Task Configuration"
2. Select the TaskDistributor agent
3. Add bindings for task executor agents
4. Specify task types each agent handles

## API Endpoints

### Task Management

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/task-queue/tasks` | POST | Create a new task |
| `/api/task-queue/tasks` | GET | List tasks (supports `assignedAgent` filter) |
| `/api/task-queue/tasks/{id}/claim` | POST | Claim a task |
| `/api/task-queue/tasks/{id}/complete` | POST | Mark task as completed |
| `/api/task-queue/notify-agent` | POST | Manually trigger agent notification |
| `/api/task-queue/agent-stats` | GET | Get agent workload statistics |

### Task Groups

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/task-groups` | POST | Create a task group |
| `/api/task-groups` | GET | List task groups |
| `/api/task-groups/{id}` | GET | Get task group details |
| `/api/task-groups/{id}/detail` | GET | Get task group with full subtask info |

### Agent Bindings

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/agent-bindings` | GET | List all agent bindings |
| `/api/agent-bindings` | POST | Create agent binding |
| `/api/agent-bindings/{id}` | DELETE | Remove agent binding |

## Webhook Notifications

ClawDash sends webhook notifications to OpenClaw agents when:

| Event | Recipient | Payload |
|-------|-----------|---------|
| TaskGroup created | TaskDistributor | `{ "event": "task_group_created", "taskGroupId": "..." }` |
| Task assigned | Assigned agent | `{ "event": "task_assigned", "taskId": "..." }` |
| All subtasks completed | Primary agent | `{ "event": "task_group_completed", "taskGroupId": "..." }` |

### Webhook Configuration

Configure webhook URLs in ClawDash settings or via the OpenClaw integration page.

## Workflow Diagram

```
┌─────────────┐     webhook      ┌─────────────────┐
│   ClawDash  │ ───────────────► │ TaskDistributor │
│  (TaskGroup)│                  │     Agent       │
└─────────────┘                  └────────┬────────┘
                                          │ decompose
                                          ▼
                                 ┌─────────────────┐
                                 │    Subtasks     │
                                 │  (assigned to   │
                                 │   agents)       │
                                 └────────┬────────┘
                                          │ webhooks
                                          ▼
                    ┌─────────────────────────────────────┐
                    │                                     │
                    ▼                                     ▼
           ┌───────────────┐                   ┌───────────────┐
           │   Executor    │                   │   Executor    │
           │   Agent A     │                   │   Agent B     │
           └───────┬───────┘                   └───────┬───────┘
                   │ claim & execute                   │ claim & execute
                   ▼                                   ▼
           ┌───────────────────────────────────────────────┐
           │                  ClawDash                     │
           │         (tracks completion)                   │
           └───────────────────────┬───────────────────────┘
                                   │ all complete
                                   ▼
                          ┌─────────────────┐
                          │  Primary Agent  │
                          │   (notified)    │
                          └─────────────────┘
```

## Task Distribution Skill Reference

The skill is installed to OpenClaw workspace and provides:

### TaskDistributor Behavior

1. Receive webhook with TaskGroup ID
2. Fetch TaskGroup details from ClawDash API
3. Decompose into subtasks based on task type
4. Create subtasks via ClawDash API
5. Assign to appropriate agents based on bindings

### TaskExecutor Behavior

1. Receive webhook with assigned task ID
2. Claim task via ClawDash API
3. Execute task logic
4. Report completion via ClawDash API

---

# OpenClaw 集成

ClawDash 通过 webhook 和任务分发技能与 OpenClaw 代理集成，实现分布式任务执行。

## 任务分发技能

ClawDash 为 OpenClaw 代理提供任务分发技能。

### 功能

- **TaskDistributor 模式**：接收 TaskGroup 并分解为子任务
- **TaskExecutor 模式**：认领并执行分配的任务

### 安装

技能通过 OpenClawService 自动安装到：
```
~/.openclaw/workspace/skills/task-distribution/SKILL.md
```

### 配置

1. 在 ClawDash 中，导航到「代理任务配置」
2. 选择 TaskDistributor 代理
3. 为任务执行代理添加绑定
4. 指定每个代理处理的任务类型

## API 端点

### 任务管理

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/task-queue/tasks` | POST | 创建任务 |
| `/api/task-queue/tasks` | GET | 列出任务（支持 `assignedAgent` 过滤） |
| `/api/task-queue/tasks/{id}/claim` | POST | 认领任务 |
| `/api/task-queue/tasks/{id}/complete` | POST | 标记任务完成 |
| `/api/task-queue/notify-agent` | POST | 手动触发代理通知 |
| `/api/task-queue/agent-stats` | GET | 获取代理工作负载统计 |

### 任务组

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/task-groups` | POST | 创建任务组 |
| `/api/task-groups` | GET | 列出任务组 |
| `/api/task-groups/{id}` | GET | 获取任务组详情 |
| `/api/task-groups/{id}/detail` | GET | 获取任务组及完整子任务信息 |

### 代理绑定

| 端点 | 方法 | 说明 |
|------|------|------|
| `/api/agent-bindings` | GET | 列出所有代理绑定 |
| `/api/agent-bindings` | POST | 创建代理绑定 |
| `/api/agent-bindings/{id}` | DELETE | 删除代理绑定 |

## Webhook 通知

ClawDash 在以下情况向 OpenClaw 代理发送 webhook 通知：

| 事件 | 接收者 | 载荷 |
|------|--------|------|
| TaskGroup 创建 | TaskDistributor | `{ "event": "task_group_created", "taskGroupId": "..." }` |
| 任务分配 | 被分配的代理 | `{ "event": "task_assigned", "taskId": "..." }` |
| 所有子任务完成 | 主代理 | `{ "event": "task_group_completed", "taskGroupId": "..." }` |

### Webhook 配置

在 ClawDash 设置或 OpenClaw 集成页面配置 webhook URL。

## 工作流程图

```
┌─────────────┐     webhook      ┌─────────────────┐
│   ClawDash  │ ───────────────► │ TaskDistributor │
│  (TaskGroup)│                  │     Agent       │
└─────────────┘                  └────────┬────────┘
                                          │ 分解
                                          ▼
                                 ┌─────────────────┐
                                 │     子任务      │
                                 │  (分配给代理)   │
                                 └────────┬────────┘
                                          │ webhooks
                                          ▼
                    ┌─────────────────────────────────────┐
                    │                                     │
                    ▼                                     ▼
           ┌───────────────┐                   ┌───────────────┐
           │   Executor    │                   │   Executor    │
           │   Agent A     │                   │   Agent B     │
           └───────┬───────┘                   └───────┬───────┘
                   │ 认领并执行                        │ 认领并执行
                   ▼                                   ▼
           ┌───────────────────────────────────────────────┐
           │                  ClawDash                     │
           │           (追踪完成状态)                      │
           └───────────────────────┬───────────────────────┘
                                   │ 全部完成
                                   ▼
                          ┌─────────────────┐
                          │  主代理         │
                          │   (收到通知)    │
                          └─────────────────┘
```

## 任务分发技能参考

该技能安装到 OpenClaw 工作区并提供：

### TaskDistributor 行为

1. 接收包含 TaskGroup ID 的 webhook
2. 从 ClawDash API 获取 TaskGroup 详情
3. 根据任务类型分解为子任务
4. 通过 ClawDash API 创建子任务
5. 根据绑定关系分配给相应代理

### TaskExecutor 行为

1. 接收包含分配任务 ID 的 webhook
2. 通过 ClawDash API 认领任务
3. 执行任务逻辑
4. 通过 ClawDash API 报告完成状态
