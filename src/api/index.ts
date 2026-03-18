import { getGatewayStatus } from '@/api/gateway'
import { getAgentList, getAllAgentDetails } from '@/api/agents'
import { getCronTasks, getCronStats } from '@/api/cron'
import { getSessions, getSessionStats } from '@/api/sessions'
import { getTaskCounts, listTasks } from '@/api/tasks'
import { cache } from '@/utils/cache'

// 缓存TTL配置
const CACHE_TTL = {
  gateway: 10000, // 10秒
  agents: 30000, // 30秒
  cron: 30000, // 30秒
  sessions: 15000, // 15秒
  tasks: 10000 // 10秒
}

export interface DashboardData {
  gateway: ReturnType<typeof getGatewayStatus>
  agents: ReturnType<typeof getAgentList>
  cronTasks: ReturnType<typeof getCronTasks>
  sessions: ReturnType<typeof getSessions>
  taskCounts: ReturnType<typeof getTaskCounts>
}

/**
 * 刷新所有数据
 */
export async function refreshAllData(): Promise<DashboardData> {
  const [gateway, agents, cronTasks, sessions, taskCounts] = await Promise.all([
    refreshGateway(),
    refreshAgents(),
    refreshCronTasks(),
    refreshSessions(),
    refreshTaskCounts()
  ])

  return {
    gateway,
    agents,
    cronTasks,
    sessions,
    taskCounts
  }
}

/**
 * 刷新Gateway数据
 */
export async function refreshGateway() {
  const cached = cache.get('gateway')
  if (cached) return cached
  return await getGatewayStatus()
}

/**
 * 刷新Agent数据
 */
export async function refreshAgents() {
  const cached = cache.get('agents')
  if (cached) return cached
  return await getAgentList()
}

/**
 * 刷新Cron数据
 */
export async function refreshCronTasks() {
  const cached = cache.get('cronTasks')
  if (cached) return cached
  return await getCronTasks()
}

/**
 * 刷新Sessions数据
 */
export async function refreshSessions() {
  const cached = cache.get('sessions')
  if (cached) return cached
  return await getSessions()
}

/**
 * 刷新Task数据
 */
export async function refreshTaskCounts() {
  const cached = cache.get('taskCounts')
  if (cached) return cached
  return await getTaskCounts()
}

/**
 * 设置缓存
 */
export function setCache() {
  // Gateway
  cache.set('gateway', getGatewayStatus(), CACHE_TTL.gateway)

  // Agents
  cache.set('agents', getAgentList(), CACHE_TTL.agents)

  // Cron
  cache.set('cronTasks', getCronTasks(), CACHE_TTL.cron)

  // Sessions
  getSessions().then(sessions => {
    cache.set('sessions', sessions, CACHE_TTL.sessions)
  })

  // Tasks
  cache.set('taskCounts', getTaskCounts(), CACHE_TTL.tasks)
}

/**
 * 清除所有缓存
 */
export function clearAllCache(): void {
  cache.clear()
}

export default {
  refreshAllData,
  refreshGateway,
  refreshAgents,
  refreshCronTasks,
  refreshSessions,
  refreshTaskCounts,
  setCache,
  clearAllCache
}
