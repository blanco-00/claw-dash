import type { CronTask, CronJob } from '@/types/cron'

/**
 * 获取Cron任务列表（模拟数据）
 */
export function getCronTasks(): CronTask[] {
  // 返回模拟数据
  return [
    { id: 'health-check', name: '健康检查', agent: 'gongbu', schedule: '*/30 * * * *', status: 'ok', enabled: true, lastRun: new Date().toISOString(), nextRun: new Date(Date.now() + 30*60000).toISOString() },
    { id: 'task-poll', name: '任务轮询', agent: 'jishu', schedule: '*/10 * * * *', status: 'ok', enabled: true, lastRun: new Date().toISOString(), nextRun: new Date(Date.now() + 10*60000).toISOString() },
    { id: 'memory-cleanup', name: '内存清理', agent: 'gongbu', schedule: '0 2 * * *', status: 'idle', enabled: false, lastRun: new Date().toISOString() },
    { id: 'stats-report', name: '统计报告', agent: 'hubu', schedule: '0 8 * * *', status: 'ok', enabled: true, lastRun: new Date().toISOString(), nextRun: new Date(Date.now() + 16*3600000).toISOString() }
  ]
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
