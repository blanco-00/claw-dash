<!--suppress HtmlUnknownTarget -->
<p align="center">
  <img width="200" src="https://user-images.githubusercontent.com/6128107/234207326-30d5a5b4-1a1a-4a8c-8c5f-8c5f8c5f8c5f.png" alt="ClawDash Logo">
</p>

<h1 align="center">ClawDash</h1>

<p align="center">
  <a href="https://github.com/blanco-00/claw-dash">
    <img src="https://img.shields.io/github/stars/blanco-00/claw-dash?style=flat" alt="GitHub Stars">
  </a>
  <a href="https://gitee.com/232911373/claw-dash">
    <img src="https://img.shields.io/badge/Gitee-镜像-brightgreen" alt="Gitee">
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

**ClawDash** 是一款面向 [OpenClaw](https://github.com/your-repo/openclaw) 的可视化配置工具。通过图形界面设计、管理多 Agent 拓扑，无需直接编辑 JSON 或 Markdown 配置文件。

## 为什么用 ClawDash？

管理多个 OpenClaw Agent 时，通常需要手动编辑 `AGENTS.md` 来配置 Agent 间的通信方式。ClawDash 将这一过程可视化：

| 不用 ClawDash | 用 ClawDash |
|---------------|-------------|
| 手动编辑 JSON/Markdown | 拖拽式拓扑编辑器 |
| 同步前无法预览 | 同步前预览差异（old vs new） |
| `AGENTS.md` 混乱难维护 | 自动生成清晰的结构化块 |
| Agent 关系不透明 | 一目了然的完整拓扑图 |

## 功能

### ✅ 已实现

| 功能 | 说明 |
|------|------|
| 🤖 **Agent 图谱编辑器** | 可视化拓扑编辑器 — 添加节点、拖拽连线、查看完整 Agent 图谱 |
| 🔗 **边路由配置** | 配置 task → reply → error 三层路由，双栏编辑器 |
| 🔄 **同步预览** | 同步前左右对比差异（old vs new）再写入 `AGENTS.md` |
| 📝 **边类型管理** | 自定义边类型定义与路由规则 |
| 🛠️ **OpenClaw 集成** | 一键安装、启动/停止、状态监控 |
| 📊 **系统概览** | Gateway 状态、Agent 数量、任务统计 |
| 📋 **任务队列** | 异步任务管理（优先级、重试） |
| 🐳 **Docker 监控** | 容器状态与资源统计 |

### 🚧 规划中

| 功能 | 状态 | 说明 |
|------|------|------|
| 🔗 **任务组** | 代码已有 | 批量任务管理与依赖链（菜单未暴露） |
| 💬 **会话监控** | 代码已有 | 活跃会话追踪（菜单未暴露） |
| ⏰ **定时任务** | 代码已有 | Cron 任务管理（菜单未暴露） |
| 📝 **消息模板库** | 进行中 | Agent 间通信的预置模板 |
| 🎨 **可视化工作流编排** | 计划中 | 拖拽式工作流编排器 |

## 截图

> Agent 拓扑图 — 拖拽节点、连接边、配置路由

```
┌─────────────────────────────────────────────┐
│  Config Graph                    [Fit][Sync]│
│  ┌──────┐     ┌──────┐     ┌──────┐      │
│  │ main │────▶│shang │────▶│ gong │      │
│  └──────┘     │shush │     │  bu  │      │
│               └──────┘     └──────┘      │
│                  │                          │
│                  ▼                          │
│               ┌──────┐                      │
│               │ menx │                      │
│               │ iash │                      │
│               └──────┘                      │
└─────────────────────────────────────────────┘
```

> 边配置 — 左侧配置任务消息，右侧配置回复/错误路由

```
┌─────────────────────────────────────────────┐
│ 边: main → menxiasheng              [保存]  │
├─────────────────────┬─────────────────────┤
│ 源配置 (main)        │ 目标配置 (menxiasheng)│
│ ─────────────────── │ ─────────────────── │
│ Task 消息模板:        │ ☑ 启用抄送           │
│ ┌─────────────────┐  │   抄送给: [尚书省]  │
│ │ {original_msg}  │  │ 回复模板:           │
│ └─────────────────┘  │ ┌─────────────────┐│
│                     │ │ 任务完成: {...}  ││
│ [✓] 启用此路由       │ └─────────────────┘│
│                     │ ☑ 启用错误处理       │
│                     │   处理人: [main]    │
└─────────────────────┴─────────────────────┘
```

## 快速开始

### 一键启动（生产环境）

```bash
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash
docker compose -f docker/docker-compose.yml up -d

# 访问
# 前端: http://localhost:5177
# 后端 API: http://localhost:5178
```

### 本地开发

```bash
# 只启动数据库服务
docker compose -f docker-compose.dev.yml up -d

# 启动后端 (Spring Boot)
cd backend && mvn spring-boot:run

# 启动前端 (Vue)
cd frontend && npm install && npm run dev

# 访问
# 前端: http://localhost:5177
# 后端: http://localhost:5178
```

## 架构

```
ClawDash
├── 前端 (Vue 3)
│   ├── Agent 图谱编辑器    — 可视化拓扑
│   ├── 边配置面板          — task/reply/error 路由
│   ├── 同步预览对话框       — old vs new 差异对比
│   └── 系统概览             — 监控仪表盘
│
├── 后端 (Spring Boot)
│   ├── ConfigGraph API     — 节点/边 CRUD
│   ├── AgentsMdSyncService  — 同步到 AGENTS.md
│   ├── OpenClaw 集成       — 安装/管理 Agent
│   └── 任务队列             — 异步任务管理
│
└── OpenClaw (托管的 Agent 运行时)
    └── AGENTS.md           — 配置文件（自动管理）
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + TypeScript + Vite + Pinia |
| UI | Element Plus + ECharts |
| 后端 | Java 17 + Spring Boot + MyBatis-Plus |
| 数据库 | MySQL 8.0 + Redis |
| 部署 | Docker Compose |

## 文档

- [用户指南](./docs/USER_GUIDE.zh-CN.md)
- [部署指南](./docs/DEPLOYMENT.zh-CN.md)
- [MCP 集成](./docs/MCP_INTEGRATION.md)
- [更新日志](./CHANGELOG.md)

## 贡献

欢迎贡献！请先阅读[贡献指南](./CONTRIBUTING.md)。

## 许可证

MIT 许可证 - 详见 [LICENSE](./LICENSE)。

---

<p align="center">
  ❤️ 由 <a href="https://github.com/blanco-00">Blanco</a> 制作
</p>

<p align="center">
  <sub>ClawDash - 面向 OpenClaw 的可视化 Agent 管理工具</sub>
</p>
