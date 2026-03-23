## Why

当前总览页面布局简单，仅展示4个基础统计卡片和一个Agent列表，未能充分利用已有的组件（如GatewayStatusCard、ResourceChart、UptimeCard等）。需要重新设计为现代化的Dashboard网格布局，提供更丰富的数据可视化、实时状态监控和更好的用户体验。

## What Changes

### 页面重构
- 将现有 `overview/index.vue` 重构为现代 Dashboard 网格布局
- 添加系统状态横幅（Gateway运行状态 + 最后更新时间）
- 实现3行卡片网格：KPI指标 → 图表+系统信息 → 最近任务+活跃Agent

### 新增组件
- **TaskDistributionChart**: 任务状态分布环形图（待处理/运行中/已完成/失败）
- **RecentTasksList**: 最近任务列表，支持状态颜色和快速查看
- **ActiveAgentsPanel**: 活跃Agent状态面板，带在线/离线指示器

### 增强现有组件
- **StatCard**: 添加颜色变体（pink/blue/green/orange/purple）和趋势指示器支持
- **ResourceChart**: 修复为显示真实数据（CPU/内存），添加刷新功能
- **GatewayStatusCard**: 改进显示，与顶部横幅保持一致

### API整合
- 利用现有 `/api/dashboard/overview` 获取核心指标
- 利用现有 `/api/tasks/stats` 获取任务分布
- 资源数据暂用模拟数据（`ResourceChart`已有基础结构）

## Capabilities

### New Capabilities

- `overview-dashboard`: 总览页面Dashboard布局 - 主页面重新设计，包含网格布局、KPI卡片、图表和动态数据列表
- `task-distribution-chart`: 任务分布图表 - 环形图展示各状态任务数量和占比
- `recent-tasks-list`: 最近任务列表 - 展示最近5-10条任务，支持状态筛选和快速操作
- `active-agents-panel`: 活跃Agent面板 - 展示Agent在线状态，支持快速跳转

### Modified Capabilities

- `stat-card`: StatCard组件增强 - 添加更多颜色变体和趋势箭头支持

## Impact

### 受影响代码
- `frontend/src/views/overview/index.vue` - 重构主页面
- `frontend/src/components/overview/*.vue` - 新增/增强组件
- `frontend/src/api/` - 确认API调用（已存在）
- `frontend/src/style.css` - 全局样式（已配置紫色主题）

### 依赖
- Element Plus 组件库（已使用）
- Vue 3 Composition API（已使用）
- Chart库（如需要环形图，可考虑 Chart.js 或纯CSS实现）

### 无Breaking Changes
纯前端重构，不影响后端API和数据库
