import { API_BASE } from './config'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

/**
 * 获取Agent列表
 */
export async function getAgentList() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/agents`)
    const agents = res.data || []
    return agents.map((a: any) => ({
      id: a.id,
      name: a.name,
      title: a.title,
      role: a.role,
      status: a.hasFiles ? 'online' : 'offline'
    }))
  } catch (error) {
    console.error('获取Agent列表失败:', error)
    return []
  }
}

/**
 * 获取Agent详情
 */
export async function getAgentDetail(id: string) {
  const agents = await getAgentList()
  return agents.find((a: any) => a.id === id) || null
}

/**
 * 获取所有Agent详情 - 从OpenClaw
 */
export async function getAllAgentDetails() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/openclaw/agents`)
    const lines = res.data || []
    const agents: any[] = []
    let currentAgent: any = null
    
    for (const line of lines) {
      if (line.startsWith('- ')) {
        if (currentAgent) agents.push(currentAgent)
        const name = line.substring(2).split(' (')[0].trim()
        currentAgent = { id: name, name, title: '', role: 'agent', status: 'online' }
      } else if (line.startsWith('Identity:') && currentAgent) {
        const identity = line.substring('Identity:'.length).trim()
        currentAgent.title = identity.split('(')[0].trim()
      }
    }
    if (currentAgent) agents.push(currentAgent)
    return agents
  } catch (error) {
    console.error('Failed to get agents:', error)
    return []
  }
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails
}
