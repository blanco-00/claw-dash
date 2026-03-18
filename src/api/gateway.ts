import type { GatewayInfo } from '@/types/gateway'

// 注意：这个文件在开发环境使用mock数据
// 生产环境应该通过后端API获取

/**
 * 获取Gateway状态（模拟数据）
 * 注意：实际项目中应该通过后端API获取
 */
export function getGatewayStatus(): GatewayInfo {
  // 开发环境返回模拟数据
  // 实际项目中需要调用后端API
  return {
    status: 'running',
    pid: 34745,
    version: 'v1.0.0',
    uptime: '2小时30分钟',
    port: 18789
  }
}

/**
 * 检查Gateway是否运行（模拟）
 */
export function isGatewayRunning(): boolean {
  return true
}

export default {
  getGatewayStatus,
  isGatewayRunning
}
