# OpenClaw Directory Structure Reference

> Critical knowledge for ClawDash developers working with OpenClaw agent workspaces.

## Directory Layout

```
~/.openclaw/                          # OpenClaw root config directory
├── openclaw.json                     # Main configuration file
├── workspace/                         # main agent's WORKSPACE
│   ├── AGENTS.md                     # ← Agent behavior rules (loaded by OpenClaw)
│   ├── SOUL.md                       # ← Agent persona/prompt
│   ├── USER.md                       # ← User preferences
│   ├── IDENTITY.md                   # ← Agent identity
│   ├── TOOLS.md                      # ← Tool notes
│   ├── HEARTBEAP.md                  # ← Heartbeat tasks
│   ├── BOOTSTRAP.md                  # ← First-run bootstrap
│   └── memory/                       # ← Daily memory logs
├── workspace-{agentId}/              # Other agents' WORKSPACES
│   ├── AGENTS.md                     # ← Agent behavior rules
│   ├── SOUL.md
│   └── ...
├── agents/                          # Agent instance directories (auto-created by OpenClaw)
│   ├── main/agent/                   # main agent's instance dir
│   │   ├── auth-profiles.json        # ← API credentials (runtime, NOT for AGENTS.md)
│   │   ├── models.json               # ← Model config (runtime)
│   │   └── thinking.json
│   ├── test/agent/                   # test agent's instance dir
│   │   ├── auth-profiles.json
│   │   ├── models.json
│   │   └── sessions/                 # ← Session history
│   └── {other-agents}/...
└── sessions/                         # Session storage
```

## Two Directory Types: Workspace vs agentDir

OpenClaw has **two completely different directory types** for each agent:

### 1. Workspace (`~/.openclaw/workspace/` or `~/.openclaw/workspace-{name}/`)

**Purpose**: Agent's "brain" — defines who the agent is and how it behaves.

| Item | Description |
|------|-------------|
| `AGENTS.md` | **Agent behavior rules** — loaded by OpenClaw on every session |
| `SOUL.md` | Agent persona/prompt — defines personality and tone |
| `USER.md` | User preferences — who the agent is helping |
| `IDENTITY.md` | Agent identity — name, emoji, avatar |
| `TOOLS.md` | Tool notes — conventions for using tools |
| `HEARTBEAT.md` | Heartbeat tasks — periodic background checks |
| `BOOTSTRAP.md` | First-run bootstrap — onboarding script |
| `memory/` | Daily memory logs — continuity between sessions |

**Who manages**: User/developer edits these files. OpenClaw reads them on session start.

**ClawDash sync target**: ✅ YES — sync writes AGENTS.md blocks here.

### 2. agentDir (`~/.openclaw/agents/{id}/agent/`)

**Purpose**: Agent instance runtime configuration — OpenClaw internal data.

| Item | Description |
|------|-------------|
| `auth-profiles.json` | API credentials and authentication |
| `models.json` | Model configuration overrides |
| `thinking.json` | Thinking mode settings |
| `sessions/` | Session history and transcripts |

**Who manages**: **OpenClaw auto-creates and manages these files.** Do NOT edit manually.

**ClawDash sync target**: ❌ NO — these are runtime files, not workspace files.

### Why the Confusion?

The directory naming is misleading:
- `agents/{id}/agent/` contains "agent" in the path but is NOT the workspace
- `workspace-{name}/` is the actual workspace but doesn't have "agent" in the name

**Remember**: If you want to modify agent behavior → edit files in `workspace-{name}/`. If you want to modify runtime config → edit `openclaw.json`, not files in `agents/`. 

## Critical: AGENTS.md Location

**The AGENTS.md file that OpenClaw reads is ALWAYS in the agent's workspace directory, NOT in `agents/{id}/workspace/`.**

| Agent | Workspace in openclaw.json | Correct AGENTS.md Path |
|-------|---------------------------|------------------------|
| main | `~/.openclaw/workspace` | `~/.openclaw/workspace/AGENTS.md` |
| test | `~/.openclaw/workspace-test` | `~/.openclaw/workspace-test/AGENTS.md` |

### WRONG Paths (Do NOT Use)

```
~/.openclaw/agents/main/workspace/AGENTS.md       ← WRONG! Does not exist
~/.openclaw/agents/test/workspace/AGENTS.md        ← WRONG! Does not exist
```

### Correct Sync Logic

When syncing to an agent, ClawDash MUST:

1. Read `openclaw.json` to get the agent's `workspace` configuration
2. If no `workspace` is configured for the agent, use `~/.openclaw/workspace` as default (for main agent)
3. Append `/AGENTS.md` to get the full path

```java
// Correct logic (pseudocode):
Path getAgentsMdPath(String agentId) {
    String workspace = getWorkspaceFromOpenClawJson(agentId);  // e.g., "~/.openclaw/workspace-test"
    if (workspace == null) {
        workspace = "~/.openclaw/workspace";  // default for main agent
    }
    workspace = expandTilde(workspace);
    return Path.of(workspace, "AGENTS.md");
}
```

## Common Mistake

The bug in `AgentsMdSyncService.getAgentsMdPath()` was:

```java
// WRONG - hardcoded wrong path:
return Path.of(System.getProperty("user.home"), ".openclaw", "agents", agentId, "workspace", "AGENTS.md");
```

This creates paths like `~/.openclaw/agents/main/workspace/AGENTS.md` which **do not exist**.

## How OpenClaw Resolves Agent Workspace

1. OpenClaw reads `openclaw.json` → `agents.list[]`
2. For each agent, checks for `workspace` field
3. If no `workspace` field, defaults to `~/.openclaw/workspace`
4. OpenClaw loads bootstrap files (AGENTS.md, SOUL.md, etc.) from the resolved workspace directory
5. The `agents/{id}/agent/` directory is for runtime instance data only — **NOT** for workspace files

## References

- OpenClaw Docs: [Agent Workspace](https://www.openclawlab.com/zh-cn/docs/concepts/agent-workspace/)
- OpenClaw Issue [#29387](https://github.com/openclaw/openclaw/issues/29387) — Bootstrap files in agentDir are silently ignored
