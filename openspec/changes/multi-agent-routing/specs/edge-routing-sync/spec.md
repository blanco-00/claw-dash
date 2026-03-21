# Edge Routing → AGENTS.md Sync

## ADDED Requirements

### Requirement: Generate CLAWDASH block for edge

The system SHALL generate a properly formatted CLAWDASH block for each enabled edge.

#### Scenario: Generate task + always block
- **WHEN** system generates block for edge with `edge_type='task'` and `decision_mode='always'`
- **THEN** output is:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="task" decision="always" -->
## [CLAWDASH] Task → {target_agent}

Type: 任务 (委托任务)
Decision: 直接发送，无条件
Message: {message_template}

<!-- CLAWDASH:BLOCK END -->
```

#### Scenario: Generate reply + llm block
- **WHEN** system generates block for edge with `edge_type='reply'` and `decision_mode='llm'`
- **THEN** output is:
```markdown
<!-- CLAWDASH:BLOCK id="edge-{uuid}" edge_type="reply" decision="llm" -->
## [CLAWDASH] Reply → {target_agent}

Type: 回复 (完成任务后回复)
Decision: 由 AI 判断是否发送此回复，以及发送什么内容
如果判断需要回复，使用 sessions_send 发送回复消息。
Message template: {message_template}

<!-- CLAWDASH:BLOCK END -->
```

#### Scenario: Generate error block
- **WHEN** system generates block for edge with `edge_type='error'`
- **THEN** block is targeted at the **source** agent (not target)
- **AND** output uses "error" edge_type with always decision
- **AND** message_template is used as-is

### Requirement: Parse existing CLAWDASH blocks

The system SHALL parse existing CLAWDASH blocks from AGENTS.md file.

#### Scenario: Parse block map
- **WHEN** system reads AGENTS.md content
- **THEN** system extracts all `<!-- CLAWDASH:BLOCK -->` sections
- **AND** builds map of `id → block_content`
- **AND** preserves content outside CLAWDASH blocks unchanged

#### Scenario: Handle malformed blocks
- **WHEN** AGENTS.md contains malformed CLAWDASH blocks
- **THEN** system logs warning
- **AND** treats malformed content as outside blocks (preserved)

### Requirement: Sync to AGENTS.md per agent

The system SHALL write CLAWDASH blocks to each affected agent's AGENTS.md.

#### Scenario: Add new block
- **WHEN** edge UUID not found in existing blocks
- **THEN** system appends new block to end of AGENTS.md content

#### Scenario: Update existing block
- **WHEN** edge UUID found in existing blocks
- **THEN** system replaces existing block with new block content

#### Scenario: Remove deleted edge block
- **WHEN** edge UUID exists in AGENTS.md but not in current edges list
- **THEN** system removes that block from AGENTS.md

#### Scenario: Skip disabled edges
- **WHEN** edge has `enabled = false`
- **THEN** system does NOT generate a block
- **AND** removes any existing block for that edge

### Requirement: Atomic file write

The system SHALL write AGENTS.md atomically to prevent corruption.

#### Scenario: Atomic write
- **WHEN** system writes to AGENTS.md
- **THEN** system writes to temp file first
- **AND** renames temp file to target on success
- **AND** cleans up temp file on failure

### Requirement: Sync preview

The system SHALL generate a preview of changes without writing files.

#### Scenario: Preview changes
- **WHEN** user requests sync preview
- **THEN** system simulates sync
- **AND** returns diff for each affected agent:
  - Blocks to be added (+)
  - Blocks to be modified (~)
  - Blocks to be removed (-)
- **AND** does NOT write any files

#### Scenario: Preview response format
```json
{
  "agents": [
    {
      "agentId": "main",
      "blocksAdded": ["edge-uuid-1"],
      "blocksModified": ["edge-uuid-2"],
      "blocksRemoved": [],
      "newContent": "... (full AGENTS.md preview)"
    }
  ],
  "totalEdgesSynced": 2
}
```

### Requirement: Sync execution with result

The system SHALL execute sync and return detailed result.

#### Scenario: Successful sync
- **WHEN** user executes sync
- **THEN** system returns:
```json
{
  "agentsUpdated": ["main", "test"],
  "edgesSynced": 4,
  "blocksAdded": 2,
  "blocksUpdated": 1,
  "blocksRemoved": 0,
  "errors": []
}
```

#### Scenario: Partial failure
- **WHEN** sync partially fails (e.g., one agent file not writable)
- **THEN** system returns:
```json
{
  "agentsUpdated": ["main"],
  "edgesSynced": 2,
  "blocksAdded": 1,
  "blocksUpdated": 1,
  "blocksRemoved": 0,
  "errors": [
    { "agentId": "test", "error": "Permission denied" }
  ]
}
```
- **AND** continues with remaining agents

### Requirement: Error edge sync location

The system SHALL sync error edges to the **source** agent's AGENTS.md, not target.

#### Scenario: Error edge sync
- **WHEN** edge has `edge_type='error'` with source='test' and target='main'
- **THEN** system generates block in test's AGENTS.md
- **AND** block content references notifying 'main'

### Requirement: Validate agent existence

The system SHALL validate that source/target agents exist before syncing.

#### Scenario: Agent does not exist
- **WHEN** edge references non-existent agent
- **THEN** system skips that edge in sync
- **AND** adds to errors list: "Agent '{agentId}' not found"

---

## Technical Notes

### File Paths

- Agent workspace: `~/.openclaw/agents/{agentId}/workspace/AGENTS.md`
- Alternative: `~/.openclaw/agents/{agentId}/AGENTS.md` (if no workspace subdir)

### Block ID Convention

- UUID v4 format: `edge-a1b2c3d4-e5f6-7890-abcd-ef1234567890`
- Generated client-side when edge is created
- Stable across syncs (never changes)

### Sync Locking

- Per-agent lock during sync to prevent race conditions
- Lock timeout: 30 seconds
- If lock cannot be acquired, skip agent with error

### Backup

- Before any write, backup current file to `AGENTS.md.bak`
- Backups retained for 7 days
