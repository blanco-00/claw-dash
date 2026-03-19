import request from '@/utils/request'
import type { AgentNode, AgentEdge, AgentGraph } from '@/types/agentGraph'

export interface OpenClawAgent {
  name: string
  workspace?: string
}

export interface CreateAgentRequest {
  name: string
  workspace: string
}

export interface BindAgentRequest {
  channel: string
}

export function listOpenClawAgents() {
  return request.get<{ data: string[] }>('/openclaw/agents')
}

export function createOpenClawAgent(data: CreateAgentRequest) {
  return request.post<{ data: { name: string; workspace: string; created: boolean } }>(
    '/openclaw/agents',
    data
  )
}

export function deleteOpenClawAgent(name: string) {
  return request.delete<{ code: number }>(`/openclaw/agents/${name}`)
}

export function bindOpenClawAgent(name: string, data: BindAgentRequest) {
  return request.post<{ code: number }>(`/openclaw/agents/${name}/bind`, data)
}

export function getMainAgent() {
  return request.get<{ data: { mainAgentId: string } }>('/openclaw/agents/main')
}

export function loadGraph(): AgentGraph {
  const stored = localStorage.getItem('agent-graph')
  if (stored) {
    return JSON.parse(stored)
  }
  return { nodes: [], edges: [] }
}

export function saveGraph(graph: AgentGraph) {
  localStorage.setItem('agent-graph', JSON.stringify(graph))
}
