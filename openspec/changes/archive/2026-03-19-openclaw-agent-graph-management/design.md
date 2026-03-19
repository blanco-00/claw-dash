# OpenClaw Agent 图谱管理 - 技术设计

## Context

### 背景

clawdash 是一个本地 AI agent 管理/监控面板，需要新增功能来管理 OpenClaw 的多 agent 系统。用户希望以「节点-关系图谱」的可视化方式，直观地管理 agent 之间的任务分配和汇报关系。

### 当前状态

- clawdash 已有基础 UI 框架和 agent 列表展示
- OpenClaw 通过 CLI 提供 agent 管理能力
- 缺少可视化的关系图谱和拓扑管理

### 约束

1. **运行环境**：macOS/Linux 本地（OpenClaw 运行在本地）
2. **技术栈**：React + TypeScript（现有）
3. **OpenClaw 依赖**：通过 CLI 调用（不直接操作数据库）
4. **数据持久化**：本地 JSON 文件（无需额外服务）

### 利益相关者

- AI 开发者/爱好者（使用 OpenClaw 管理多个 agent）
- 需要可视化理解 agent 拓扑的用户

---

## Goals / Non-Goals

### Goals

1. **图谱可视化**：使用力导向图展示 agent 节点及关系
2. **节点 CRUD**：创建、读取、更新、删除 agent 元数据
3. **关系管理**：定义「任务分配」和「工作汇报」关系
4. **双向同步**：
   - 读取：扫描 OpenClaw 配置 → 生成图谱
   - 写入：图谱编辑 → 调用 CLI 同步到 OpenClaw
5. **Main 节点只读**：系统 main agent 不可修改

### Non-Goals

- 实时监控 agent 状态（属于另一个功能）
- OpenClaw 之外的 agent 框架支持
- 云端部署/远程管理
- 复杂的权限和审计日志

---

## Decisions

### D1: 图可视化库选型

**决策**：使用 **React Flow** (@xyflow/react) 作为图可视化库

**备选**：

- React Flow (35.7K ⭐) - 成熟稳定，文档完善，适合节点编辑器
- Reagraph (997 ⭐) - WebGL 渲染，适合大规模节点，但学习曲线陡
- react-force-graph - 力导向图，2D/3D，但定制性较低

**理由**：

1. React Flow 是节点编辑器的事实标准，API 清晰
2. 支持自定义节点组件（可放入 agent 头像、状态指示器）
3. 社区活跃，TypeScript 支持好
4. 足够满足「数十个节点」规模的性能需求

### D2: 元数据存储格式

**决策**：使用 **JSON 文件**存储 agent 图谱元数据

**备选**：

- SQLite - 需额外依赖，overkill
- 图数据库 (Neo4j) - 引入重型依赖
- OpenClaw 配置目录 - 可能与 OpenClaw 升级冲突

**理由**：

1. 简单，无需额外服务
2. 易于版本控制（git）
3. 与 OpenClaw 的 YAML/JSON 配置风格一致
4. 足够满足「本地单人使用」场景

### D3: OpenClaw 集成方式

**决策**：通过 **CLI 命令**集成，优先 CLI 降级到 API

**备选**：

- 直接读取 OpenClaw 配置文件 - 可能因版本变化失效
- HTTP API 调用 - 更灵活但需要 Gateway 开启
- WebSocket 实时 - 过度工程

**理由**：

1. CLI 是官方推荐的稳定接口
2. `openclaw agents list/add/delete` 已覆盖核心操作
3. Gateway API (18789 端口) 可作为进阶选项

> ⚠️ **澄清**：这里管理的 Agent 是**长期代理**（通过 `openclaw agents add` 创建），不是临时的 Sub-agent（通过 `/subagents spawn` 创建）。

### D4: Agent 关系建模

**决策**：通过解析各 workspace 的 **SOUL.md** 提取关系

**关系来源**：

- `SOUL.md` 中的「交互关系」表格
- `SOUL.md` 中的「接收任务」和「汇报对象」字段

**边类型**：

- `assigns`：任务分配关系（上级 → 下级）
- `reports`：汇报关系（下级 → 上级）

### D5: Task Queue 重实现

**决策**：在现有 Spring Boot 项目中新增 Task Queue 代码，复用现有目录结构

