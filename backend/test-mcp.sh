#!/bin/bash

# Test MCP Server via SSE Transport
# This script demonstrates how to connect to the ClawDash MCP server

BASE_URL="http://localhost:5178"

echo "=== Step 1: Connect to SSE endpoint to get session ==="
# Connect to SSE and capture session ID
SESSION_RESPONSE=$(curl -s -N -H "Accept: text/event-stream" "${BASE_URL}/sse" 2>&1)

# Extract session ID from the response
SESSION_ID=$(echo "$SESSION_RESPONSE" | grep "data:" | sed 's/data://' | sed 's/\?sessionId=//' | tr -d '[:space:]')

echo "Session ID: $SESSION_ID"

if [ -z "$SESSION_ID" ]; then
    echo "Failed to get session ID"
    exit 1
fi

echo ""
echo "=== Step 2: Initialize MCP session ==="
INIT_RESPONSE=$(curl -s -X POST "${BASE_URL}/mcp/message?sessionId=${SESSION_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "initialize",
    "params": {
      "protocolVersion": "2024-11-05",
      "capabilities": {},
      "clientInfo": {
        "name": "test-client",
        "version": "1.0"
      }
    }
  }')

echo "$INIT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$INIT_RESPONSE"

echo ""
echo "=== Step 3: List available tools ==="
TOOLS_RESPONSE=$(curl -s -X POST "${BASE_URL}/mcp/message?sessionId=${SESSION_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 2,
    "method": "tools/list",
    "params": {}
  }')

echo "$TOOLS_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TOOLS_RESPONSE"

echo ""
echo "=== Step 4: Call task_list tool ==="
CALL_RESPONSE=$(curl -s -X POST "${BASE_URL}/mcp/message?sessionId=${SESSION_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc": "2.0",
    "id": 3,
    "method": "tools/call",
    "params": {
      "name": "task_list",
      "arguments": {}
    }
  }')

echo "$CALL_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CALL_RESPONSE"
