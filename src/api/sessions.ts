import { readdirSync, readFileSync, existsSync, statSync } from 'fs'
import { join } from 'path'
import type { SessionInfo, SessionStats } from '@/types/session'

const OPENCLAW_HOME = '/Users/hannah/.openclaw'
const SESSIONS_DIR = join(OPENCLAW_HOME, 'sessions')

/**
 * 获取会话列表（从status命令输出解析）
 */
export async function getSessions(): Promise<SessionInfo[]> {
  try {
    // 使用openclaw status获取会话信息
    const { execSync } = await import('child_process')
    const output = execSync('/Users/hannah/.npm-global/bin/openclaw status', {
      encoding: 'utf-8',
      timeout: 10000
    })

    return parseSessionsFromStatus(output)
  } catch (error) {
    console.error('获取会话列表失败:', error)
    return []
  }
}

/**
 * 从status输出解析会话信息
 */
function parseSessionsFromStatus(output: string): SessionInfo[] {
  const sessions: SessionInfo[] = []
  
  // 找到Sessions表格
  const tableMatch = output.match(/Sessions[\s\S]*?├─.*?┘/m)
  if (!tableMatch) return sessions

  const lines = tableMatch[0].split('\n').filter(line => line.includes('│'))
  
  // 跳过表头
  for (let i = 1; i < lines.length; i++) {
    const line = lines[i].trim()
    if (!line || line.includes('─')) continue

    const parts = line.split('│').map(p => p.trim()).filter(Boolean)
    if (parts.length >= 5) {
      const key = parts[0]
      const agentMatch = key.match(/agent:([^:]+):/)
      
      sessions.push({
        key,
        agentId: agentMatch ? agentMatch[1] : 'unknown',
        kind: parts[1] as SessionInfo['kind'] || 'direct',
        age: parts[2] || '',
        model: parts[3] || '',
        tokens: parts[4] || '',
        lastActive: new Date()
      })
    }
  }

  return sessions
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
