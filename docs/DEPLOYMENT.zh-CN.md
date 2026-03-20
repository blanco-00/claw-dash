# ClawDash 部署指南

> [English](./DEPLOYMENT.md) | 中文

## 端口规范

| 服务   | 端口 |
|--------|------|
| 前端   | 5177 |
| 后端   | 5178 |

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+

## 开发模式

### 1. 启动数据库服务

```bash
# 方式一：Docker（推荐）
docker compose -f docker-compose.dev.yml up -d

# 方式二：本地安装 MySQL + Redis
# MySQL: localhost:3306, 用户 root, 密码 root123
# Redis: localhost:6379
```

### 2. 启动后端

```bash
cd backend

# IDEA/Eclipse 导入 Maven 项目直接运行
# 或命令行运行
mvn spring-boot:run

# 后端启动后: http://localhost:5178
```

### 3. 启动前端

```bash
cd frontend

npm install
npm run dev

# 前端启动后: http://localhost:5177
```

## 生产部署

### Docker Compose 一键部署

```bash
# 克隆项目
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash

# 启动所有服务
docker compose -f docker/docker-compose.yml up -d
```

### 访问服务

- 前端: http://localhost:5177
- 后端 API: http://localhost:5178
- Swagger 文档: http://localhost:5178/swagger-ui.html

## 环境变量

| 变量           | 说明       | 默认值          |
| -------------- | ---------- | --------------- |
| MYSQL_HOST     | MySQL 地址 | localhost       |
| MYSQL_PORT     | MySQL 端口 | 3306            |
| MYSQL_DATABASE | 数据库名   | clawdash        |
| MYSQL_USER     | 数据库用户 | root            |
| MYSQL_PASSWORD | 数据库密码 | root123         |
| REDIS_HOST     | Redis 地址 | localhost       |
| REDIS_PORT     | Redis 端口 | 6379            |
| JWT_SECRET     | JWT 密钥   | your-secret-key |
| AUTH_ENABLED   | 启用认证   | false           |

## Docker 服务说明

| 服务     | 端口 | 说明             |
| -------- | ---- | ---------------- |
| mysql    | 3306 | MySQL 数据库     |
| redis    | 6379 | Redis 缓存       |
| backend  | 5178 | Spring Boot 后端 |
| frontend | 5177 | Vue 前端         |

## 验证部署

```bash
# 检查容器状态
docker compose ps

# 查看日志
docker compose logs -f

# 健康检查
curl http://localhost:5178/api/gateway/status
```
