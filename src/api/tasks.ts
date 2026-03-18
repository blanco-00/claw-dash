import { query, queryOne } from './database'
import type { Task, TaskCounts, TaskGroup, TaskDependency } from '@/types/task'

const TABLE_NAME = 'tasks'

/**
 * 获取任务列表
 */
export function listTasks(limit = 50, status?: string): Task[] {
  let sql = `SELECT * FROM ${TABLE_NAME} ORDER BY createdAt DESC LIMIT ?`
  const params: any[] = [limit]
  
  if (status) {
    sql = `SELECT * FROM ${TABLE_NAME} WHERE status = ? ORDER BY createdAt DESC LIMIT ?`
    params.unshift(status)
  }
  
  return query<Task>(sql, params)
}

/**
 * 获取任务详情
 */
export function getTask(id: string): Task | undefined {
  return queryOne<Task>(`SELECT * FROM ${TABLE_NAME} WHERE id = ?`, [id])
}

/**
 * 获取任务统计
 */
export function getTaskCounts(): TaskCounts {
  const rows = query<{ status: string; count: number }>(
    `SELECT status, COUNT(*) as count FROM ${TABLE_NAME} GROUP BY status`
  )
  
  const counts: TaskCounts = {
    pending: 0,
    running: 0,
    completed: 0,
    failed: 0,
    dead: 0,
    total: 0
  }
  
  rows.forEach(row => {
    const key = row.status.toLowerCase() as keyof TaskCounts
    if (key in counts) {
      counts[key] = row.count
    }
  })
  counts.total = rows.reduce((sum, r) => sum + r.count, 0)
  
  return counts
}

/**
 * 按类型获取任务
 */
export function getTasksByType(type: string, limit = 20): Task[] {
  return query<Task>(
    `SELECT * FROM ${TABLE_NAME} WHERE type = ? ORDER BY createdAt DESC LIMIT ?`,
    [type, limit]
  )
}

/**
 * 获取待处理任务
 */
export function getPendingTasks(limit = 10): Task[] {
  return query<Task>(
    `SELECT * FROM ${TABLE_NAME} WHERE status = 'PENDING' ORDER BY createdAt ASC LIMIT ?`,
    [limit]
  )
}

// ========== 任务组相关 ==========

/**
 * 获取所有任务组
 */
export function getTaskGroups(): TaskGroup[] {
  const tasks = query<Task>(`SELECT * FROM ${TABLE_NAME} ORDER BY createdAt DESC`)
  
  // 按groupId分组
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
  
  // 转换为TaskGroup
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
  
  // 处理无组任务
  if (noGroup.length > 0) {
    result.push({
      id: 'ungrouped',
      name: '未分组任务',
      tasks: noGroup,
      status: noGroup.some(t => t.status === 'COMPLETED') ? 'completed' : 'pending',
      createdAt: noGroup[0]?.createdAt || '',
      totalTasks: noGroup.length,
      completedTasks: noGroup.filter(t => t.status === 'COMPLETED').length,
      progress: Math.round((noGroup.filter(t => t.status === 'COMPLETED').length / noGroup.length) * 100)
    })
  }
  
  return result
}

/**
 * 获取单个任务组详情
 */
export function getTaskGroupDetail(groupId: string): TaskGroup | null {
  const tasks = query<Task>(
    `SELECT * FROM ${TABLE_NAME} WHERE groupId = ? ORDER BY orderIndex ASC`,
    [groupId]
  )
  
  if (tasks.length === 0) return null
  
  const completed = tasks.filter(t => t.status === 'COMPLETED').length
  const status = completed === tasks.length ? 'completed' 
    : tasks.some(t => t.status === 'RUNNING') ? 'running'
    : tasks.some(t => t.status === 'FAILED') ? 'failed'
    : 'pending'
  
  return {
    id: groupId,
    name: `任务组 ${groupId.slice(0, 8)}`,
    tasks: tasks.sort((a, b) => (a.orderIndex || 0) - (b.orderIndex || 0)),
    status,
    createdAt: tasks[0]?.createdAt || '',
    totalTasks: tasks.length,
    completedTasks: completed,
    progress: Math.round((completed / tasks.length) * 100)
  }
}

/**
 * 获取任务依赖信息
 */
export function getTaskDependencies(taskId: string): TaskDependency | null {
  const task = getTask(taskId)
  if (!task) return null
  
  // 获取所有依赖任务的状态
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
export function detectCircularDependencies(tasks: Task[]): string[] | null {
  const visited = new Set<string>()
  const recursionStack = new Set<string>()
  const circularTasks: string[] = []
  
  function hasCycle(taskId: string): boolean {
    visited.add(taskId)
    recursionStack.add(taskId)
    
    const task = tasks.find(t => t.id === taskId)
    if (task?.dependsOn) {
      for (const depId of task.dependsOn) {
        if (!visited.has(depId)) {
          if (hasCycle(depId)) {
            circularTasks.push(taskId)
            return true
          }
        } else if (recursionStack.has(depId)) {
          circularTasks.push(taskId)
          return true
        }
      }
    }
    
    recursionStack.delete(taskId)
    return false
  }
  
  for (const task of tasks) {
    if (!visited.has(task.id)) {
      if (hasCycle(task.id)) {
        return circularTasks
      }
    }
  }
  
  return null
}

/**
 * 获取可执行任务（依赖已满足）
 */
export function getExecutableTasks(): Task[] {
  const pendingTasks = query<Task>(
    `SELECT * FROM ${TABLE_NAME} WHERE status = 'PENDING'`
  )
  
  return pendingTasks.filter(task => {
    if (!task.dependsOn || task.dependsOn.length === 0) {
      return true
    }
    
    // 检查所有依赖是否已完成
    return task.dependsOn.every(depId => {
      const depTask = getTask(depId)
      return depTask?.status === 'COMPLETED'
    })
  })
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
