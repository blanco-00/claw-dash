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
    const result = await fetchAPI(`${API_BASE}/api/openclaw/status`)
    // 替换 Docker 内部地址为浏览器可访问的地址
    const data = result.data || {}
    if (data.apiUrl) {
      data.apiUrl = data.apiUrl
        .replace(/host\.docker\.internal/g, 'localhost')
        .replace(/172\.17\.0\.1/g, 'localhost')
    }
    return {
      status: data.running ? 'running' : 'stopped',
      ...data
    }
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
