import { readdirSync, readFileSync, existsSync, statSync } from 'fs'
import { join } from 'path'
import type { CronTask, CronJob } from '@/types/cron'

const OPENCLAW_HOME = '/Users/hannah/.openclaw'
const CRON_DIR = join(OPENCLAW_HOME, 'cron')

/**
 * 获取Cron任务列表
 */
export function getCronTasks(): CronTask[] {
  try {
    if (!existsSync(CRON_DIR)) {
      return []
    }

    const files = readdirSync(CRON_DIR).filter(f => f.endsWith('.json'))
    
    return files.map(file => {
      const filePath = join(CRON_DIR, file)
      const content = readFileSync(filePath, 'utf-8')
      const job = JSON.parse(content) as CronJob
      
      const stat = statSync(filePath)
      const mtime = stat.mtime.toISOString()
      
      return {
        id: file.replace('.json', ''),
        name: job.name || file.replace('.json', ''),
        agent: job.agentId || 'unknown',
        schedule: parseSchedule(job.schedule),
        status: job.enabled ? 'ok' : 'idle',
        enabled: job.enabled,
        lastRun: mtime,
        nextRun: calculateNextRun(job.schedule)
      }
    })
  } catch (error) {
    console.error('获取Cron任务列表失败:', error)
    return []
  }
}

/**
 * 解析调度表达式
 */
function parseSchedule(schedule: CronJob['schedule']): string {
  if (!schedule) return 'unknown'
  
  switch (schedule.kind) {
    case 'cron':
      return schedule.expr || '* * * * *'
    case 'every':
      if (schedule.everyMs) {
        const minutes = Math.floor(schedule.everyMs / 60000)
        return `*/${minutes} * * * *`
      }
      return 'every'
    case 'at':
      return schedule.at || 'once'
    default:
      return 'unknown'
  }
}

/**
 * 计算下次执行时间（简单估算）
 */
function calculateNextRun(schedule: CronJob['schedule']): string | undefined {
  if (!schedule || !schedule.enabled) return undefined
  
  const now = new Date()
  
  switch (schedule.kind) {
    case 'every':
      if (schedule.everyMs) {
        return new Date(now.getTime() + schedule.everyMs).toISOString()
      }
      break
    case 'at':
      if (schedule.at) {
        return schedule.at
      }
      break
    case 'cron':
      // 简单处理：返回当前时间+1小时作为估算
      return new Date(now.getTime() + 3600000).toISOString()
  }
  
  return undefined
}

/**
 * 获取Cron任务统计
 */
export function getCronStats(): { total: number; enabled: number; disabled: number; errors: number } {
  const tasks = getCronTasks()
  return {
    total: tasks.length,
    enabled: tasks.filter(t => t.enabled).length,
    disabled: tasks.filter(t => !t.enabled).length,
    errors: 0
  }
}

export default {
  getCronTasks,
  getCronStats
}
