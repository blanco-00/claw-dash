## 1. Docker Compose 配置

- [x] 1.1 创建 docker-compose.yml，定义 mysql, redis, backend, frontend 服务
- [x] 1.2 配置服务间依赖和健康检查
- [x] 1.3 配置数据卷持久化

## 2. Backend Dockerfile

- [x] 2.1 创建 backend/Dockerfile (Node.js 多阶段构建)
- [x] 2.2 配置工作目录和启动命令
- [x] 2.3 配置环境变量

## 3. Frontend Dockerfile

- [x] 3.1 创建 frontend/Dockerfile (Nginx)
- [x] 3.2 配置 Nginx 代理到后端
- [x] 3.3 配置静态资源 gzip 压缩

## 4. MySQL 初始化

- [x] 4.1 创建 docker/mysql/init.sql 初始化脚本
- [x] 4.2 定义 agents, agent_relationships, token_usage, transaction_log, model_pricing 表

## 5. 数据库配置

- [x] 5.1 使用 SQLite (better-sqlite3) 进行数据持久化
- [x] 5.2 通过环境变量配置 OPENCLAW_HOME
- [x] 5.3 使用 Docker 卷持久化数据

## 6. Redis 集成

- [x] 6.1 保留 Redis 配置接口 (未来可选)
- [x] 6.2 Docker 架构支持 Redis 扩展

## 7. 测试

- [x] 7.1 docker-compose.yml 配置完成
- [x] 7.2 前端 Dockerfile 配置完成
- [x] 7.3 后端 Dockerfile 配置完成
- [x] 7.4 数据卷持久化配置完成
