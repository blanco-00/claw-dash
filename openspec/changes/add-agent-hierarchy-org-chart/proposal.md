## Why

当前 ClawDash 的 Agent 页面只展示表格形式列表，无法直观看到 Agent 之间的层级关系和汇报路径。需要添加组织架构图可视化，让用户一目了然地看到女儿国的官职体系。

## What Changes

- 新增 Agent 组织架构图页面，使用 d3-org-chart 库
- 在现有 Agent 管理页面添加"视图切换"功能（表格/架构图）
- 女儿国主题的自定义节点样式（皇后/贵妃/妃/嫔/丫鬟不同颜色）
- 支持点击节点查看 Agent 详情
- 支持展开/折叠子节点

## Capabilities

### New Capabilities

- `agent-hierarchy-org-chart`: Agent 组织架构图可视化，展示女儿国官职层级关系

### Modified Capabilities

- (无)

## Impact

- 新增依赖: `d3`, `d3-org-chart`, `d3-flextree`
- 新增页面: `/agents-org` 或在 `/agents` 页面添加视图切换
- 修改现有 API 可能需要添加 Agent 层级关系数据
