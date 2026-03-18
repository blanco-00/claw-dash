// Cron任务类型定义
export interface CronTask {
  id: string
  name: string
  agent: string
  schedule: string
  status: 'ok' | 'error' | 'idle' | 'running'
  nextRun?: string
  lastRun?: string
  enabled: boolean
}

export interface CronJob {
  name: string
  agentId: string
  schedule: {
    kind: 'cron' | 'every' | 'at'
    expr?: string
    everyMs?: number
    at?: string
  }
  payload: {
    kind: 'systemEvent' | 'agentTurn'
    text?: string
    message?: string
    model?: string
  }
  enabled: boolean
}
