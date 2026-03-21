# Config Graph Edge Routing Editor

## ADDED Requirements

### Requirement: Edge routing fields

The system SHALL store routing information on Config Graph edges.

#### Scenario: View edge with routing
- **WHEN** user selects an edge in Config Graph
- **THEN** system displays: edge_type, decision_mode, message_template, enabled

#### Scenario: Edit edge routing
- **WHEN** user edits edge fields and clicks Save
- **THEN** system updates edge in database
- **AND** system shows "Saved" confirmation

#### Scenario: Create edge with routing
- **WHEN** user creates a new edge via drag-connect
- **THEN** system creates edge with default values:
  - `edge_type = 'task'`
  - `decision_mode = 'always'`
  - `message_template = ''`
  - `enabled = true`
- **AND** system opens edge detail panel for editing

#### Scenario: Delete edge
- **WHEN** user clicks Delete on edge detail panel
- **THEN** system deletes edge from database
- **AND** edge disappears from Config Graph

### Requirement: Edge type selector

The system SHALL display edge type options from `edge_types` database table.

#### Scenario: Select edge type
- **WHEN** user opens edge detail panel
- **THEN** system fetches edge_types from `/api/edge-types`
- **AND** displays as radio buttons or dropdown:
  - 始终 (always) - 无条件发送
  - 任务 (task) - 委托任务给目标 agent
  - 回复 (reply) - 完成任务后回复给某 agent
  - 错误 (error) - 发生错误时通知某 agent

#### Scenario: Edge type changes message template hint
- **WHEN** user selects edge type
- **THEN** system shows relevant variable hints:
  - `task`: "{original_message}"
  - `reply`: "{task_result}"
  - `error`: "{error_message}"
  - `always`: no variables

### Requirement: Decision mode selector

The system SHALL allow choosing how the agent decides to send.

#### Scenario: Select decision mode
- **WHEN** user opens edge detail panel
- **THEN** system displays decision mode options:
  - 直接发送 (always) - 无条件发送
  - AI 判断 (llm) - 由 AI 决定是否发送及内容

#### Scenario: LLM mode shows description
- **WHEN** user selects "AI 判断" mode
- **THEN** system shows description: "AI 会判断这个消息是否应该发送给目标 agent，以及发送什么内容"

### Requirement: Message template editor

The system SHALL provide a textarea for editing the message template.

#### Scenario: Edit message template
- **WHEN** user types in message template field
- **THEN** system supports variable substitution hints

#### Scenario: Variable hints displayed
- **WHEN** user edits message template
- **THEN** system shows available variables:
  - `{original_message}` - 触发路由的原始消息
  - `{task_result}` - 目标任务的结果
  - `{error_message}` - 错误描述
  - `{source_id}` - 源 agent ID
  - `{target_id}` - 目标 agent ID

#### Scenario: Empty template for always mode
- **WHEN** edge type is `always` and decision mode is `always`
- **THEN** message template may be empty (use default "Done")

### Requirement: Enable/disable edge

The system SHALL allow toggling edge routing on/off.

#### Scenario: Disable edge
- **WHEN** user unchecks "启用此路由"
- **THEN** system sets `enabled = false`
- **AND** edge appears grayed out in Config Graph
- **AND** edge block is NOT synced to AGENTS.md

#### Scenario: Enable edge
- **WHEN** user checks "启用此路由"
- **THEN** system sets `enabled = true`
- **AND** edge appears normal in Config Graph
- **AND** edge block WILL be synced on next sync

### Requirement: Sync preview from edge panel

The system SHALL allow previewing sync changes for a single edge.

#### Scenario: Preview sync for one edge
- **WHEN** user clicks "预览 Sync 效果" in edge panel
- **THEN** system calls `GET /api/config-graphs/{graphId}/sync-preview?edgeId={id}`
- **AND** displays diff for affected agent's AGENTS.md
- **AND** does NOT write any files

### Requirement: Full sync from Config Graph

The system SHALL sync all edges from Config Graph to OpenClaw.

#### Scenario: Sync all edges
- **WHEN** user clicks "Sync to OpenClaw" button in Config Graph
- **THEN** system calls `POST /api/config-graphs/{id}/sync`
- **AND** displays result: agents updated, edges synced, blocks added/updated/removed
- **AND** shows success/error message

---

## Technical Notes

- Edge detail panel is a slide-out sidebar
- Edges in graph view show edge_type icon (task=📋, reply=↩️, error=⚠️, always=📌)
- Disabled edges render with dashed line
- Sync preview uses diff2html or similar for display
