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
 * 获取所有Agent详情
 */
export async function getAllAgentDetails() {
  const res = await fetchAPI(`${API_BASE}/api/agents`)
  const agents = res.data || []
  return agents.map((a: any) => ({
    id: a.id,
    name: a.name,
    title: a.title,
    role: a.role,
    status: a.hasFiles ? 'online' : 'offline',
    memory: {
      soul: a.soulSize,
      memory: a.memorySize
    }
  }))
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails
}
