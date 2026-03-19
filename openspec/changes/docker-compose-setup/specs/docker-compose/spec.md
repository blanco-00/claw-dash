## Overview

Docker Compose 一键启动所有服务，包括 MySQL、Redis、Backend、Frontend。

## Specification

### Services

- **mysql**: MySQL 8.0 数据库
- **redis**: Redis 7 缓存
- **backend**: Node.js Express API 服务
- **frontend**: Nginx 静态文件服务

### Configuration

- 端口: Frontend 80, Backend 3001, MySQL 3306, Redis 6379
- 环境变量通过 .env 文件或 docker-compose 参数传入
- 数据卷持久化 MySQL 和 Redis 数据

### Dependencies

- docker-compose >= 3.8
- Docker Engine >= 20.10

## Acceptance Criteria

1. `docker-compose up` 能成功启动所有服务
2. Frontend 访问 http://localhost:80 能看到界面
3. Backend API 能连接 MySQL 和 Redis
4. 服务重启后数据持久化
