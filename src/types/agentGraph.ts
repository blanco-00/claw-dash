// Agent Graph Types for OpenClaw Agent Management

export interface AgentNode {
  id: string // OpenClaw agent ID
  name: string // Display name
  description?: string // Business description
  workspace: string // OpenClaw workspace path
  model?: string // Model configuration
  tags?: string[] // Business tags
  isMain: boolean // Whether this is main agent (read-only)
  createdAt: string
  updatedAt: string
}

export interface AgentEdge {
  id: string
  source: string // Source node ID (assigns/reports from)
  target: string // Target node ID (assigns/reports to)
  type: 'assigns' | 'reports'
  label?: string // Optional edge label
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
