## Context

当前 ClawDash 系统使用表格形式展示 Agent 列表，用户无法直观理解 Agent 之间的层级关系。OpenClaw 本身有 Agent 委派机制，但缺乏可视化呈现。

## Goals / Non-Goals

**Goals:**

- 使用 d3-org-chart 实现女儿国 Agent 组织架构图
- 支持自定义节点样式，区分不同官职等级
- 支持交互式展开/折叠
- 与现有 Agent 页面整合

**Non-Goals:**

- 不修改后端 API（使用现有 API + 前端映射）
- 不实现实时数据推送（使用轮询）
- 不支持节点拖拽编辑

## Decisions

### 1. 使用 d3-org-chart 而非其他方案

**选择**: d3-org-chart (https://github.com/bumbeishvili/org-chart)

**理由**:

- 功能最全面：支持展开/折叠、搜索、多种布局
- Vue 兼容性好
- 社区活跃 (1.1k stars)
- 支持自定义节点样式

**替代方案**:

- ECharts 树图: 功能有限，交互性差
- D3.js 原生: 需要更多开发时间
- react-d3-tree: React 专用

### 2. 前端映射层级关系

**方案**: 在前端维护 Agent 层级配置，不修改后端

**理由**:

- OpenClaw 本身没有明确的父子层级配置
- 女儿国体系是业务层面的虚拟设定
- 最小化后端改动

### 3. 节点颜色按官职等级区分

| 等级 | 官职        | 颜色           |
| ---- | ----------- | -------------- |
| 1    | 皇后        | #FFD700 (金色) |
| 2    | 皇贵妃/贵妃 | #FF69B4 (粉红) |
| 3    | 妃          | #9370DB (紫色) |
| 4    | 嫔          | #87CEEB (天蓝) |
| 5    | 丫鬟        | #90EE90 (浅绿) |

## Risks / Trade-offs

- [风险] d3-org-chart 依赖 D3 v7，可能与现有 ECharts 冲突 → 隔离引入，按需加载
- [风险] 大规模节点性能 → 限制默认展开层级，深层次默认折叠
- [风险] 中文渲染 → 使用系统字体，设置 fallback

## Migration Plan

1. 安装依赖: `npm install d3 d3-org-chart d3-flextree`
2. 创建 AgentOrgChart.vue 组件
3. 在 Agent 页面添加视图切换 Tab
4. 配置层级数据映射
5. 测试不同屏幕尺寸
6. 部署验证
