## Context

当前项目使用 SQLite 本地数据库 (better-sqlite3)，数据存储在 `~/.openclaw/agentforge.db`。这种架构的局限：

- 多实例部署时无法共享数据
- 数据持久化依赖宿主机特定路径
- 无法使用 Redis 做缓存和会话管理

## Goals / Non-Goals

**Goals:**

- 通过 Docker Compose 一键启动所有服务
- MySQL 替代 SQLite 作为主数据库
- Redis 用于缓存和可选的会话存储
- 前端使用 Nginx 部署

**Non-Goals:**

- 不修改 OpenClaw 本身（只集成其 CLI）
- 不改变现有的 API 接口（兼容现有前端）
- 不支持数据库集群（单实例 MySQL）

## Decisions

### 1. 使用 MySQL 而非 PostgreSQL

- 现有 docker-compose.yml 模板使用 MySQL，保持一致
- MySQL 对本项目复杂度足够

### 2. 服务编排使用 docker-compose

- 适合单主机部署
- 维护简单，生态成熟

### 3. Backend 使用多阶段构建

- 减少最终镜像体积
- Node.js 18 LTS

### 4. Frontend 使用 Nginx Alpine

- 镜像小巧
- 静态资源 gzip 压缩

## Migration Plan

1. 创建 `backend/Dockerfile`
2. 创建 `frontend/Dockerfile`
3. 创建 `docker/mysql/init.sql` 初始化脚本
4. 更新 `docker-compose.yml`
5. 修改 `server/index.ts` 支持 MySQL 环境变量
6. 添加 Redis 连接代码（可选）
7. 测试 `docker-compose up` 一键启动

## Risks / Trade-offs

- [风险] MySQL 数据迁移 → 提供初始化脚本，历史数据需手动迁移
- [风险] OpenClaw CLI 在容器内运行 → 挂载宿主机 `.openclaw` 目录
- [权衡] 容器化后调试不便 → 提供 `docker-compose exec` 命令