**代码位置**（按现有结构分布）：

| 现有目录    | 新增文件                                                  |
| ----------- | --------------------------------------------------------- |
| entity/     | TaskQueueTask.java, TaskStatus.java                       |
| mapper/     | TaskQueueTaskMapper.java                                  |
| dto/        | CreateTaskRequest.java, TaskPageResponse.java             |
| controller/ | TaskQueueController.java                                  |
| service/    | TaskQueueService.java, TaskWorker.java, CacheService.java |
| config/     | (复用现有 RedisConfig)                                    |

**API 端点**：`/api/task-queue/**`

**架构**：

```
┌─────────────────┐     REST API      ┌─────────────────────┐
│   Frontend     │ ◄────────────────► │   Spring Boot      │
│  (Display Only)│                   │   (Existing)       │
└─────────────────┘                   └─────────┬─────────┘
                                                  │
                           ┌──────────────────────┼──────────────────────┐
                           ▼                      ▼                      ▼
                    ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
                    │    MySQL    │      │    Redis    │      │   Worker   │
                    │ (Existing)  │      │ (Existing)  │      │ (New)       │
                    └─────────────┘      └─────────────┘      └─────────────┘
```

**核心设计问题**：

| 问题             | 解决方案                                                                          |
| ---------------- | --------------------------------------------------------------------------------- |
| **重复领取任务** | 使用原子操作：`UPDATE tasks SET status='RUNNING' WHERE id=? AND status='PENDING'` |
| **并行任务数**   | 配置 `concurrency` 参数，默认 1，可调整                                           |
| **任务状态**     | PENDING → RUNNING → COMPLETED/FAILED/DEAD                                         |
| **执行结果**     | 存储在 `result` 字段（JSON）                                                      |
| **错误信息**     | 存储在 `error` 字段                                                               |
| **重试机制**     | `retry_count` + `max_retries`，可配置                                             |
| **任务依赖**     | `depends_on` 字段，支持任务链                                                     |
| **优先级**       | `priority` 字段：HIGH=10, MEDIUM=5, LOW=0                                         |
| **定时任务**     | `scheduled_at` 字段，支持延迟执行                                                 |

**理由**：

1. 复用 clawdash 现有的 MySQL + Redis 基础设施
2. 提供标准 REST API，方便 OpenClaw 调用
3. 支持任务优先级、延迟调度、重试机制
4. 与 clawdash 现有架构统一

### D6: 元数据存储设计

**决策**：图谱元数据独立存储，不直接解析 OpenClaw 内部配置

**理由**：

1. 隔离关注点：图谱是「业务视图」，OpenClaw 配置是「技术实现」
2. 允许添加 OpenClaw 不直接支持的业务属性（负责人、描述、标签）
3. 简化同步逻辑：图谱 → OpenClaw 是「发布」而非「双向同步」

---

## Risks / Trade-offs

### R1: OpenClaw 子代理关系配置复杂

**风险**：OpenClaw 的 `allowAgents` 配置语法可能有版本差异

**缓解**：

- MVP 阶段只支持「列出现有 agent」，不自动推断关系
- 关系由用户在图谱中手动定义
- 同步时生成符合主流版本的配置

### R2: 同步冲突

**风险**：图谱修改与 OpenClaw 手动修改可能冲突

**缓解**：

- 同步前显示「差异预览」
- 支持「强制覆盖」或「合并」策略
- 保留配置备份

### R3: 性能

**风险**：大量节点时图渲染卡顿

**缓解**：

- React Flow 支持虚拟化
- 超过 100 节点时提示切换到 Reagraph

---

## 完整项目代码结构

```
claw-dash/
├── frontend/                         # Vue3 + TypeScript 前端
│   ├── src/
│   │   ├── api/
│   │   │   └── taskQueue.ts       # [NEW] Task Queue API
│   │   ├── components/taskQueue/ # [NEW] Task Queue 组件
│   │   │   ├── TaskList.vue      # 任务列表（分页）
│   │   │   └── TaskDetail.vue    # 任务详情
│   │   ├── views/
│   │   │   ├── TaskQueuePage.vue # [NEW] Task Queue 页面
│   │   │   └── AgentGraphPage.vue # [NEW] Agent 图谱页面
│   │   └── ...
│   └── package.json
│
├── backend/                         # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/java/com/clawdash/
│       ├── controller/              # [NEW: TaskQueueController.java]
│       ├── service/                # [NEW: TaskQueueService, TaskWorker, CacheService]
│       ├── mapper/                 # [NEW: TaskQueueTaskMapper.java]
│       ├── entity/                 # [NEW: TaskQueueTask, TaskStatus]
│       ├── dto/                   # [NEW: CreateTaskRequest, TaskPageResponse]
│       ├── config/                # 复用现有
│       └── common/                 # 复用现有
│
├── server/                          # Express.js
├── docker/                          # Docker
└── openspec/                        # OpenSpec
```

