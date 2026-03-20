## 1. Backend - MCP Server Setup

- [x] 1.1 Add Spring AI MCP dependency to pom.xml
- [x] 1.2 Create McpServerConfig.java configuration class
- [x] 1.3 Configure MCP server endpoint in application.yml
- [ ] 1.4 Verify MCP server starts without errors

## 2. Backend - MCP Tools Implementation

- [x] 2.1 Create TaskQueueMcpTools.java with @McpTool annotations
- [x] 2.2 Implement task_create tool
- [x] 2.3 Implement task_list tool
- [x] 2.4 Implement task_status tool
- [x] 2.5 Implement task_complete tool
- [x] 2.6 Implement task_fail tool
- [x] 2.7 Implement task_stats tool

## 3. Backend - API Endpoints

- [ ] 3.1 Add MCP configuration info endpoint (for frontend to display)
- [ ] 3.2 Test MCP tools manually via curl

## 4. Frontend - MCP Configuration UI

- [x] 4.1 Add "Configure MCP" button to OpenClaw page
- [x] 4.2 Generate MCP configuration JSON
- [x] 4.3 Add copy to clipboard functionality
- [x] 4.4 Add success/error notifications

## 5. Integration Testing

- [ ] 5.1 Configure OpenClaw to connect to ClawDash MCP
- [ ] 5.2 Test task_create from OpenClaw agent
- [ ] 5.3 Test task_list from OpenClaw agent
- [ ] 5.4 Test task_status from OpenClaw agent

## 6. OpenClaw Configuration Cleanup

- [x] 6.1 Remove @openclaw-task-queue/core from allowed plugins in OpenClaw config
- [x] 6.2 Remove privy-jiner from allowed plugins in OpenClaw config
- [ ] 6.3 Verify OpenClaw works without the old plugins

## 7. Documentation

- [ ] 7.1 Update docs/MCP_INTEGRATION.md with final implementation details
