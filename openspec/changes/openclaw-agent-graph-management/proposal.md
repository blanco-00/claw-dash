# OpenClaw Agent 图谱管理提案

## Why

当前 OpenClaw 的 agent 管理主要通过 CLI 命令行和配置文件实现，缺乏可视化的关系图谱界面。用户希望 clawdash 能够以**节点-关系图**的方式管理 OpenClaw 的多 agent 系统，实现：

1. **可视化拓扑**：直观展示 agent 之间的任务分配和汇报关系
2. **一键同步**：通过图形界面创建/更新 OpenClaw agent 及关系
3. **元数据管理**：维护 agent 的业务属性（角色、能力、负责人等）

这是因为随着 agent 数量增长，手动管理配置文件变得复杂，需要一个直观的图形化工具来降低运维复杂度。

## What Changes

### 核心功能变更

1. **Agent 节点管理**
   - 在 clawdash 中维护 OpenClaw agent 元数据（节点）
   - **不修改数据库中已存在的 agent**（只读）
   - 支持创建新的 agent（通过页面添加 → 调用 CLI）
   - 支持编辑/删除新创建的 agent

2. **关系图谱可视化**
   - 使用力导向图展示 agent 节点
   - 边（Edge）表示两种关系：
     - **分配关系**（assigns）：上级 agent 可以给下级 agent 分配任务
     - **汇报关系**（reports）：下级 agent 完成任务后向上级汇报
   - 支持拖拽、缩放、点击交互

3. **同步机制**
   - **同步按钮**：将图谱中的节点和关系同步到 OpenClaw
   - 调用 OpenClaw CLI 命令或 API 实现创建/更新
   - 支持双向同步（读取现有配置 → 编辑 → 写回）

### 技术方案

1. **元数据存储**
   - 使用本地 JSON/YAML 文件存储 agent 关系图谱
   - 兼容 OpenClaw 的 agent 目录结构

2. **OpenClaw 集成**
   - 读取：`openclaw agents list` 获取现有 agent
   - 写入：`openclaw agents add <name>` 创建新 agent
   - 配置：通过修改 `AGENT.md` 或配置文件设置子代理关系

## Capabilities

### New Capabilities

- **openclaw-agent-graph**: OpenClaw agent 关系图谱管理
  - 节点管理：CRUD agent 元数据
  - 关系管理：定义任务分配和汇报关系
  - 可视化：力导向图展示拓扑
  - 同步：与 OpenClaw 双向同步

### Modified Capabilities

无。现有能力不涉及 OpenClaw agent 管理。

## Impact

### 受影响的系统

- **clawdash 前端**：新增 agent 图谱可视化页面
- **clawdash 后端**：新增 OpenClaw 集成模块
- **OpenClaw**：通过 CLI/API 集成，无侵入

### 依赖

- **可视化库**：React Flow 或 Reagraph（WebGL 图可视化）
- **OpenClaw CLI**：本地安装并可访问
- **HTTP 客户端**：调用 OpenClaw Gateway API（可选）

### 技术选型

| 组件     | 技术栈                 | 职责                                       |
| -------- | ---------------------- | ------------------------------------------ |
| 后端 API | Spring Boot (Java)     | 所有业务逻辑：任务创建/领取/执行/缓存      |
| 数据库   | MySQL                  | 持久化存储                                 |
| 旁路缓存 | Redis                  | cache-aside 缓存模式、优先级队列、分布式锁 |
| 前端     | 现有 Vue3 + TypeScript | **仅显示**，调用后端 API                   |

---

## 调研结论

### OpenClaw Agent 通信机制（已验证本地配置 + 官方文档）

#### 1. Session Tools（OpenClaw 内置工具）

在 `~/.openclaw/workspace/memory/2026-02-27.md` 中记录了可用工具：

| 工具             | 说明                   | 用途             |
| ---------------- | ---------------------- | ---------------- |
| `agents_list`    | 列出可用的 agent id    | 查看有哪些 Agent |
| `sessions_list`  | 列出其他会话           | 查看活跃会话     |
| `sessions_send`  | **向其他会话发送消息** | Agent 间通信     |
| `sessions_spawn` | 启动子 agent           | 临时任务调用     |
| `subagents`      | 管理子 agent           | list/kill/steer  |

#### 2. Task Queue 插件（openclaw-task-queue）

你计划在 clawdash 中重新实现这个插件，提供：

| 工具          | 说明         | 用途     |
| ------------- | ------------ | -------- |
| `task_create` | 创建异步任务 | 分发任务 |
| `task_list`   | 拉取任务队列 | 接收任务 |
| `task_status` | 更新任务状态 | 汇报结果 |

#### 3. 配置定义（SOUL.md）

在各个 workspace 的 `SOUL.md` 中定义：

```markdown
## 交互关系

| 关系     | 对象             | 说明             |
| -------- | ---------------- | ---------------- |
| 接收任务 | 尚书省/工部/主人 | 接收异步任务     |
| 汇报对象 | 工部（灵犀）     | 定期汇报执行情况 |
```

#### 4. 已有 Agent 拓扑示例

| Agent                | 角色        | 接收任务来源  | 汇报对象      |
| -------------------- | ----------- | ------------- | ------------- |
| main (瑾儿)          | 中书省/皇后 | 主人          | -             |
| shangshusheng (红袖) | 尚书省      | main          | main          |
| menxiasheng          | 门下省/审核 | -             | main          |
| jinyiwei             | 锦衣卫/督查 | shangshusheng | shangshusheng |
| jishu (青岚)         | 工部/技术   | shangshusheng | 工部          |
| gongbu               | 工部        | shangshusheng | shangshusheng |
| hubu                 | 户部        | shangshusheng | shangshusheng |
| ...                  | 六部其他    | shangshusheng | shangshusheng |

### 关系建模

用户提出的「分配-汇报」关系本质上是 **有向图**：

```
        assigns (任务分配)
  Main ──────────────────► Shangshusheng
    │                         │
    │                         ▼
    │                   ┌──────────┐
    │                   │  Jinyiwei │
    │                   └──────────┘
    │                         │
    └──────────────────► ┌──────────┐
                          │   Gongbu │
                          │   Jishu  │
                          │   Hubu   │
                          └──────────┘
```

- Main 是根节点，**只读不修改**
- 各 Agent 的关系定义在各自 workspace 的 `SOUL.md` 中
- 边的方向：assigns（分配任务）从上级指向下级

### 可行性评估

✅ **高可行性**：

1. OpenClaw 已有完善的 CLI 接口 (`openclaw agents add/list/delete`)
2. Agent 通信通过 Session Tools + Task Queue + SOUL.md 多方式实现
3. React Flow / Reagraph 等成熟的可视化库
4. 关系模型清晰，适合图数据库或 JSON 存储

### 推荐技术栈

| 层级         | 技术选型                                         |
| ------------ | ------------------------------------------------ |
| 前端图可视化 | React Flow + @xyflow/react（成熟稳定，35.7K ⭐） |
| 备选可视化   | Reagraph（WebGL，适合大规模节点）                |
| 状态管理     | Zustand / Jotai                                  |
| 后端集成     | Node.js child_process 调用 CLI                   |
| 存储         | 本地 JSON 文件（无需额外数据库）                 |

### 实施建议

1. **MVP**：扫描 `~/.openclaw/agents/` 和各 workspace 的 `SOUL.md` 生成关系图谱（只读）
2. **进阶**：支持创建新 agent → 调用 `openclaw agents add` 同步
3. **完善**：关系编辑 + SOUL.md 模板同步

---

_调研完成日期：2026-03-19_
