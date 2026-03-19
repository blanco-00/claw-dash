// Task类型定义（扩展）
export interface Task {
  id: string
  type: string
  payload: Record<string, any>
  priority: 'high' | 'medium' | 'low'
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'DEAD'
  createdAt: string
  startedAt?: string
  completedAt?: string
  retryCount: number
  error?: string
  result?: string
  
  // 任务组相关
  groupId?: string
  dependsOn?: string[]
  orderIndex?: number
  blocking?: 'interactive' | 'background'
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
  description?: string
  tasks: Task[]
  status: 'pending' | 'running' | 'completed' | 'failed'
  createdAt: string
  completedAt?: string
  
  // 统计
  totalTasks: number
  completedTasks: number
  progress: number
}

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
