import { loadGraph, saveGraph } from './agentApi'
import type { AgentGraph, AgentNode, AgentEdge } from '@/types/agentGraph'

export function loadGraphFromStorage(): AgentGraph {
  return loadGraph()
}

export function saveGraphToStorage(graph: AgentGraph): void {
  saveGraph(graph)
}

export function detectMainAgent(): string {
  return 'main'
}

export async function parseSoulMdRelations(workspacePath: string): Promise<AgentEdge[]> {
  const edges: AgentEdge[] = []
  return edges
}

export function createNode(name: string, workspace: string): AgentNode {
  return {
    id: name,
    name,
    workspace,
    isMain: false,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  }
}

export function createEdge(source: string, target: string, type: 'assigns' | 'reports'): AgentEdge {
  return {
    id: `${source}-${type}-${target}`,
    source,
    target,
    type,
    label: type
  }
}
