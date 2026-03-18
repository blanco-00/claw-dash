const API_BASE = 'http://localhost:3001'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

/**
 * 获取Cron任务列表
 */
export async function getCronTasks() {
  try {
    return await fetchAPI(`${API_BASE}/api/cron`)
  } catch (error) {
    console.error('获取Cron任务失败:', error)
    return []
  }
}

/**
 * 获取Cron任务统计
 */
export async function getCronStats() {
  const tasks = await getCronTasks()
  return {
    total: tasks.length,
    enabled: tasks.filter((t: any) => t.enabled).length,
    disabled: tasks.filter((t: any) => !t.enabled).length,
    errors: tasks.filter((t: any) => t.status === 'error').length
  }
}

export default {
  getCronTasks,
  getCronStats
}
