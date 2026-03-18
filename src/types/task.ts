// Task类型定义
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
}

export interface TaskCounts {
  pending: number
  running: number
  completed: number
  failed: number
  dead: number
  total: number
}
