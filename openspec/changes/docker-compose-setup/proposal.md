## Why

当前项目使用 SQLite 本地数据库，无法在多实例部署时共享数据，且数据持久化依赖宿主机路径。通过 Docker Compose 一键启动可以简化部署流程，支持 MySQL 和 Redis，满足生产环境需求。

## What Changes

1. **创建 Docker Compose 配置** - 定义 MySQL、Redis、Backend、Frontend 服务
2. **创建 Backend Dockerfile** - Node.js 应用的容器化配置
3. **创建 Frontend Dockerfile** - Vue3 应用的 Nginx 部署配置
4. **数据库迁移** - 从 SQLite 切换到 MySQL，更新数据访问层
5. **添加 Redis 支持** - 用于缓存和会话存储
6. **创建初始化脚本** - MySQL 数据库和表的自动初始化

## Capabilities

### New Capabilities

- `docker-compose`: Docker Compose 一键启动所有服务
- `mysql-backend`: MySQL 数据库后端集成
- `redis-cache`: Redis 缓存层集成

### Modified Capabilities

- (无 - 初始功能集)

## Impact

- 新增 `docker-compose.yml` - 主编排文件
- 新增 `backend/Dockerfile` - 后端容器配置
- 新增 `frontend/Dockerfile` - 前端容器配置
- 新增 `docker/mysql/init.sql` - MySQL 初始化脚本
- 修改 `server/index.ts` - 从 SQLite 迁移到 MySQL + Redis
- 修改 `package.json` - 添加 MySQL/Redis 客户端依赖
