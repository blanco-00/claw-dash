# Task-Queue 集成方案

> 更新日期：2026-03-20

## 背景

OpenClaw 项目有一个内置的任务队列插件 (`@openclaw-task-queue/core`)，基于 SQLite 实现。ClawDash 当前使用的是基于 MySQL + Redis 的旧版实现。

## 目标

让 OpenClaw 使用新版 task-queue 插件，并在 ClawDash 中实现任务管理和监控功能。

---

## 新版 task-queue 特性

| 特性 | 说明 |
|-----|------|
| 存储 | SQLite (单文件，无外部依赖) |
| 并发安全 | CAS 原子操作 |
| 任务类型 | task_create, task_status, task_list, task_stats, task_decompose 等 |
| 生命周期 | PENDING → RUNNING → COMPLETED/FAILED → DEAD |
| 内置 Cron | 自动领取和执行任务 |

---

## 集成方案

### 方案 A：OpenClaw 插件模式（推荐）

OpenClaw 独立运行，task-queue 作为其插件：

```
┌─────────────────┐     ┌─────────────────┐
│   ClawDash      │     │   OpenClaw      │
│   (5178)       │────▶│   (3000)        │
│                 │     │                 │
│  任务监控面板   │     │  task-queue 插件 │
└─────────────────┘     └─────────────────┘
```

**优点**：
- OpenClaw 自主管理任务队列
- 插件自动提供 AI 工具
- 架构清晰

**需要实现**：
1. OpenClaw 安装并启用 task-queue 插件
2. ClawDash 调用 OpenClaw API 获取任务状态

### 方案 B：ClawDash 嵌入模式

ClawDash 后端直接使用 npm 包：

```typescript
import { TaskQueue, TaskWorker } from "@openclaw-task-queue/core";

const queue = new TaskQueue({ dbPath: "./tasks.db" });
```

**优点**：
- 更紧密的集成
- 可完全控制任务队列

**缺点**：
- 需要 Node.js + native 依赖
- 与现有 Java 后端架构不一致

---

## 待确定问题

1. **方案选择**：你倾向 A 还是 B？
2. **部署方式**：OpenClaw 如何部署？（本地/Docker/云）
3. **API 需求**：ClawDash 需要哪些任务管理功能？
   - 查看任务列表
   - 创建任务
   - 监控状态
   - 手动触发/取消

---

## 相关文件

- 新版包：`/Users/hannah/git/openclaw-task-queue/`
- 旧版实现：`/Users/hannah/git/claw-dash/backend/src/main/java/com/clawdash/service/TaskQueueService.java`
- 前端页面：`/Users/hannah/git/claw-dash/frontend/src/views/TaskQueue.vue`