---

### 新增文件清单（按目录分布）

| 目录 | 文件 | 说明 |
|------|------|------|
| backend/entity/ | TaskQueueTask.java | 任务实体 |
| backend/entity/ | TaskStatus.java | 状态枚举 |
| backend/mapper/ | TaskQueueTaskMapper.java | MyBatis Mapper |
| backend/dto/ | CreateTaskRequest.java | 创建请求 DTO |
| backend/dto/ | TaskPageResponse.java | 分页响应 DTO |
| backend/controller/ | TaskQueueController.java | REST API |
| backend/service/ | TaskQueueService.java | 业务逻辑 |
| backend/service/ | TaskWorker.java | 任务执行器 |
| backend/service/ | CacheService.java | Redis 缓存 |
| frontend/api/ | taskQueue.ts | 前端 API 客户端 |
| frontend/components/ | taskQueue/TaskList.vue | 任务列表组件 |
| frontend/components/ | taskQueue/TaskDetail.vue | 任务详情组件 |
| frontend/views/ | TaskQueuePage.vue | Task Queue 页面 |
| frontend/views/ | AgentGraphPage.vue | Agent 图谱页面 |

### 阶段一：MVP（1-2 周）

1. 创建 `src/lib/openclaw.ts` - CLI 封装模块
2. 创建 `src/components/AgentGraph/` - React Flow 图组件
3. 创建 `src/pages/AgentGraphPage.tsx` - 图谱页面
4. 实现读取：扫描 OpenClaw agents → 显示节点
5. 实现基础 CRUD：创建/编辑节点（本地 JSON）

### 阶段二：同步（1 周）

1. 实现「同步到 OpenClaw」按钮
2. 调用 `openclaw agents add` 创建新 agent
3. 生成/更新 `allowAgents` 配置
4. 实现差异预览对话框

### 阶段三：完善（1 周）

1. 关系边编辑（拖拽创建边）
2. Main 节点锁定
3. 导入/导出图谱
4. 快捷操作菜单

---

## Open Questions

### Q1: 关系方向语义

**问题**：「分配」和「汇报」两种关系的语义是否清晰？

- A → B 表示「A 给 B 分配任务」
- A → B 表示「A 向 B 汇报工作」

**待定**：是否需要用不同颜色/线型区分？

### Q2: 自动发现关系

**问题**：是否尝试从 OpenClaw 配置自动推断现有关系？

- **方案 A**：只做手动关系定义（简单）
- **方案 B**：解析 `allowAgents` 自动填充（智能但复杂）

**建议**：MVP 采用方案 A，进阶采用方案 B

### Q3: 删除行为

**问题**：删除图谱节点时，是否同步删除 OpenClaw agent？

- **方案 A**：只从图谱移除，不动 OpenClaw（安全）
- **方案 B**：同步删除（彻底但危险）

**建议**：默认方案 A，提供「同时删除」选项

---

## 数据模型设计

```typescript
// 图谱元数据结构
interface AgentGraph {
  version: string
  lastSync: string
  nodes: AgentNode[]
  edges: AgentEdge[]
}

interface AgentNode {
  id: string // OpenClaw agent ID
  name: string // 显示名称
  description?: string // 业务描述
  workspace: string // OpenClaw workspace 路径
  model?: string // 使用的模型
  tags?: string[] // 业务标签
  isMain: boolean // 是否为 main agent（只读）
  createdAt: string
  updatedAt: string
}

interface AgentEdge {
  id: string
  source: string // 分配者/上级
  target: string // 被分配者/下级
  type: 'assigns' | 'reports'
  label?: string // 可选的边标签
}
```

---

_设计完成日期：2026-03-19_
