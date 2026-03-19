## Overview

MySQL 数据库后端集成，替代现有的 SQLite。

## Specification

### Database Schema

- `agents` 表: Agent 元数据
- `agent_relationships` 表: Agent 关系
- `token_usage` 表: Token 消耗记录
- `transaction_log` 表: 操作日志
- `model_pricing` 表: 模型定价

### Environment Variables

- `DB_HOST`: MySQL 主机 (默认: mysql)
- `DB_PORT`: MySQL 端口 (默认: 3306)
- `DB_USER`: 数据库用户 (默认: root)
- `DB_PASSWORD`: 数据库密码
- `DB_NAME`: 数据库名 (默认: agentforge)

### Client

- 使用 mysql2 npm 包
- 支持连接池
- 自动重连

## Acceptance Criteria

1. 服务启动时自动创建数据库和表
2. 所有现有 API 正常工作
3. 数据持久化到 MySQL
