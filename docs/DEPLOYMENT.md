# ClawDash 部署指南

## 环境要求

- JDK 17+
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
docker-compose up -d
```

### 2. 访问服务

- 前端: http://localhost:5177
- 后端 API: http://localhost:8080
- Swagger 文档: http://localhost:8080/swagger-ui.html

## 手动部署

### 后端部署

```bash
cd backend

# 构建
mvn clean package -DskipTests

# 运行
java -jar target/clawdash-backend-1.0.0.jar
```

### 前端部署

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

## 环境变量

| 变量           | 说明       | 默认值          |
| -------------- | ---------- | --------------- |
| MYSQL_HOST     | MySQL 地址 | localhost       |
| MYSQL_PORT     | MySQL 端口 | 3306            |
| MYSQL_DATABASE | 数据库名   | clawdash        |
| MYSQL_USER     | 数据库用户 | root            |
| MYSQL_PASSWORD | 数据库密码 | password        |
| REDIS_HOST     | Redis 地址 | localhost       |
| REDIS_PORT     | Redis 端口 | 6379            |
| JWT_SECRET     | JWT 密钥   | your-secret-key |
| AUTH_ENABLED   | 启用认证   | false           |

## Docker 服务说明

| 服务     | 端口 | 说明             |
| -------- | ---- | ---------------- |
| mysql    | 3306 | MySQL 数据库     |
| redis    | 6379 | Redis 缓存       |
| backend  | 8080 | Spring Boot 后端 |
| frontend | 5177 | Vue 前端         |
| nginx    | 80   | Nginx 反向代理   |

## 验证部署

```bash
# 检查容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 健康检查
curl http://localhost:8080/api/dashboard/overview
```
