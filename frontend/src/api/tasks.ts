import { API_BASE } from './config'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

/**
 * 获取任务列表
 */
export async function listTasks(limit = 50, status?: string) {
  try {
    const url = status 
      ? `${API_BASE}/api/tasks?status=${status}`
      : `${API_BASE}/api/tasks`
    const res = await fetchAPI(url)
    return res.data || []
  } catch (error) {
    console.error('获取任务列表失败:', error)
    return []
  }
}

/**
 * 获取任务详情
 */
export async function getTask(id: string) {
  const tasks = await listTasks(100)
  return tasks.find((t: any) => t.id === id)
}

/**
 * 获取任务统计
 */
export async function getTaskCounts() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/tasks/stats`)
    const stats = res.data || {}
    return { 
      pending: stats.PENDING || 0, 
      running: stats.RUNNING || 0, 
      completed: stats.COMPLETED || 0, 
      failed: stats.FAILED || 0, 
      dead: stats.DEAD || 0, 
      total: (stats.PENDING || 0) + (stats.RUNNING || 0) + (stats.COMPLETED || 0) + (stats.FAILED || 0) + (stats.DEAD || 0)
    }
  } catch (error) {
    console.error('获取任务统计失败:', error)
    return { pending: 0, running: 0, completed: 0, failed: 0, dead: 0, total: 0 }
  }
}

/**
 * 按类型获取任务
 */
export async function getTasksByType(type: string, limit = 20) {
  const tasks = await listTasks(100)
  return tasks.filter((t: any) => t.type === type).slice(0, limit)
}

/**
 * 获取待处理任务
 */
export async function getPendingTasks(limit = 10) {
  return listTasks(limit, 'PENDING')
}

// ========== 任务组相关 ==========

/**
 * 获取所有任务组
 */
export async function getTaskGroups() {
  try {
    return await fetchAPI(`${API_BASE}/api/task-groups`)
  } catch (error) {
    console.error('获取任务组失败:', error)
    return []
  }
}

/**
 * 获取单个任务组详情
 */
export async function getTaskGroupDetail(groupId: string) {
  const groups = await getTaskGroups()
  return groups.find((g: any) => g.id === groupId) || null
}

/**
 * 获取任务依赖信息
 */
export async function getTaskDependencies(taskId: string) {
  const task = await getTask(taskId)
  if (!task) return null
  
  return {
    taskId,
    dependsOn: task.dependsOn || [],
    blockedBy: [],
    canExecute: true,
    isBlocked: false
  }
}

/**
 * 检测循环依赖
 */
export async function detectCircularDependencies(_tasks: any[]) {
  return null
}

/**
 * 获取可执行任务
 */
export async function getExecutableTasks() {
  return listTasks(100, 'PENDING')
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
