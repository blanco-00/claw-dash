# ClawDash 增强版 - 一体化方案 Proposal

## Why

当前存在三个独立系统：claw-dash（监控面板）、openclaw-task-queue（任务队列）、jiner（个人管家），它们功能互补但分散在不同仓库，部署繁琐，无法统一管理。随着 OpenClaw 生态发展，亟需一个一体化管理控制系统，提供任务调度、数据可视化、Agent管理、个人助理等完整能力。

## What Changes

### 架构重构

- **前端**：从现有 Vue3 单体重构为基于 vue-pure-admin 的现代化管理后台
- **后端**：从 Express (Node.js) 完全迁移到 Spring Boot (Java)
- **数据库**：从 SQLite 迁移到 MySQL + Redis
- **部署**：Docker Compose 一键启动

### 功能合并

- 整合 task-queue 任务队列核心逻辑（任务创建/执行/依赖/任务组/Cron）
- 整合 jiner 个人管家模块（仅保留财务管理）
- 保留并增强 claw-dash 监控面板（Gateway/Agent/会话/Tokens）

### 新增能力

- 一键接入 OpenClaw（页面按钮触发安装/配置）
- 统一用户认证与权限管理
- 插件化模块系统（支持动态加载 jiner 模块）
- 财务管理（收支记录、月度报表、预算管理）

## Capabilities

### New Capabilities

- `task-queue-core`: 任务队列核心引擎（Spring Boot 重写）
- `privy-modules`: 个人管家模块（财务：收支记录/月度报表/预算管理 + 健康：喝水记录/运动记录）
- `openclaw-integration`: OpenClaw 一键接入与集成
- `unified-dashboard`: 统一监控面板（重构自现有 dash）
- `user-auth`: 统一用户认证与权限管理

### Modified Capabilities

- `frontend-layout`: 基于 vue-pure-admin 的全新前端布局（现有前端完全重构）

## Impact

### 代码影响

- 新增 `backend/` 目录（Spring Boot 项目）
- 重构 `frontend/` 目录（基于 vue-pure-admin）
- 新增 `docker/` 目录（Docker Compose 配置）
- 新增 `mysql/` 目录（数据库初始化脚本）

### 依赖影响

- 移除：Express, better-sqlite3, Element Plus（旧版）
- 新增：Spring Boot, MySQL, Redis, vue-pure-admin, Pinia, TailwindCSS

### 系统影响

- 部署方式从 npm 脚本变为 Docker Compose
- 配置文件从 .env 变为 application.yml + docker-compose.yml
