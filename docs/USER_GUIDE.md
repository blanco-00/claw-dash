# ClawDash User Guide

> English | [中文](./USER_GUIDE.zh-CN.md)

## Overview

ClawDash is a visual configuration tool for OpenClaw. It provides a graphical interface to manage multi-agent topologies, configure routing between agents, and monitor runtime status.

## Features

### 1. Agent Config Graph

Visual topology editor for managing agent relationships.

- **Add Node**: Create new agents with templates
- **Connect Edges**: Drag from source to target to create communication routes
- **Edge Config**: Configure task → reply → error routing for each connection
- **Sync Preview**: Review changes before writing to `AGENTS.md`

### 2. Agent List

View and manage all registered agents.

- View agent status (running/stopped)
- Configure agent parameters
- Monitor agent health

### 3. Runtime Graph

Real-time view of agent communication.

- See active sessions between agents
- Monitor message flow in real-time

### 4. Task Queue

Async task management with priority, retries, and dependencies.

- Create tasks with custom payloads
- Set priority levels
- Configure retry policies
- View task status and results

### 5. Cron Tasks

Scheduled task management.

- Create cron expressions for recurring tasks
- View execution history
- Enable/disable schedules

### 6. Task Groups

Batch task management with dependency chains.

- Group related tasks together
- Configure dependencies between tasks
- Execute in parallel or sequential order

### 7. Sessions

Active session monitoring.

- View all active agent sessions
- Session statistics and duration
- End sessions if needed

### 8. Tokens

API token management for agent authentication.

- Create and revoke tokens
- Set access permissions
- View usage statistics

### 9. OpenClaw

One-click OpenClaw installation and management.

- Install OpenClaw with one click
- Monitor installation status
- Manage plugins

### 10. Docker

Container monitoring for OpenClaw services.

- View container status
- Resource usage (CPU, memory)
- Container logs

### 11. Edge Types

Manage edge type definitions for agent routing.

- Define custom edge types
- Configure routing rules
- Set default behaviors

## Getting Started

### Login

Access the system with default admin credentials or skip authentication in development mode.

### Create a Task

1. Go to **Task Queue**
2. Click **New Task**
3. Fill in task type, payload, and priority
4. Click **Create**

### Configure Agent Routing

1. Go to **Agent → Config Graph**
2. Click **Add Node** to create or select an agent
3. Drag from source agent to target agent to create an edge
4. Click the edge to open the config panel
5. Set **Task Message** (left) and **Reply/Error Routing** (right)
6. Click **Sync to OpenClaw** to preview and apply changes

### Monitor Agents

1. Go to **Agent → Runtime Graph** to see active communication
2. Go to **Sessions** to monitor active connections
3. Go to **Task Queue** to track async task execution

## FAQ

### Q: How to enable authentication?

A: Set `AUTH_ENABLED=true` in environment variables, then register via `/api/auth/register`.

### Q: How to view API documentation?

A: Access `/swagger-ui.html` for the full API reference.

### Q: How to extend functionality?

A: Add new controllers under `backend/src/main/java/com/clawdash/controller`.

## Tech Stack

- **Backend**: Spring Boot 3.4, MyBatis-Plus, JWT, Redis
- **Frontend**: Vue 3, TypeScript, Element Plus, ECharts
- **Database**: MySQL 8.0, Redis
- **Deployment**: Docker Compose
