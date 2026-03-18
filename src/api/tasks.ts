import type { Task, TaskCounts, TaskGroup, TaskDependency } from '@/types/task'

/**
 * 获取任务列表（模拟数据）
 */
export function listTasks(limit = 50, status?: string): Task[] {
  const tasks: Task[] = [
    { id: '1', type: 'jishu-dev', payload: {}, priority: 'high', status: 'PENDING', createdAt: new Date().toISOString(), retryCount: 0, groupId: 'group-1', dependsOn: [], orderIndex: 1 },
    { id: '2', type: 'jishu-dev', payload: {}, priority: 'medium', status: 'RUNNING', createdAt: new Date().toISOString(), startedAt: new Date().toISOString(), retryCount: 0, groupId: 'group-1', dependsOn: ['1'], orderIndex: 2 },
    { id: '3', type: 'jishu-dev', payload: {}, priority: 'low', status: 'COMPLETED', createdAt: new Date().toISOString(), completedAt: new Date().toISOString(), retryCount: 0, groupId: 'group-1', dependsOn: ['2'], orderIndex: 3 },
    { id: '4', type: 'system-task', payload: {}, priority: 'high', status: 'FAILED', createdAt: new Date().toISOString(), completedAt: new Date().toISOString(), retryCount: 2, error: 'Task timeout', groupId: 'group-2', dependsOn: [], orderIndex: 1 },
    { id: '5', type: 'jishu-dev', payload: {}, priority: 'medium', status: 'PENDING', createdAt: new Date().toISOString(), retryCount: 0 }
  ]
  
  if (status) {
    return tasks.filter(t => t.status === status).slice(0, limit)
  }
  return tasks.slice(0, limit)
}

/**
 * 获取任务详情
 */
export function getTask(id: string): Task | undefined {
  return listTasks(100).find(t => t.id === id)
}

/**
 * 获取任务统计
 */
export function getTaskCounts(): TaskCounts {
  const tasks = listTasks(100)
  return {
    pending: tasks.filter(t => t.status === 'PENDING').length,
    running: tasks.filter(t => t.status === 'RUNNING').length,
    completed: tasks.filter(t => t.status === 'COMPLETED').length,
    failed: tasks.filter(t => t.status === 'FAILED').length,
    dead: 0,
    total: tasks.length
  }
}

/**
 * 按类型获取任务
 */
export function getTasksByType(type: string, limit = 20): Task[] {
  return listTasks(limit).filter(t => t.type === type)
}

/**
 * 获取待处理任务
 */
export function getPendingTasks(limit = 10): Task[] {
  return listTasks(limit, 'PENDING')
}

// ========== 任务组相关 ==========

/**
 * 获取所有任务组
 */
export function getTaskGroups(): TaskGroup[] {
  const tasks = listTasks(100)
  
  const groups = new Map<string, Task[]>()
  const noGroup: Task[] = []
  
  tasks.forEach(task => {
    if (task.groupId) {
      const group = groups.get(task.groupId) || []
      group.push(task)
      groups.set(task.groupId, group)
    } else {
      noGroup.push(task)
    }
  })
  
  const result: TaskGroup[] = []
  
  groups.forEach((groupTasks, groupId) => {
    const completed = groupTasks.filter(t => t.status === 'COMPLETED').length
    const status = completed === groupTasks.length ? 'completed' 
      : groupTasks.some(t => t.status === 'RUNNING') ? 'running'
      : groupTasks.some(t => t.status === 'FAILED') ? 'failed'
      : 'pending'
    
    result.push({
      id: groupId,
      name: `任务组 ${groupId.slice(0, 8)}`,
      tasks: groupTasks.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0)),
      status,
      createdAt: groupTasks[0]?.createdAt || '',
      totalTasks: groupTasks.length,
      completedTasks: completed,
      progress: Math.round((completed / groupTasks.length) * 100)
    })
  })
  
  return result
}

/**
 * 获取单个任务组详情
 */
export function getTaskGroupDetail(groupId: string): TaskGroup | null {
  const groups = getTaskGroups()
  return groups.find(g => g.id === groupId) || null
}

/**
 * 获取任务依赖信息
 */
export function getTaskDependencies(taskId: string): TaskDependency | null {
  const task = getTask(taskId)
  if (!task) return null
  
  const dependsOn = task.dependsOn || []
  const blockedBy: string[] = []
  let canExecute = true
  let isBlocked = false
  
  dependsOn.forEach(depId => {
    const depTask = getTask(depId)
    if (depTask && depTask.status !== 'COMPLETED') {
      blockedBy.push(depId)
      canExecute = false
      isBlocked = true
    }
  })
  
  return {
    taskId,
    dependsOn,
    blockedBy,
    canExecute,
    isBlocked
  }
}

/**
 * 检测循环依赖
 */
export function detectCircularDependencies(_tasks: Task[]): string[] | null {
  return null
}

/**
 * 获取可执行任务
 */
export function getExecutableTasks(): Task[] {
  return listTasks(100).filter(t => t.status === 'PENDING' && (!t.dependsOn || t.dependsOn.length === 0))
}

export default {
  listTasks,
  getTask,
  getTaskCounts,
  getTasksByType,
  getPendingTasks,
  getTaskGroups,
  getTaskGroupDetail,
  getTaskDependencies,
  detectCircularDependencies,
  getExecutableTasks
}
