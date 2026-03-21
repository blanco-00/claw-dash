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
 * 获取所有Agent详情 - 从OpenClaw /details 端点
 */
export async function getAllAgentDetails() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/openclaw/agents/details`)
    const agents = res.data || []
    return agents.map((a: any) => ({
      id: a.name,
      name: a.name,
      title: a.title || '',
      role: a.role || 'agent',
      workspace: a.workspace || '',
      status: 'online'
    }))
  } catch (error) {
    console.error('Failed to get agents:', error)
    return []
  }
}

export async function getOrphanedAgents() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/openclaw/agents/orphaned`)
    return res.data || []
  } catch (error) {
    console.error('Failed to get orphaned agents:', error)
    return []
  }
}

export async function cleanupOrphanedAgents() {
  try {
    const res = await fetch(`${API_BASE}/api/openclaw/agents/cleanup`, { method: 'POST' })
    return res.json()
  } catch (error) {
    console.error('Failed to cleanup orphaned agents:', error)
    return { code: 500, message: 'Cleanup failed' }
  }
}

export async function getAgentFileContent(agentName: string, filename: string) {
  try {
    const res = await fetchAPI(`${API_BASE}/api/openclaw/agents/${encodeURIComponent(agentName)}/files/${encodeURIComponent(filename)}`)
    return res
  } catch (error) {
    console.error('Failed to get agent file content:', error)
    return { code: 500, message: 'Failed to get file content' }
  }
}

export async function saveAgentFileContent(agentName: string, filename: string, content: string) {
  try {
    const res = await fetch(`${API_BASE}/api/openclaw/agents/${encodeURIComponent(agentName)}/files/${encodeURIComponent(filename)}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content })
    })
    return res.json()
  } catch (error) {
    console.error('Failed to save agent file:', error)
    return { code: 500, message: 'Failed to save file' }
  }
}

export default {
  getAgentList,
  getAgentDetail,
  getAllAgentDetails,
  getOrphanedAgents,
  cleanupOrphanedAgents,
  getAgentFileContent,
  saveAgentFileContent
}
