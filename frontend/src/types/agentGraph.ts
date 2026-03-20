// Agent Graph Types for OpenClaw Agent Management

export interface AgentNode {
  id: string
  name: string
  description?: string
  workspace: string
  model?: string
  tags?: string[]
  isMain: boolean
  createdAt: string
  updatedAt: string
}

export interface AgentEdge {
  id: string
  source: string
  target: string
  type: 'assigns' | 'reports'
  label?: string
}

export interface AgentGraph {
  version: string
  lastSync: string
  nodes: AgentNode[]
  edges: AgentEdge[]
}

// Task Queue Types

export enum TaskStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  DEAD = 'DEAD'
}

export enum TaskPriority {
  LOW = 0,
  MEDIUM = 5,
  HIGH = 10
}

export interface TaskQueueTask {
  id: string
  type: string
  payload: Record<string, unknown>
  priority: TaskPriority
  status: TaskStatus
  retryCount: number
  maxRetries: number
  claimedBy?: string
  createdAt: string
  startedAt?: string
  completedAt?: string
  scheduledAt?: string
  result?: Record<string, unknown>
  error?: string
  dependsOn?: string[]
}

export interface CreateTaskRequest {
  type: string
  payload: Record<string, unknown>
  priority?: TaskPriority
  scheduledAt?: string
  maxRetries?: number
  dependsOn?: string[]
}

export interface TaskPageResponse {
  content: TaskQueueTask[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// Config Graph Types (VueFlow-based graph system)

export interface GraphNode {
  id: string
  name: string
  description?: string
  workspace: string
  model?: string
  tags?: string[]
  isMain: boolean
  createdAt: string
  updatedAt: string
}

export interface GraphEdge {
  id: string
  source: string
  target: string
  type: 'assigns' | 'reports'
}

export type EdgeType = 'assigns' | 'reports_to' | 'communicates'

export interface ConfigEdge {
  id: number
  graphId: number
  sourceId: string
  targetId: string
  edgeType: EdgeType
  enabled: boolean
  label?: string
}

export interface ConfigNode {
  id: number
  graphId: number
  agentId: string
  x: number
  y: number
  collapsed?: boolean
}

export interface ConfigGraph {
  id: number
  name: string
  description?: string
  version: number
  lastSyncedAt?: string
  createdAt: string
  updatedAt: string
}

export interface ConfigGraphWithDetails extends ConfigGraph {
  nodes: ConfigNode[]
  edges: ConfigEdge[]
}

export interface VueFlowNode {
  id: string
  type: string
  position: { x: number; y: number }
  data: {
    label: string
    agent?: GraphNode
    collapsed?: boolean
  }
}

export interface VueFlowEdge {
  id: string
  source: string
  target: string
  type?: string
  animated?: boolean
  data?: {
    edgeType: EdgeType
    label?: string
  }
}
