# Simplify Edge Config — Implementation Tasks

## Phase 1: Database Migration

- [x] 1.1 创建 V6__edge_reply_error_config.sql migration
- [x] 1.2 添加 reply_target, reply_template, error_target, error_template 字段

## Phase 2: Backend

### 2.1 Entity Update
- [x] 2.1.1 ConfigGraphEdge.java 添加 4 个新字段
- [x] 2.1.2 添加 getter/setter

### 2.2 Sync Logic Update
- [x] 2.2.1 修改 AgentsMdSyncService.generateBlock() 生成三种块
- [x] 2.2.2 修改 syncToAgent() 支持多 agent 同步（源 agent 写 task，目标 agent 写 reply/error）
- [x] 2.2.3 修改 syncPreview() 按 agent 分组显示
- [x] 2.2.4 修改 SyncPreviewResult 包含所有受影响的 agent

### 2.3 API Update
- [x] 2.3.1 ConfigGraphController.updateEdge() 支持新字段
- [x] 2.3.2 ConfigGraphController.addEdge() 支持新字段

## Phase 3: Frontend

### 3.1 ConfigGraphTab Changes
- [x] 3.1.1 移除 linkMode 状态（已完成）
- [x] 3.1.2 移除 linkMode 选择器（已完成）
- [x] 3.1.3 新建边时添加 reply/error 默认空值（通过 EdgeDetailPanel）

### 3.2 EdgeDetailPanel Redesign
- [x] 3.2.1 重构为左右两栏布局
- [x] 3.2.2 左侧：源配置（task 消息模板）
- [x] 3.2.3 右侧：目标配置（reply/error 下拉+模板）
- [x] 3.2.4 移除边类型和决策模式选择器
- [x] 3.2.5 添加 agent 下拉选择器（用于 reply/error 目标）
- [x] 3.2.6 保存时传递所有新字段

### 3.3 Sync Preview Updates
- [x] 3.3.1 SyncPreviewDialog 按 agent 分组显示（已有 el-tabs 实现）
- [x] 3.3.2 显示每个 agent 的变更块（已有 blocksAdded/Modified/Removed 实现）

## Phase 4: Testing

- [ ] 4.1 运行 V6 migration
- [ ] 4.2 重启 backend
- [ ] 4.3 测试创建 task 边 + 配置 reply/error
- [ ] 4.4 测试同步预览显示正确
- [ ] 4.5 测试同步后检查 AGENTS.md 文件

---

## Task Dependencies

```
Phase 1 (Database)
    ↓
Phase 2.1 (Entity) → Phase 2.2 (Sync) → Phase 2.3 (API)
    ↓
Phase 3.1 (ConfigGraphTab) ─────────────────┐
    ↓                                        │
Phase 3.2 (EdgeDetailPanel) ───────────────► Phase 4 (Testing)
    ↓                                        │
Phase 3.3 (SyncPreview) ────────────────────┘
```
