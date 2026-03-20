# OpenClaw 一键对接设计

> 日期：2026-03-20
> 目标：用户点击按钮，自动完成 OpenClaw 对接

## 背景

当前 ClawDash 的 OpenClaw 对接需要用户手动配置：
- 手动输入 OpenClaw API 地址
- 手动配置端口、token 等

用户希望：**点击按钮 → 自动检测 + 显示配置 → 确认 → 完成**

## 安全原则

**验证真实运行状态**：不直接读取配置文件，必须通过 OpenClaw API 验证其真实运行状态。

---

## 架构设计

### 数据流

```
用户点击"一键对接"
  │
  ├─▶ 后端调用 OpenClaw API (/health)
  │      │
  │      ├─▶ 成功 → 获取 plugins 配置
  │      │         → 返回给前端显示
  │      │
  │      └─▶ 失败 → 返回错误，前端提示
  │
  ├─▶ 前端显示检测结果（port、plugins 列表）
  │
  ├─▶ 用户点击"确认对接"
  │
  └─▶ 后端保存配置到数据库
```

### 配置项映射

| OpenClaw 配置 | ClawDash 用途 |
|--------------|---------------|
| `gateway.port` | API 地址端口 |
| `gateway.auth.token` | 认证 token |
| `plugins.entries` | 插件列表及状态 |
| `plugins.installs` | 已安装插件 |

---

## API 设计

### 1. 自动检测接口

**请求**
```
POST /api/openclaw/auto-detect
```

**响应（成功）**
```json
{
  "success": true,
  "data": {
    "running": true,
    "apiUrl": "http://localhost:18789",
    "plugins": {
      "task-queue": { "enabled": true, "config": {...} },
      "qqbot": { "enabled": true },
      "privy-jiner": { "enabled": true }
    },
    "workspaces": ["bingbu", "xingbu", "gongbu", ...]
  }
}
```

**响应（失败）**
```json
{
  "success": false,
  "error": "OPENCLAW_NOT_RUNNING",
  "message": "请先启动 OpenClaw"
}
```

### 2. 确认对接接口

**请求**
```
POST /api/openclaw/confirm-connect
Content-Type: application/json

{
  "apiUrl": "http://localhost:18789",
  "token": "xxx"
}
```

---

## 前端设计

### 当前页面
`/frontend/src/views/OpenClaw.vue`

### 改造点

1. **"一键安装" 按钮** → 改为 **"一键对接"**
2. **点击后行为**：
   - 调用 `/api/openclaw/auto-detect`
   - 显示检测结果弹窗
   - 用户确认后调用 `/api/openclaw/confirm-connect`

### 弹窗设计

```
┌─────────────────────────────────────┐
│  OpenClaw 自动检测结果               │
├─────────────────────────────────────┤
│  ✅ OpenClaw 运行中                 │
│                                     │
│  API 地址: http://localhost:18789   │
│                                     │
│  已启用插件:                         │
│  - task-queue ✅                     │
│  - qqbot ✅                          │
│  - privy-jiner ✅                    │
│                                     │
│  工作空间: bingbu, xingbu, gongbu   │
│                                     │
├─────────────────────────────────────┤
│  [取消]              [确认对接]       │
└─────────────────────────────────────┘
```

---

## 后端实现

### 文件

| 文件 | 改动 |
|------|------|
| `OpenClawService.java` | 新增 `autoDetect()`, `confirmConnect()` 方法 |
| `OpenClawController.java` | 新增 `/auto-detect`, `/confirm-connect` 接口 |

### 配置读取

从 `~/.openclaw/openclaw.json` 读取：
- `gateway.port` → API 端口
- `gateway.auth.token` → Token
- `plugins.entries` → 插件配置
- `workspaces` → 工作空间列表

### 验证流程

1. 读取配置文件获取候选 port
2. 调用 `http://localhost:{port}/health` 验证运行状态
3. 成功 → 返回配置；失败 → 返回错误

---

## 实现步骤

### Step 1: 后端 - 自动检测

- 新增 `autoDetect()` 方法
- 读取配置文件 + 验证 API

### Step 2: 后端 - 确认对接

- 新增 `confirmConnect()` 方法
- 保存配置到数据库

### Step 3: 前端 - 改造按钮

- 调用 auto-detect 接口
- 显示检测结果弹窗
- 调用 confirm-connect 接口

### Step 4: 测试

- 验证 OpenClaw 运行时能正确检测
- 验证 OpenClaw 未运行时提示正确

---

## 注意事项

1. **安全**：不直接在 API 响应中返回 token 给前端
2. **容错**：配置文件不存在、端口不对等情况要有友好提示
3. **兼容**：支持默认端口 18789，也支持自定义端口
