# ClawDash 用户指南

> [English](./USER_GUIDE.md) | 中文

## 概述

ClawDash 是一个基于 OpenClaw 的多 Agent 可视化管理系统，采用中国古代王朝（女儿国）风格设计。

## 功能模块

### 1. 系统概览

- Gateway 状态监控
- 系统资源使用情况
- 实时任务统计

### 2. Agent 管理

- 查看所有 Agent 列表
- 配置 Agent 参数
- 监控 Agent 状态

### 3. 任务队列

- 创建新任务
- 查看任务列表
- 任务状态跟踪

### 4. Cron 任务

- 创建定时任务
- 管理任务计划
- 查看执行历史

### 5. 任务组

- 创建任务组
- 批量任务管理
- 任务依赖配置

### 6. Token 管理

- 创建 API Token
- 管理访问权限
- 查看使用统计

### 7. 会话管理

- 查看活跃会话
- 结束会话
- 会话统计

### 8. OpenClaw 集成

- 一键安装 OpenClaw
- 状态监控
- 插件管理

### 9. Docker 监控

- 容器状态
- 镜像管理
- 资源统计

### 10. 财务管理

- 收入记录
- 支出记录
- 月度报表
- 预算管理

## 快速开始

### 登录

首次访问系统，使用默认管理员账户登录（或跳过认证）。

### 创建任务

1. 进入「任务队列」页面
2. 点击「新建任务」
3. 填写任务类型、Payload、优先级
4. 点击「创建」

### 配置 Cron 任务

1. 进入「Cron 任务」页面
2. 点击「新建任务」
3. 填写任务名称、Cron 表达式
4. 设置任务模板
5. 启用任务

## 常见问题

### Q: 如何启用认证？

A: 在环境变量中设置 `AUTH_ENABLED=true`，然后通过 `/api/auth/register` 注册用户。

### Q: 如何查看 API 文档？

A: 访问 `/swagger-ui.html` 查看完整的 API 文档。

### Q: 如何扩展功能？

A: 可以在 `server/` 下添加新的 API 路由。

## 技术栈

- **后端**: Node.js + Express + TypeScript + MySQL + Redis
- **前端**: Vue 3 + TypeScript + Element Plus + ECharts
- **部署**: Docker Compose
