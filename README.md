# 的女儿国监控平台

基于OpenClaw的多Agent可视化管理系统，采用中国古代王朝（女儿国）风格。

## 快速开始

### 开发模式

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
# 构建
npm run build

# 预览
npm run preview
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| VITE_OPENCLAW_API | OpenClaw API地址 | http://localhost:3000 |
| VITE_APP_TITLE | 应用标题 | ClawDash - 女儿国监控系统 |
| VITE_DEV_PORT | 开发服务器端口 | 5173 |

## 功能模块

- 📊 系统概览 - Gateway状态、系统资源
- 👩‍💼 Agent管理 - 女儿国Agent列表与详情
- ⏰ Cron任务 - 定时任务监控
- 📋 任务队列 - 异步任务管理
- 🔗 任务组 - 任务组与依赖关系
- 💬 会话管理 - 会话统计

## 技术栈

- Vue3 + TypeScript
- Vite
- Element Plus
- Pinia
- ECharts

## 部署

### Docker

```bash
docker-compose up -d
```

### 手动部署

1. 构建: `npm run build`
2. 配置Nginx
3. 启动服务

## 女儿国体系

| 官职 | AgentID | 职责 |
|------|---------|------|
| 皇后 | main | 中书省决策 |
| 贵妃 | shangshusheng | 尚书省分发 |
| 嫔 | gongbu | 工部技术 |
| 丫鬟 | jishu | 技术研发 |

---
*女儿国监控系统 - 让技术更有温度* 💋
