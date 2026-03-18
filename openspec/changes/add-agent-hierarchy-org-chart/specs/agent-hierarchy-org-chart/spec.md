## ADDED Requirements

### Requirement: Agent组织架构图展示

系统 SHALL 提供可视化的 Agent 组织架构图，直观展示女儿国体系中各 Agent 的汇报关系。

#### Scenario: 默认展示完整层级

- **WHEN** 用户访问 Agent 组织架构图页面
- **THEN** 显示完整的女儿国官职层级结构，默认展开到第三级

#### Scenario: 节点颜色区分官职等级

- **WHEN** 架构图渲染节点时
- **THEN** 根据官职等级显示不同颜色：皇后(金)、贵妃(粉)、妃(紫)、嫔(蓝)、丫鬟(绿)

### Requirement: 节点交互功能

架构图 SHALL 支持基本的节点交互功能。

#### Scenario: 点击展开/折叠子节点

- **WHEN** 用户点击节点左侧的展开/折叠图标
- **THEN** 展开或折叠该节点下的子节点，带动画过渡

#### Scenario: 点击节点查看详情

- **WHEN** 用户点击节点（不包括展开图标）
- **THEN** 打开详情抽屉，显示 Agent 的名称、封号、职责、状态

### Requirement: 视图切换

系统 SHALL 支持在表格视图和组织架构图之间切换。

#### Scenario: 切换到架构图视图

- **WHEN** 用户点击"架构图"Tab
- **THEN** 切换到组织架构图展示模式

#### Scenario: 切换回表格视图

- **WHEN** 用户点击"列表"Tab
- **THEN** 切换回原有的表格列表模式

### Requirement: 架构图导航

架构图 SHALL 提供导航辅助功能。

#### Scenario: 自动布局适应屏幕

- **WHEN** 架构图渲染时
- **THEN** 自动调整布局以适应视口大小，显示适合的缩放级别

#### Scenario: 平移查看

- **WHEN** 用户拖拽画布
- **THEN** 架构图随拖拽移动，允许查看超出视口的区域
