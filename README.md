# AgentForge - Multi-Agent Management System

基于OpenClaw的可视化多Agent管理系统。

## 快速开始

### 开发模式

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:5177

### 生产构建

```bash
# 构建
npm run build

# 预览
npm run preview
```

## 环境变量

| 变量              | 说明             | 默认值                |
| ----------------- | ---------------- | --------------------- |
| VITE_OPENCLAW_API | OpenClaw API地址 | http://localhost:3000 |
| VITE_APP_TITLE    | 应用标题         | AgentForge            |
| VITE_DEV_PORT     | 开发服务器端口   | 5177                  |

## 功能模块

- 📊 系统概览 - Gateway状态、系统资源
- 👩‍💼 Agent管理 - Agent列表与详情
- ⏰ Cron任务 - 定时任务监控
- 📋 任务队列 - 异步任务管理
- 🔗 任务组 - 任务组与依赖关系
- 💬 会话管理 - 会话统计
- 💰 Token统计 - Token消耗与成本分析

## 技术栈

- Vue3 + TypeScript
- Vite
- Element Plus
- Pinia
- ECharts
- better-sqlite3

## 核心特性

### Agent管理

- 可视化Agent列表与组织架构图
- Agent元数据管理（名称、封号、角色、描述）
- Agent关系配置（上下级关系）

### 任务控制

- 任务列表实时监控
- 任务启动/停止/重试
- 任务详情与日志查看

### Token统计

- Token消耗实时统计
- 按Agent/Model分组分析
- 成本计算与趋势图表

## 部署

### Docker

```bash
docker-compose up -d
```

### 手动部署

1. 构建: `npm run build`
2. 配置Nginx
3. 启动服务

---

_AgentForge - 让Agent管理更简单_
