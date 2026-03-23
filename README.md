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
  <strong>English</strong> | <a href="README.zh-CN.md">中文</a>
</p>

---

**ClawDash** is a visual configuration tool for [OpenClaw](https://github.com/your-repo/openclaw). It provides a graphical interface to design, configure, and manage multi-agent topologies — without directly editing JSON or Markdown files.

## Why ClawDash?

When managing multiple OpenClaw agents, you typically edit `AGENTS.md` manually to configure how agents communicate. ClawDash replaces that with a visual editor:

| Without ClawDash | With ClawDash |
|-----------------|--------------|
| Edit JSON/MD by hand | Drag-and-drop topology editor |
| No preview before sync | Preview diff before syncing (old vs new) |
| Messy `AGENTS.md` | Clean structured blocks auto-generated |
| No visibility into agent relationships | See the full agent topology at a glance |

## Features

### ✅ Implemented

| Feature | Description |
|---------|-------------|
| 🤖 **Agent Graph Editor** | Visual topology editor — add nodes, drag to connect, see the full agent graph |
| 🔗 **Edge Routing Config** | Configure task → reply → error routing between agents with a two-panel editor |
| 🔄 **Sync Preview** | Side-by-side diff (old vs new) before syncing changes to `AGENTS.md` |
| 📝 **Edge Type Management** | Custom edge type definitions and routing rules |
| 🛠️ **OpenClaw Integration** | One-click agent install, start/stop, status monitoring |
| 📊 **System Overview** | Gateway status, agent count, task statistics |
| 📋 **Task Queue** | Async task management with priorities and retries |
| 🐳 **Docker Monitor** | Container status and resource stats |

### 🚧 Roadmap

| Feature | Status | Description |
|---------|--------|-------------|
| 🔗 **Task Groups** | Code exists | Batch task management with dependencies (not exposed in menu) |
| 💬 **Session Monitor** | Code exists | Active session tracking (not exposed in menu) |
| ⏰ **Cron Tasks** | Code exists | Scheduled task management (not exposed in menu) |
| 📝 **Message Template Library** | In Progress | Pre-built templates for agent communication |
| 🎨 **Visual Workflow Builder** | Planned | Drag-and-drop workflow orchestration |

## Screenshots

> Agent topology graph — drag nodes, connect edges, configure routes

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

> Edge config — left panel for task message, right panel for reply/error routing

```
┌─────────────────────────────────────────────┐
│ Edge: main → menxiasheng           [Save]    │
├─────────────────────┬─────────────────────┤
│ Source (main)       │ Target (menxiasheng)│
│ ─────────────────── │ ─────────────────── │
│ Task Message:        │ ☑ Enable Reply     │
│ ┌─────────────────┐  │   CC to: [shangshu]│
│ │ {original_msg}  │  │ Reply Template:    │
│ └─────────────────┘  │ ┌─────────────────┐│
│                       │ │ Task done: {..}││
│ [✓] Enable Route      │ └─────────────────┘│
│                       │ ☑ Enable Error     │
│                       │   Handler: [main]   │
└───────────────────────┴─────────────────────┘
```

## Quick Start

### Prerequisites

1. **OpenClaw** must be installed and running on your machine
   ```bash
   # Install OpenClaw if not already installed
   npm install -g openclaw
   
   # Start OpenClaw (must be running before starting ClawDash)
   openclaw start
   ```

2. **Docker** 20.10+ and **Docker Compose** v2+

3. **Ports** (ensure these are available):
   - 13306 (MySQL)
   - 16379 (Redis)
   - 5177 (Frontend)
   - 5178 (Backend)

### One-Click Start (Production)

```bash
git clone https://github.com/blanco-00/claw-dash.git
cd claw-dash
docker compose -f docker/docker-compose.yml up -d

# Wait ~60 seconds for services to start
# Then access: http://localhost:5177
```

### Accessing the Application

| Service | URL |
|---------|-----|
| Frontend (Web UI) | http://localhost:5177 |
| Backend API | http://localhost:5178 |
| MySQL | localhost:13306 (user: root, pass: root123) |
| Redis | localhost:16379 |

### OpenClaw Integration

ClawDash automatically connects to your local OpenClaw installation. Make sure:
- OpenClaw is running (`openclaw start`)
- The `~/.openclaw/openclaw.json` config file exists

### Updating

When the project adds new features, update and restart:

```bash
git pull
docker compose -f docker/docker-compose.yml down
docker compose -f docker/docker-compose.yml up -d
```

Flyway auto-migrates the database schema — your data is preserved.

### Local Development

```bash
# Start database services only
docker compose -f docker-compose.dev.yml up -d

# Start backend (Spring Boot)
cd backend && mvn spring-boot:run

# Start frontend (Vue)
cd frontend && npm install && npm run dev

# Access
# Frontend: http://localhost:5177
# Backend: http://localhost:5178
```

### China Users (阿里云用户)

The project uses Aliyun Maven mirror by default for faster downloads in China. International users can change `docker/backend/settings.xml` to use `central` instead.

## Architecture

```
ClawDash
├── Frontend (Vue 3)
│   ├── Agent Graph Editor     — visual topology
│   ├── Edge Config Panel       — task/reply/error routing
│   ├── Sync Preview Dialog     — old vs new diff
│   └── System Overview         — monitoring dashboard
│
├── Backend (Spring Boot)
│   ├── ConfigGraph API        — CRUD for nodes/edges
│   ├── AgentsMdSyncService    — sync to AGENTS.md
│   ├── OpenClaw Integration   — install/manage agents
│   └── Task Queue            — async task management
│
└── OpenClaw (Managed Agent Runtime)
    └── AGENTS.md              — configuration file (auto-managed)
```

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | Vue 3 + TypeScript + Vite + Pinia |
| UI | Element Plus + ECharts |
| Backend | Java 17 + Spring Boot + MyBatis-Plus |
| Database | MySQL 8.0 + Redis |
| Deployment | Docker Compose |

## Documentation

- [User Guide](./docs/USER_GUIDE.md)
- [Deployment Guide](./docs/DEPLOYMENT.md)
- [MCP Integration](./docs/MCP_INTEGRATION.md)
- [Changelog](./CHANGELOG.md)

## Contributing

Contributions are welcome! Please read our [Contributing Guidelines](./CONTRIBUTING.md) first.

## License

MIT License - see [LICENSE](./LICENSE) for details.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/blanco-00">Blanco</a>
</p>

<p align="center">
  <sub>ClawDash - Visual Agent Management for OpenClaw</sub>
</p>
