import { execSync } from 'child_process'
import type { GatewayInfo } from '@/types/gateway'

const OPENCLAW_PATH = '/Users/hannah/.npm-global/bin/openclaw'

/**
 * 获取Gateway状态
 */
export function getGatewayStatus(): GatewayInfo {
  try {
    const output = execSync(`${OPENCLAW_PATH} status`, {
      encoding: 'utf-8',
      timeout: 10000
    })

    return parseGatewayOutput(output)
  } catch (error) {
    console.error('获取Gateway状态失败:', error)
    return {
      status: 'error',
      version: 'unknown'
    }
  }
}

/**
 * 解析openclaw status输出
 */
function parseGatewayOutput(output: string): GatewayInfo {
  const lines = output.split('\n')
  
  const info: GatewayInfo = {
    status: 'stopped'
  }

  // 解析状态
  const statusMatch = output.match(/Gateway service.*?(running|stopped|loaded)/i)
  if (statusMatch) {
    info.status = statusMatch[1] === 'running' ? 'running' : 'stopped'
  }

  // 解析PID
  const pidMatch = output.match(/pid (\d+)/)
  if (pidMatch) {
    info.pid = parseInt(pidMatch[1], 10)
  }

  // 解析版本
  const versionMatch = output.match(/Channel.*?stable.*?\(default\)(.*?)·/s)
  // 尝试其他方式获取版本
  const nodeMatch = output.match(/node (v[\d.]+)/)
  if (nodeMatch) {
    info.version = nodeMatch[1]
  }

  // 解析Dashboard端口
  const portMatch = output.match(/Dashboard.*?http.*?:(\d+)/)
  if (portMatch) {
    info.port = parseInt(portMatch[1], 10)
  }

  // 解析运行时长
  const uptimeMatch = output.match(/Gateway service.*?loaded.*?\(([^)]+)\)/i)
  if (uptimeMatch) {
    info.uptime = uptimeMatch[1]
  }

  return info
}

/**
 * 检查Gateway是否运行
 */
export function isGatewayRunning(): boolean {
  try {
    const output = execSync(`${OPENCLAW_PATH} status`, {
      encoding: 'utf-8',
      timeout: 5000
    })
    return output.includes('running')
  } catch {
    return false
  }
}

export default {
  getGatewayStatus,
  isGatewayRunning
}
