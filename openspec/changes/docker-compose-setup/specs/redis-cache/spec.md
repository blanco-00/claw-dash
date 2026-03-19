## Overview

Redis 缓存层集成，用于缓存和会话存储。

## Specification

### Use Cases

- API 响应缓存
- 会话数据存储 (可选)
- 实时状态缓存

### Environment Variables

- `REDIS_HOST`: Redis 主机 (默认: redis)
- `REDIS_PORT`: Redis 端口 (默认: 6379)
- `REDIS_PASSWORD`: Redis 密码 (可选)

### Client

- 使用 ioredis npm 包
- 支持连接池
- 自动重连

## Acceptance Criteria

1. Redis 连接成功
2. 可用于缓存 API 响应 (可选功能)
3. 服务重启后连接自动恢复
