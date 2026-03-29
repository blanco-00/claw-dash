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
  taskId: string
  type: string
  title?: string
  payload: Record<string, unknown>
  priority: TaskPriority
  status: TaskStatus
  retryCount: number
  maxRetries: number
  claimedBy?: string
  assignedAgent?: string
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
  taskGroupId?: string
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
  sourceHandle?: string
  targetHandle?: string
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

// Edge Routing Types (Phase 3)

export type EdgeRoutingType = 'always' | 'task' | 'reply' | 'error'
export type DecisionMode = 'always' | 'llm'

export interface EdgeRoutingConfig {
  edgeType: EdgeRoutingType
  decisionMode: DecisionMode
  messageTemplate: string
  enabled: boolean
}

export interface EdgeTypeOption {
  value: EdgeRoutingType
  label: string
  labelCn: string
  description: string
}

export const EDGE_TYPE_OPTIONS: EdgeTypeOption[] = [
  { value: 'always', label: 'Always', labelCn: '始终', description: '无条件发送' },
  { value: 'task', label: 'Task', labelCn: '任务', description: '委托任务给目标 Agent' },
  { value: 'reply', label: 'Reply', labelCn: '回复', description: '任务完成后回复' },
  { value: 'error', label: 'Error', labelCn: '错误', description: '发生错误时通知' }
]

export const EDGE_TYPE_ICONS: Record<EdgeRoutingType, string> = {
  always: '📌',
  task: '📋',
  reply: '↩️',
  error: '⚠️'
}

export const EDGE_VARIABLE_HINTS: Record<EdgeRoutingType, string[]> = {
  always: [],
  task: ['{original_message}'],
  reply: ['{task_result}', '{original_message}'],
  error: ['{error_message}', '{original_message}']
}

// Agent Task Distribution Types

export interface NotifyAgentRequest {
  agentId: string
  taskGroupId?: string
  action: 'decompose' | 'execute' | 'complete'
}

export interface AgentStats {
  agentId: string
  pending: number
  running: number
  completed: number
  failed: number
  total: number
}

export interface AgentBinding {
  agentId: string
  agentName: string
  taskTypes: string[]
  pending: number
  running: number
  completed: number
}

// Sync Types (Phase 4)

export interface SyncAgentPreview {
  agentId: string
  blocksAdded: string[]
  blocksModified: string[]
  blocksRemoved: string[]
  diff: string
  newContent: string
}

export interface SyncPreviewResult {
  agents: SyncAgentPreview[]
  totalEdgesSynced: number
}

export interface SyncResult {
  agentsUpdated: string[]
  edgesSynced: number
  blocksAdded: number
  blocksUpdated: number
  blocksRemoved: number
  errors: Array<{ agentId: string; error: string }>
}

// Extended ConfigEdge with routing fields

export interface ConfigEdgeWithRouting extends ConfigEdge {
  edgeRoutingType: EdgeRoutingType
  decisionMode: DecisionMode
  messageTemplate: string
  replyTarget?: string
  replyTemplate?: string
  errorTarget?: string
  errorTemplate?: string
}
