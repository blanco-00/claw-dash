import { API_BASE } from '@/api/config'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

export async function getCurrentRuntimeGraph() {
  try {
    const res = await fetchAPI(`${API_BASE}/api/runtime-graphs/current`)
    if (res.code === 200 || res.code === null) {
      return res.data || { nodes: [], edges: [] }
    }
    throw new Error(res.message || 'Failed to fetch runtime graph')
  } catch (error) {
    console.error('Failed to fetch runtime graph:', error)
    return { nodes: [], edges: [], error: (error as Error).message }
  }
}

export async function getRuntimeGraphHistory(timeRange: string = '1h') {
  try {
    const res = await fetchAPI(`${API_BASE}/api/runtime-graphs/history?timeRange=${timeRange}`)
    if (res.code === 200 || res.code === null) {
      return res.data || {}
    }
    throw new Error(res.message || 'Failed to fetch runtime graph history')
  } catch (error) {
    console.error('Failed to fetch runtime graph history:', error)
    return { error: (error as Error).message }
  }
}

export default {
  getCurrentRuntimeGraph,
  getRuntimeGraphHistory
}
