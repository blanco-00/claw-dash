import type { SessionInfo, SessionStats } from '@/types/session'

/**
 * 获取会话列表（模拟数据）
 */
export async function getSessions(): Promise<SessionInfo[]> {
  // 返回模拟数据
  return [
    { key: 'agent:jishu:main', agentId: 'jishu', kind: 'direct', age: 'just now', model: 'MiniMax-M2.5', tokens: '42k/200k', lastActive: new Date() },
    { key: 'agent:main:main', agentId: 'main', kind: 'direct', age: '1m ago', model: 'MiniMax-M2.5', tokens: '74k/200k', lastActive: new Date() },
    { key: 'agent:gongbu:cron:xxx', agentId: 'gongbu', kind: 'direct', age: '12m ago', model: 'MiniMax-M2.5', tokens: '18k/200k', lastActive: new Date() },
    { key: 'agent:jinyiwei:cron:yyy', agentId: 'jinyiwei', kind: 'direct', age: '17m ago', model: 'MiniMax-M2.5', tokens: '23k/200k', lastActive: new Date() },
    { key: 'agent:shangshiju:cron:zzz', agentId: 'shangshiju', kind: 'direct', age: '18m ago', model: 'MiniMax-M2.5', tokens: '19k/200k', lastActive: new Date() }
  ]
}

/**
 * 获取会话统计
 */
export async function getSessionStats(): Promise<SessionStats> {
  const sessions = await getSessions()
  
  const byAgent: Record<string, number> = {}
  sessions.forEach(s => {
    byAgent[s.agentId] = (byAgent[s.agentId] || 0) + 1
  })

  return {
    total: sessions.length,
    active: sessions.length,
    byAgent
  }
}

export default {
  getSessions,
  getSessionStats
}
