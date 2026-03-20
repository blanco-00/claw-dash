## Why

OpenClaw agents 目前只能通过 CLI 管理，缺乏可视化界面。用户希望能在 ClawDash 中直观地管理 OpenClaw agents，包括一键添加、配置 agent 之间的 A2A 通信关系，并通过节点+连线的图来展示拓扑结构。

## What Changes

- **Config Graph 页面重构**：作为 OpenClaw agents 的主要管理界面
  - 节点 = OpenClaw Agent（通过 `openclaw agents add` 创建）
  - 边 = A2A 通信关系（存储在 ClawDash 数据库）
  - 节点位置/样式（存储在 ClawDash 数据库）

- **Agent List 页面调整**：
  - 不再作为独立的数据存储
  - 作为 Config Graph 的补充，显示 OpenClaw agents 详情
  - 提供快速查看 agent 信息（名称、封号、官职、职责）的入口

- **数据源策略**：
  - OpenClaw 文件系统作为 agent 实体的唯一真实来源
  - ClawDash 只存储拓扑关系和 UI 相关配置
  - 废弃或简化 ClawDash agents 表

- **OpenClaw 命令集成**：
  - `openclaw agents add` - 创建新 agent
  - `openclaw agents delete` - 删除 agent
  - `openclaw agents bind` - 绑定 agent 到 channel
  - `openclaw agents set-identity` - 设置 agent 身份

## Capabilities

### New Capabilities

- `config-graph-agent-management`: Config Graph 作为 OpenClaw agent 管理界面
  - 从 OpenClaw 读取 agents 列表并展示
  - 一键添加 agent 到拓扑图（调用 openclaw agents add）
  - 可视化编辑 agent 之间的 A2A 关系
  - 节点拖拽、位置保存

- `agent-identity-editor`: Agent 身份信息编辑
  - 读取 OpenClaw agent 目录下的配置文件
  - 显示/编辑 agent 的名称、封号、官职、职责
  - 与 OpenClaw set-identity 命令集成

### Modified Capabilities

- `agent-topology-tables`: 现有数据库表结构调整
  - config_graph_nodes 表增加 agent name 引用
  - 简化 agents 表或标记为废弃

## Impact

- **Frontend**: ConfigGraphTab.vue, AgentListTab.vue 重构
- **Backend**: OpenClawService 新增方法，AgentController 调整
- **Database**: config_graph_nodes 表结构可能需要调整
- **External**: 依赖 OpenClaw CLI 命令
