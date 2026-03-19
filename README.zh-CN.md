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
  <strong>英文</strong> | <a href="README.md">English</a>
</p>

---

ClawDash 是一个基于 OpenClaw 的可视化多 Agent 管理系统，采用中国古代王朝（女儿国）风格设计。

## ✨ 功能特性

| 功能             | 描述                             |
| ---------------- | -------------------------------- |
| 📊 仪表盘        | 网关状态、系统资源、实时任务统计 |
| 👩‍💼 Agent 管理    | 可视化 Agent 列表与组织架构图    |
| ⏰ Cron 任务     | 定时任务监控与管理               |
| 📋 任务队列      | 异步任务管理                     |
| 🔗 任务组        | 批量任务管理与依赖配置           |
| 💬 会话管理      | 活跃会话监控                     |
| 💰 Token 统计    | Token 消耗与成本分析             |
| 🔌 OpenClaw 集成 | 一键安装、状态监控               |
| 🐳 Docker 监控   | 容器状态与资源统计               |
| 💵 财务管理      | 收支记录与报表                   |

## 🚀 快速开始

### Docker Compose（推荐）

```bash
# 克隆
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash

# 启动所有服务
docker compose up -d

# 访问
# 前端: http://localhost:5177
# 后端 API: http://localhost:3001
```

### 开发模式

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev:all

# 或分别启动
npm run dev      # 前端: http://localhost:5177
npm run server   # 后端: http://localhost:3001
```

### 生产构建

```bash
# 构建
npm run build

# 预览
npm run preview
```

## 🐳 Docker 服务

| 服务     | 端口 | 说明                |
| -------- | ---- | ------------------- |
| mysql    | 3306 | MySQL 8.0 数据库    |
| redis    | 6379 | Redis 7.0 缓存      |
| backend  | 3001 | Node.js API 服务器  |
| frontend | 5177 | Vue3 Web UI (Nginx) |

### Docker 命令

```bash
# 查看日志
docker compose logs -f

# 停止服务
docker compose down

# 重新构建
docker compose up -d --build
```

## ⚙️ 环境变量

| 变量                | 描述                | 默认值                |
| ------------------- | ------------------- | --------------------- |
| `VITE_OPENCLAW_API` | OpenClaw API 地址   | http://localhost:3000 |
| `VITE_APP_TITLE`    | 应用标题            | ClawDash              |
| `VITE_DEV_PORT`     | 开发服务器端口      | 5177                  |
| `DB_HOST`           | MySQL 主机 (Docker) | mysql                 |
| `DB_PORT`           | MySQL 端口          | 3306                  |
| `DB_USER`           | MySQL 用户          | root                  |
| `DB_PASSWORD`       | MySQL 密码          | root123               |
| `DB_NAME`           | 数据库名称          | clawdash              |

## 🛠 技术栈

| 层级     | 技术                           |
| -------- | ------------------------------ |
| 前端     | Vue 3 + TypeScript + Vite      |
| UI 库    | Element Plus + ECharts         |
| 状态管理 | Pinia                          |
| 后端     | Node.js + Express + TypeScript |
| 数据库   | MySQL 8.0                      |
| 缓存     | Redis 7.0                      |
| 部署     | Docker Compose                 |

## 📖 文档

- [用户指南](./docs/USER_GUIDE.md)
- [部署指南](./docs/DEPLOYMENT.md)
- [更新日志](./CHANGELOG.md)

## 🤝 贡献

欢迎贡献！请先阅读[贡献指南](./CONTRIBUTING.md)。

## 📄 许可证

MIT 许可证 - 详见 [LICENSE](./LICENSE)。

---

<p align="center">
  ❤️ 由 <a href="https://github.com/blanco-00">Blanco</a> 制作
</p>

<p align="center">
  <sub>ClawDash - 让 Agent 管理更简单</sub>
</p>
