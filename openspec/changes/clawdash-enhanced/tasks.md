# ClawDash 增强版 - Implementation Tasks

## 1. 基础设施搭建

- [x] 1.1 创建 Spring Boot 后端项目骨架 (pom.xml, application.yml)
- [x] 1.2 搭建 vue-pure-admin 前端项目
- [x] 1.3 编写 docker-compose.yml (MySQL + Redis + Backend + Frontend)
- [x] 1.4 创建 MySQL 数据库初始化脚本 (init.sql)
- [x] 1.5 配置 Nginx 反向代理
- [x] 1.6 配置 Docker 环境变量 (AUTH_ENABLED, JWT_SECRET)
- [x] 1.7 实现本地/生产模式自动切换

## 2. 安全模块 (Security)

- [x] 2.1 集成 Spring Security 框架
- [x] 2.2 实现 BCryptPasswordEncoder 密码加密
- [x] 2.3 实现 JWT Token 生成/验证 (JwtTokenProvider)
- [x] 2.4 实现 JwtAuthenticationFilter
- [x] 2.5 配置 CORS 跨域规则
- [x] 2.6 实现接口限流 (RateLimiter)
- [x] 2.7 敏感数据脱敏处理 (日志/响应)
- [x] 2.8 配置 HTTPS 安全头

## 3. 用户认证模块 (User Auth)

- [x] 3.1 实现用户注册 API (POST /api/auth/register)
- [x] 3.2 实现用户登录 API (POST /api/auth/login)
- [x] 3.3 实现 Session 管理 (Redis)
- [x] 3.4 实现密码修改 API (POST /api/auth/password)
- [x] 3.5 前端登录页面集成
- [x] 3.6 前端注册页面集成
- [x] 3.7 前端 Token 存储与自动携带

## 4. 任务队列核心 (Task Queue Core)

- [x] 4.1 设计并创建 MySQL tasks 表
- [x] 4.2 实现任务创建 API (POST /api/tasks)
- [x] 4.3 实现任务原子Claim机制 (防止并发重复)
- [x] 4.4 实现任务完成/失败 API (PATCH /api/tasks/:id)
- [x] 4.7 实现任务统计 API (GET /api/tasks/stats)
- [x] 3.8 设计并创建 task_groups 表
- [x] 3.9 实现任务组管理 API (CRUD)
- [x] 3.10 设计并创建 cron_jobs 表
- [x] 3.11 实现 Cron 调度器
- [x] 3.12 前端任务列表页面
- [x] 3.13 前端任务组页面
- [x] 3.14 前端 Cron 任务页面

## 5. 统一监控面板 (Unified Dashboard)

- [x] 4.1 实现系统概览 API (GET /api/dashboard/overview)
- [x] 4.2 实现网关状态 API (GET /api/dashboard/gateway)
- [x] 4.3 实现会话列表 API (GET /api/dashboard/sessions)
- [x] 4.4 实现结束会话 API (DELETE /api/dashboard/sessions/:id)
- [x] 4.5 实现 Token 管理 API (GET/POST/DELETE /api/tokens)
- [x] 4.6 前端概览页面 (Overview)
- [x] 4.7 前端网关状态页面
- [x] 4.8 前端会话管理页面
- [x] 4.9 前端 Token 管理页面

## 6. Agent 管理

- [x] 5.1 设计并创建 agents 表
- [x] 5.2 实现 Agent 列表 API (GET /api/agents)
- [x] 5.3 实现 Agent 详情 API (GET /api/agents/:id)
- [x] 5.4 实现 Agent 配置更新 API (PATCH /api/agents/:id/config)

## 6. 个人管家模块 (Privy Modules)

### 6.1 财务管理

- [x] 6.1.1 设计并创建 privy_finance 表
- [x] 6.1.2 实现收支记录 API (CRUD)
- [x] 6.1.3 实现月度报表 API
- [x] 6.1.4 实现预算管理 API
- [x] 6.1.5 前端财务管理页面

### 6.2 ~~健康管理~~ (不做 - 简单数据无需数据库)

### 6.3 ~~知识库~~ (不做 - OpenClaw Skill 实现)

### 6.4 ~~新闻聚合~~ (不做 - OpenClaw 已实现)

### 6.5 ~~AI 对话~~ (不做 - OpenClaw 原生支持)

## 7. OpenClaw 集成

- [x] 7.1 设计并创建 openclaw_config 表
- [x] 7.2 实现一键安装 API (POST /api/openclaw/install)
- [x] 7.3 实现状态检测 API (GET /api/openclaw/status)
- [x] 7.4 实现 Agent 自动创建 API
- [x] 7.5 实现插件管理 API (启用/禁用)
- [x] 7.6 前端 OpenClaw 接入页面 (一键安装按钮)
- [x] 7.7 前端 OpenClaw 状态面板
- [x] 7.8 前端 Docker 状态页面

## 11. 测试与部署

- [x] 11.1 编写后端单元测试 (JUnit)
- [x] 11.2 编写后端集成测试
- [x] 11.3 前端组件测试
- [x] 11.4 E2E 测试用例
- [x] 11.5 Docker 镜像构建优化
- [x] 11.6 CI/CD 流程配置
- [x] 11.7 生产环境部署文档
- [x] 11.8 用户使用文档

## 12. 优化与完善

- [x] 12.1 性能优化 (缓存、索引)
- [x] 12.2 安全加固 (定期更换密钥)
- [x] 12.3 日志完善 (结构化日志配置)
- [x] 12.4 错误处理统一
- [x] 12.5 API 文档生成 (Swagger/OpenAPI)
