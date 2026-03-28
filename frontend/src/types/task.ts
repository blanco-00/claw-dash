// Task类型定义（扩展）
export interface Task {
  id: string
  type: string
  payload: Record<string, any>
  priority: 'high' | 'medium' | 'low'
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'DEAD' | 'NEEDS_INTERVENTION'
  createdAt: string
  startedAt?: string
  completedAt?: string
  retryCount: number
  maxRetries?: number
  error?: string
  result?: string
  assignedAgent?: string
  
  // 任务组相关
  groupId?: string
  taskGroupId?: string
  dependsOn?: string[]
  orderIndex?: number
  blocking?: 'interactive' | 'background'
  context?: TaskContext
}

// 任务上下文
export interface TaskContext {
  totalGoal?: string
  overallDesign?: string
  subtaskDescription?: string
}

export interface TaskCounts {
  pending: number
  running: number
  completed: number
  failed: number
  dead: number
  total: number
}

// 任务组类型
export interface TaskGroup {
  id: string
  name: string
  title?: string
  description?: string
  tasks?: Task[]
  status: TaskGroupStatus
  createdAt: string
  completedAt?: string
  totalGoal?: string
  overallDesign?: string
  decomposerAgentId?: string
  
  // 统计
  totalTasks?: number
  completedTasks?: number
  failedTasks?: number
  needsInterventionTasks?: number
  progress?: number
}

export type TaskGroupStatus = 'pending' | 'in_progress' | 'completed' | 'failed'

// 依赖关系
export interface TaskDependency {
  taskId: string
  dependsOn: string[]
  blockedBy: string[]
  canExecute: boolean
  isBlocked: boolean
}

// 任务状态流转
export interface TaskTransition {
  from: Task['status']
  to: Task['status']
  timestamp: string
}

// 任务组分页响应
export interface TaskGroupPageResponse {
  content: TaskGroup[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// 任务组统计
export interface TaskGroupStats {
  inProgress: number
  needsIntervention: number
  completed: number
  failed: number
}
