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

### Docker Compose (Recommended / 推荐)

```bash
# Clone / 克隆
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash

# Start all services / 启动所有服务
docker compose up -d

# Access / 访问
# Frontend: http://localhost:5177
# Backend API: http://localhost:3001
```

### Development Mode / 开发模式

```bash
# Install dependencies / 安装依赖
npm install

# Start development servers / 启动开发服务器
npm run dev:all

# Or separately / 或分别启动
npm run dev      # Frontend: http://localhost:5177
npm run server   # Backend: http://localhost:3001
```

### Production Build / 生产构建

```bash
# Build / 构建
npm run build

# Preview / 预览
npm run preview
```

## 🐳 Docker Services / Docker 服务

| Service  | Port | Description         |
| -------- | ---- | ------------------- |
| mysql    | 3306 | MySQL 8.0 Database  |
| redis    | 6379 | Redis 7.0 Cache     |
| backend  | 3001 | Node.js API Server  |
| frontend | 5177 | Vue3 Web UI (Nginx) |

### Docker Commands / Docker 命令

```bash
# View logs / 查看日志
docker compose logs -f

# Stop services / 停止服务
docker compose down

# Rebuild / 重新构建
docker compose up -d --build
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

| Layer      | Technology                     |
| ---------- | ------------------------------ |
| Frontend   | Vue 3 + TypeScript + Vite      |
| UI Library | Element Plus + ECharts         |
| State      | Pinia                          |
| Backend    | Node.js + Express + TypeScript |
| Database   | MySQL 8.0                      |
| Cache      | Redis 7.0                      |
| Deployment | Docker Compose                 |

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
