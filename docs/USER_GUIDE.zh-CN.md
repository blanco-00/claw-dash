# ClawDash 用户指南

> [English](./USER_GUIDE.md) | 中文

## 概述

ClawDash 是一款面向 OpenClaw 的可视化配置工具。通过图形界面管理多 Agent 拓扑、配置 Agent 路由、监控运行时状态。

## 功能模块

### 1. Agent 配置图谱

可视化拓扑编辑器，管理 Agent 关系。

- **添加节点**：使用模板创建新 Agent
- **连接边**：从源 Agent 拖拽到目标 Agent 创建通信路由
- **边配置**：为每条连接配置 task → reply → error 三层路由
- **同步预览**：预览变更后再写入 `AGENTS.md`

### 2. Agent 列表

查看和管理所有已注册的 Agent。

- 查看 Agent 状态（运行中/已停止）
- 配置 Agent 参数
- 监控 Agent 健康状态

### 3. 运行时图谱

实时查看 Agent 通信状态。

- 查看 Agent 间的活跃会话
- 实时监控消息流转

### 4. 任务队列

异步任务管理，支持优先级、重试、依赖。

- 创建自定义 Payload 的任务
- 设置优先级
- 配置重试策略
- 查看任务状态和结果

### 5. Cron 任务

定时任务管理。

- 创建循环执行的时间表达式
- 查看执行历史
- 启用/禁用任务计划

### 6. 任务组

批量任务管理与依赖链。

- 将相关任务分组
- 配置任务间依赖关系
- 并行或顺序执行

### 7. 会话管理

活跃会话监控。

- 查看所有活跃 Agent 会话
- 会话统计和持续时间
- 必要时结束会话

### 8. Token 管理

Agent 认证的 API Token 管理。

- 创建和吊销 Token
- 设置访问权限
- 查看使用统计

### 9. OpenClaw

OpenClaw 一键安装与管理。

- 一键安装 OpenClaw
- 监控安装状态
- 管理插件

### 10. Docker 监控

OpenClaw 服务的容器监控。

- 查看容器状态
- 资源使用情况（CPU、内存）
- 容器日志

### 11. 边类型管理

Agent 路由的边类型定义管理。

- 定义自定义边类型
- 配置路由规则
- 设置默认行为

### 12. 代理任务配置

配置 OpenClaw 代理进行分布式任务执行。

- **TaskDistributor 配置**：选择负责分解任务组的主代理
- **代理绑定**：将特定任务类型分配给指定代理
- **工作负载监控**：追踪每个代理的待处理、进行中和已完成任务

#### 配置步骤

1. **配置 TaskDistributor**
   - 导航到「代理任务配置」页面
   - 选择负责分解任务组的主代理

2. **配置代理绑定**
   - 点击「添加绑定」
   - 选择代理
   - 选择该代理负责的任务类型
   - 点击添加保存

3. **查看工作负载**
   - 待处理 (pending)：等待执行的任务数
   - 进行中 (running)：正在执行的任务数
   - 已完成 (completed)：已完成的任务数

#### 工作流程

1. 用户创建 TaskGroup → ClawDash 触发 webhook 通知 TaskDistributor
2. TaskDistributor 分解任务 → 创建 subtasks 并分配给相应代理
3. 各代理通过 webhook 收到通知 → claim 并执行 subtasks
4. 所有 subtasks 完成后 → ClawDash 通知主代理

## 快速开始

### 登录

首次访问系统，使用默认管理员账户登录（或在开发模式下跳过认证）。

### 创建任务

1. 进入「**任务队列**」
2. 点击「**新建任务**」
3. 填写任务类型、Payload、优先级
4. 点击「**创建**」

### 配置 Agent 路由

1. 进入「**Agent → 配置图谱**」
2. 点击「**添加节点**」创建或选择 Agent
3. 从源 Agent 拖拽到目标 Agent 创建边
4. 点击边打开配置面板
5. 设置**任务消息**（左侧）和**回复/错误路由**（右侧）
6. 点击「**同步到 OpenClaw**」预览并应用变更

### 监控 Agent

1. 进入「**Agent → 运行时图谱**」查看活跃通信
2. 进入「**会话**」监控活跃连接
3. 进入「**任务队列**」追踪异步任务执行

## 常见问题

### Q: 如何启用认证？

A: 在环境变量中设置 `AUTH_ENABLED=true`，然后通过 `/api/auth/register` 注册用户。

### Q: 如何查看 API 文档？

A: 访问 `/swagger-ui.html` 查看完整的 API 文档。

### Q: 如何扩展功能？

A: 在 `backend/src/main/java/com/clawdash/controller` 下添加新的 Controller。

## 技术栈

- **后端**: Spring Boot 3.4, MyBatis-Plus, JWT, Redis
- **前端**: Vue 3, TypeScript, Element Plus, ECharts
- **数据库**: MySQL 8.0, Redis
- **部署**: Docker Compose
