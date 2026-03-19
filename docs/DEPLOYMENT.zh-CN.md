# ClawDash 部署指南

> [English](./DEPLOYMENT.md) | 中文

## 环境要求

- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Docker & Docker Compose

## 快速部署

### 1. Docker Compose 一键部署

```bash
# 克隆项目
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash

# 启动所有服务
docker compose up -d
```

### 2. 访问服务

- 前端: http://localhost:5177
- 后端 API: http://localhost:3001

## 手动部署

### 前端部署

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

### 后端部署

```bash
cd server

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产模式
npm run start
```

## 环境变量

| 变量        | 说明       | 默认值    |
| ----------- | ---------- | --------- |
| DB_HOST     | MySQL 地址 | localhost |
| DB_PORT     | MySQL 端口 | 3306      |
| DB_DATABASE | 数据库名   | clawdash  |
| DB_USER     | 数据库用户 | root      |
| DB_PASSWORD | 数据库密码 | root123   |
| REDIS_HOST  | Redis 地址 | localhost |
| REDIS_PORT  | Redis 端口 | 6379      |

## Docker 服务说明

| 服务     | 端口 | 说明             |
| -------- | ---- | ---------------- |
| mysql    | 3306 | MySQL 数据库     |
| redis    | 6379 | Redis 缓存       |
| backend  | 3001 | Node.js 后端     |
| frontend | 5177 | Vue 前端 (Nginx) |

## 验证部署

```bash
# 检查容器状态
docker compose ps

# 查看日志
docker compose logs -f

# 健康检查
curl http://localhost:3001/api/health
```
