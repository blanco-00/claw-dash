import { query, queryOne } from './database'
import type { Task, TaskCounts } from '@/types/task'

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
  const result = queryOne<{ status: string; count: number }>(
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
  
  if (result) {
    const rows = query<{ status: string; count: number }>(
      `SELECT status, COUNT(*) as count FROM ${TABLE_NAME} GROUP BY status`
    )
    rows.forEach(row => {
      const key = row.status.toLowerCase() as keyof TaskCounts
      if (key in counts) {
        counts[key] = row.count
      }
    })
    counts.total = rows.reduce((sum, r) => sum + r.count, 0)
  }
  
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

export default {
  listTasks,
  getTask,
  getTaskCounts,
  getTasksByType,
  getPendingTasks
}
