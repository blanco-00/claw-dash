## Context

当前总览页面 (`frontend/src/views/overview/index.vue`) 结构简单，仅包含：
- 1个简化的Gateway状态卡片
- 4个基础统计卡片（Agent统计、Cron任务、会话统计、任务队列）
- 1个Agent列表表格

已有但未充分利用的组件：
- `GatewayStatusCard.vue` - 详细Gateway状态
- `ResourceChart.vue` - 资源图表（CPU/内存）
- `StatCard.vue` - 可复用统计卡片
- `UptimeCard.vue` - 运行时长
- `VersionCard.vue` - 版本信息

项目使用 Vue 3 + Element Plus + TypeScript，紫色主题已配置。

## Goals / Non-Goals

**Goals:**
- 实现现代化的 Dashboard 网格布局
- 充分利用已有组件，减少重复代码
- 提供更丰富的数据可视化（任务分布图）
- 改善用户体验（实时状态、快速操作）

**Non-Goals:**
- 不修改后端 API（前端纯展示）
- 不添加新的图表库依赖（使用 Element Plus 内置或 CSS 实现）
- 不修改其他页面

## Decisions

### 1. 布局结构：3行卡片网格

采用 Element Plus 的 `el-row` 和 `el-col` 实现响应式网格：

```
第一行: Gateway状态横幅 (span=24)
第二行: 4个 KPI 统计卡片 (各 span=6)
第三行: 资源图表(8) + 任务分布(8) + 系统信息(8)
第四行: 最近任务(12) + 活跃Agent(12)
```

**Rationale**: 与现有项目风格保持一致，利用 Element Plus 栅格系统实现响应式布局。

### 2. 任务分布图：使用 CSS + SVG 实现的环形图

**Alternative**: 引入 ECharts 或 Chart.js
**Decision**: 不引入新依赖，使用纯 CSS + SVG 实现简单环形图
**Rationale**: 减少打包体积，项目中只有简单的数据展示需求

### 3. 任务列表和Agent面板

使用 Element Plus 的 `el-table` 和 `el-tag` 展示状态，颜色遵循现有系统：
- 待处理: `warning` (橙色)
- 运行中: `primary` (紫色)
- 已完成: `success` (绿色)
- 失败: `danger` (红色)

### 4. 数据获取

统一使用 `onMounted` 触发一次性数据加载，配合 `Promise.all` 并行获取：
- Gateway状态
- Dashboard概览（任务/Agent统计）
- 任务统计（各状态分布）
- 最近任务列表
- Agent列表

## Risks / Trade-offs

[Risk] 前端数据展示与后端实际状态可能存在延迟
→ **Mitigation**: 添加"最后更新时间"戳，刷新按钮允许手动同步

[Risk] 模拟数据可能导致页面在无数据时显示为空
→ **Mitigation**: ResourceChart 已有 loading 状态，使用 `el-skeleton` 改善加载体验

## Migration Plan

1. **分组件开发**: 先创建/增强子组件，最后集成到主页面
2. **保持向后兼容**: 旧页面代码保留注释，验证新布局正常后删除
3. **无数据回退**: 所有组件已有 loading 和 empty 状态处理

## Open Questions

- 资源数据（CPU/内存）后端是否提供？如无，是否需要新增 API？
- 是否需要定时刷新（Polling）？建议首次加载后不自动刷新，由用户手动刷新
