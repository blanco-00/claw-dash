const API_BASE = 'http://localhost:3001'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

/**
 * 获取会话列表
 */
export async function getSessions() {
  try {
    const sessions = await fetchAPI(`${API_BASE}/api/sessions`)
    return sessions.map((s: any) => ({
      key: s.key || '',
      agentId: extractAgent(s.key),
      kind: s.kind || 'direct',
      age: s.age || '',
      model: s.model || '',
      tokens: s.tokens || '',
      lastActive: new Date()
    }))
  } catch (error) {
    console.error('获取会话列表失败:', error)
    return []
  }
}

function extractAgent(key: string): string {
  const match = key?.match(/agent:([^:]+)/)
  return match ? match[1] : 'unknown'
}

/**
 * 获取会话统计
 */
export async function getSessionStats() {
  const sessions = await getSessions()
  
  const byAgent: Record<string, number> = {}
  sessions.forEach((s: any) => {
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
