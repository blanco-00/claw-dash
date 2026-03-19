import { API_BASE } from './config'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

/**
 * 获取Gateway状态
 */
export async function getGatewayStatus() {
  try {
    return await fetchAPI(`${API_BASE}/api/gateway/status`)
  } catch (error) {
    console.error('获取Gateway状态失败:', error)
    return { status: 'error', error: String(error) }
  }
}

/**
 * 检查Gateway是否运行
 */
export async function isGatewayRunning() {
  const status = await getGatewayStatus()
  return status.status === 'running'
}

export default {
  getGatewayStatus,
  isGatewayRunning
}
