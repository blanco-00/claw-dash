import { API_BASE } from './config'

async function fetchAPI(url: string) {
  const response = await fetch(url)
  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`)
  }
  return response.json()
}

export interface DashboardOverview {
  totalTasks: number
  pendingTasks: number
  processingTasks: number
  completedTasks: number
  failedTasks: number
  totalAgents: number
  activeAgents: number
  activeCronJobs: number
  taskGroups: number
  timestamp: string
}

export interface DashboardStats {
  tasksToday: number
  tasksThisWeek: number
  tasksThisMonth: number
  successRate: {
    total: number
    completed: number
    failed: number
    rate: number
  }
}

export interface SystemInfo {
  osName: string
  osVersion: string
  osArch: string
  availableProcessors: number
  systemLoadAverage: number
  cpuUsage: number
  memoryTotal: number
  memoryUsed: number
  memoryFree: number
  memoryMax: number
  memoryUsagePercent: number
  jvmUptime: number
  jvmUptimeFormatted: string
  jvmName: string
  jvmVersion: string
  jvmVendor: string
  threadCount: number
  peakThreadCount: number
  daemonThreadCount: number
  gcCount: number
  gcTime: number
  gcCountPerMinute: number
  openclawDir: string
  openclawDirExists: boolean
  openclawDirSize: number
  timestamp: string
}

export async function getDashboardOverview(): Promise<DashboardOverview> {
  try {
    const result = await fetchAPI(`${API_BASE}/api/dashboard/overview`)
    return result.data
  } catch (error) {
    console.error('获取Dashboard概览失败:', error)
    return {
      totalTasks: 0,
      pendingTasks: 0,
      processingTasks: 0,
      completedTasks: 0,
      failedTasks: 0,
      totalAgents: 0,
      activeAgents: 0,
      activeCronJobs: 0,
      taskGroups: 0,
      timestamp: ''
    }
  }
}

export async function getDashboardStats(): Promise<DashboardStats> {
  try {
    const result = await fetchAPI(`${API_BASE}/api/dashboard/stats`)
    return result.data
  } catch (error) {
    console.error('获取Dashboard统计失败:', error)
    return {
      tasksToday: 0,
      tasksThisWeek: 0,
      tasksThisMonth: 0,
      successRate: { total: 0, completed: 0, failed: 0, rate: 0 }
    }
  }
}

export async function getSystemInfo(): Promise<SystemInfo> {
  try {
    const result = await fetchAPI(`${API_BASE}/api/dashboard/system-info`)
    return result.data
  } catch (error) {
    console.error('获取系统信息失败:', error)
    return {
      osName: '',
      osVersion: '',
      osArch: '',
      availableProcessors: 0,
      systemLoadAverage: 0,
      cpuUsage: 0,
      memoryTotal: 0,
      memoryUsed: 0,
      memoryFree: 0,
      memoryMax: 0,
      memoryUsagePercent: 0,
      jvmUptime: 0,
      jvmUptimeFormatted: '',
      jvmName: '',
      jvmVersion: '',
      jvmVendor: '',
      threadCount: 0,
      peakThreadCount: 0,
      daemonThreadCount: 0,
      gcCount: 0,
      gcTime: 0,
      gcCountPerMinute: 0,
      openclawDir: '',
      openclawDirExists: false,
      openclawDirSize: 0,
      timestamp: ''
    }
  }
}

export default {
  getDashboardOverview,
  getDashboardStats,
  getSystemInfo
}
