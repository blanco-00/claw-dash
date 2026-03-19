<!--suppress HtmlUnknownTarget -->
<p align="center">
  <img width="200" src="https://user-images.githubusercontent.com/6128107/234207326-30d5a5b4-1a1a-4a8c-8c5f-8c5f8c5f8c5f.png" alt="ClawDash Logo">
</p>

<h1 align="center">ClawDash</h1>

<p align="center">
  <a href="https://github.com/blanco-00/claw-dash">
    <img src="https://img.shields.io/github/stars/blanco-00/claw-dash?style=flat" alt="GitHub Stars">
  </a>
  <a href="https://github.com/blanco-00/claw-dash/releases">
    <img src="https://img.shields.io/github/v/release/blanco-00/claw-dash" alt="GitHub Release">
  </a>
  <a href="https://github.com/blanco-00/claw-dash/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/blanco-00/claw-dash" alt="License">
  </a>
  <a href="https://docker-compose.com">
    <img src="https://img.shields.io/badge/Docker-Compose-Ready-blue" alt="Docker Compose">
  </a>
</p>

<p align="center">
  <strong>English</strong> | <a href="README.zh-CN.md">中文</a>
</p>

---

ClawDash is a visual multi-Agent management system built on OpenClaw, featuring a Chinese dynasty-themed (Kingdom of Women) UI design.

## ✨ Features / 功能特性

| Feature                 | Description                                            | 说明                             |
| ----------------------- | ------------------------------------------------------ | -------------------------------- |
| 📊 Dashboard            | Gateway status, system resources, real-time task stats | 网关状态、系统资源、实时任务统计 |
| 👩‍💼 Agent Management     | Visual agent list with org chart                       | 可视化Agent列表与组织架构图      |
| ⏰ Cron Tasks           | Scheduled task monitoring & management                 | 定时任务监控与管理               |
| 📋 Task Queue           | Async task management                                  | 异步任务管理                     |
| 🔗 Task Groups          | Batch task management with dependencies                | 批量任务管理与依赖配置           |
| 💬 Sessions             | Active session monitoring                              | 活跃会话监控                     |
| 💰 Token Stats          | Token consumption & cost analysis                      | Token消耗与成本分析              |
| 🔌 OpenClaw Integration | One-click install, status monitoring                   | 一键安装、状态监控               |
| 🐳 Docker Monitor       | Container status & resource stats                      | 容器状态与资源统计               |
| 💵 Finance              | Income/expense tracking & reports                      | 收支记录与报表                   |

## 🚀 Quick Start / 快速开始

### Mode 1: One-Click Start (Production / 一键启动)

```bash
# Clone / 克隆
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash

# Start all services (full containerized) / 启动所有服务
docker compose -f docker/docker-compose.yml up -d

# Access / 访问
# Frontend: http://localhost:5177
# Backend API: http://localhost:5178
```

### Mode 2: Local Development / 本地开发模式

```bash
# 1. Start database services only / 只启动数据库服务
docker compose -f docker-compose.dev.yml up -d

# 2. Start backend (Spring Boot) / 启动后端
cd backend
# Import as Maven project in IDEA/Eclipse, or run:
mvn spring-boot:run

# 3. Start frontend (Vue) / 启动前端
cd frontend
npm install
npm run dev

# Access / 访问
# Frontend: http://localhost:5173
# Backend: http://localhost:5178
```

### Production Build / 生产构建

```bash
# Build / 构建
npm run build

# Preview / 预览
npm run preview
```

## 🐳 Docker Services / Docker 服务

| Service  | Port  | Description         |
| -------- | ----- | ------------------ |
| mysql    | 3306  | MySQL 8.0 Database |
| redis    | 6379  | Redis 7.0 Cache    |
| backend  | 5178  | Spring Boot API    |
| frontend | 5177  | Vue3 Web UI        |

### Docker Commands / Docker 命令

```bash
# Production mode (full containers) / 生产模式
docker compose -f docker/docker-compose.yml up -d
docker compose -f docker/docker-compose.yml logs -f
docker compose -f docker/docker-compose.yml down

# Development mode (DB only) / 开发模式
docker compose -f docker-compose.dev.yml up -d
docker compose -f docker-compose.dev.yml logs -f
docker compose -f docker-compose.dev.yml down

> **Important / 重要提示**: Do NOT run both modes at the same time. Stop one mode before starting the other.
> 两种模式不能同时使用，切换前请先停止当前模式。
```

## ⚙️ Environment Variables / 环境变量

| Variable            | Description          | Default               |
| ------------------- | -------------------- | --------------------- |
| `VITE_OPENCLAW_API` | OpenClaw API Address | http://localhost:3000 |
| `VITE_APP_TITLE`    | Application Title    | ClawDash              |
| `VITE_DEV_PORT`     | Dev Server Port      | 5177                  |
| `DB_HOST`           | MySQL Host (Docker)  | mysql                 |
| `DB_PORT`           | MySQL Port           | 3306                  |
| `DB_USER`           | MySQL User           | root                  |
| `DB_PASSWORD`       | MySQL Password       | root123               |
| `DB_NAME`           | Database Name        | clawdash              |

## 🛠 Tech Stack / 技术栈

| Layer      | Technology                    |
| ---------- | ----------------------------- |
| Frontend   | Vue 3 + TypeScript + Vite     |
| UI Library | Element Plus + ECharts       |
| State      | Pinia                         |
| Backend    | Java 17 + Spring Boot         |
| Database   | MySQL 8.0                    |
| Cache      | Redis 7.0                     |
| Deployment | Docker Compose                |

## 📖 Documentation / 文档

- [User Guide / 用户指南](./docs/USER_GUIDE.md)
- [Deployment Guide / 部署指南](./docs/DEPLOYMENT.md)
- [Changelog](./CHANGELOG.md)

## 🤝 Contributing / 贡献

Contributions are welcome! Please read our [Contributing Guidelines](./CONTRIBUTING.md) first.

欢迎贡献！请先阅读[贡献指南](./CONTRIBUTING.md)。

## 📄 License / 许可证

MIT License - see [LICENSE](./LICENSE) for details.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/blanco-00">Blanco</a>
</p>

<p align="center">
  <sub>ClawDash - Simplifying Agent Management / 让Agent管理更简单</sub>
</p>
