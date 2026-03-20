#!/usr/bin/env python3
import requests
import json
import time
import threading

BASE_URL = "http://localhost:5178"
session_id = None
session_ready = threading.Event()

def sse_listener():
    global session_id
    try:
        with requests.get(f"{BASE_URL}/sse", stream=True, headers={"Accept": "text/event-stream"}, timeout=5) as r:
            for line in r.iter_lines():
                if line:
                    line = line.decode('utf-8')
                    if line.startswith('data:'):
                        data = line[5:].strip()
                        if 'sessionId=' in data:
                            session_id = data.split('sessionId=')[1]
                            print(f"Got session ID: {session_id}")
                            session_ready.set()
                            break
    except Exception as e:
        print(f"SSE Error: {e}")

print("=== Starting SSE listener in background ===")
t = threading.Thread(target=sse_listener, daemon=True)
t.start()

print("Waiting for session...")
if session_ready.wait(timeout=10):
    print(f"\n=== Session ready: {session_id} ===\n")
    
    print("=== Step 2: Initialize MCP session ===")
    init_resp = requests.post(
        f"{BASE_URL}/mcp/message?sessionId={session_id}",
        headers={"Content-Type": "application/json"},
        json={
            "jsonrpc": "2.0",
            "id": 1,
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {"name": "test-client", "version": "1.0"}
            }
        }
    )
    print(json.dumps(init_resp.json(), indent=2))
    
    print("\n=== Step 3: List available tools ===")
    tools_resp = requests.post(
        f"{BASE_URL}/mcp/message?sessionId={session_id}",
        headers={"Content-Type": "application/json"},
        json={
            "jsonrpc": "2.0",
            "id": 2,
            "method": "tools/list",
            "params": {}
        }
    )
    print(json.dumps(tools_resp.json(), indent=2))
    
    print("\n=== Step 4: Call task_list tool ===")
    call_resp = requests.post(
        f"{BASE_URL}/mcp/message?sessionId={session_id}",
        headers={"Content-Type": "application/json"},
        json={
            "jsonrpc": "2.0",
            "id": 3,
            "method": "tools/call",
            "params": {
                "name": "task_list",
                "arguments": {}
            }
        }
    )
    print(json.dumps(call_resp.json(), indent=2))
else:
    print("Failed to get session ID")
