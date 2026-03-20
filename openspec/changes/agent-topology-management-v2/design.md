## Context

**现状：**
- OpenClaw agents 通过 CLI 管理（`openclaw agents add/delete/bind` 等）
- ClawDash 有 Config Graph（节点+边）和 Agent List 页面
- 但存在数据源不统一的问题：ClawDash agents 表 vs OpenClaw 文件系统

**OpenClaw Agent 结构：**
```
~/.openclaw/agents/{agent-name}/
└── agent/
    ├── auth-profiles.json   # 认证配置
    └── models.json          # 模型配置
```

**OpenClaw CLI 命令：**
- `openclaw agents list` - 列出所有 agents
- `openclaw agents add <name>` - 创建新 agent
- `openclaw agents delete <name>` - 删除 agent
- `openclaw agents bind --agent <name> --bind <channel>` - 绑定到 channel
- `openclaw agents set-identity --agent <name> --name xxx` - 设置身份

**关键发现：**
- OpenClaw 的 "bind" 是把 agent 绑定到 channel（如 wechat、telegram），不是 agent 之间的关系
- agent 之间的 A2A 通信关系需要 ClawDash 自己管理

## Goals / Non-Goals

**Goals:**
- Config Graph 作为 OpenClaw agents 的主要管理界面
- 一键添加 agent 到拓扑图（调用 openclaw agents add）
- 可视化配置 agent 之间的 A2A 关系
- OpenClaw 作为 agent 实体的唯一真实来源

**Non-Goals:**
- 不管理 OpenClaw 的 channel 绑定细节
- 不复制 agent 数据到 ClawDash agents 表
- 不实现 agent 之间的实际消息传递

## Decisions

### Decision 1: 数据源策略 - OpenClaw 作为唯一真实来源

**选择：** OpenClaw 文件系统是 agent 实体的唯一来源，ClawDash 只存储拓扑配置

**理由：**
- 避免数据不一致
- 简化 ClawDash 数据模型
- OpenClaw CLI 是事实标准

**替代方案：**
- ClawDash 存储 agent 副本 → 会有同步问题
- 分层混合 → 增加复杂度

### Decision 2: 节点类型 - Config Graph 节点直接引用 OpenClaw Agent

**选择：** 节点只存储 OpenClaw agent name 作为引用

**理由：**
- 节点是 OpenClaw agent 的"投影"
- 实际 agent 数据在 OpenClaw，ClawDash 只是展示
- 简化节点数据模型

**替代方案：**
- 节点存储完整 agent 数据副本 → 数据冗余，同步复杂

### Decision 3: 边的语义 - A2A 关系存 ClawDash，OpenClaw bind 独立

**选择：** Config Graph 的边代表 A2A 通信能力，存储在 ClawDash；OpenClaw bind 代表 channel 订阅，两者独立

**理由：**
- OpenClaw bind 是 agent-to-channel 的绑定
- Config Graph 边是 agent-to-agent 的关系
- 两者语义不同，应该分开管理

### Decision 4: Agent List 定位 - 详情展示，不做管理

**选择：** Agent List 作为只读详情页，补充 Config Graph

**理由：**
- 集中展示 agent 信息（名称、封号、官职、职责）
- 不做管理操作（管理在 Config Graph）

## Risks / Trade-offs

**[Risk]** OpenClaw 目录结构变化 → **Mitigation:** 定期检查 OpenClaw CLI 行为，保持兼容

**[Risk]** 两个数据源（OpenClaw + ClawDash 边）需要同步 → **Mitigation:** 明确职责，OpenClaw 管实体，ClawDash 管拓扑

**[Risk]** 删除 OpenClaw agent 后 Config Graph 节点变成 dangling reference → **Mitigation:** 刷新时检测并提示用户

## Open Questions

1. Agent 的"封号、官职、职责"信息存储在哪里？目前 OpenClaw 目录下没有 IDENTITY.md 文件
2. A2A 关系的具体语义是什么？是"可以通信"还是"实际有通信"？
3. 是否需要支持从 Config Graph 删除 agent（调用 openclaw agents delete）？
