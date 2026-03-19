// Sessions类型定义
export interface SessionInfo {
  key: string
  agentId: string
  kind: 'direct' | 'channel' | 'group'
  age: string
  model: string
  tokens: string
  lastActive: Date
}

export interface SessionStats {
  total: number
  active: number
  byAgent: Record<string, number>
}
