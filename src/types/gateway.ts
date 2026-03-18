// Gateway类型定义
export interface GatewayStatus {
  running: boolean
  pid?: number
  version?: string
  uptime?: number
  startTime?: string
  ports?: {
    gateway: number
    websocket: number
  }
  memory?: number
}

export interface GatewayInfo {
  status: 'running' | 'stopped' | 'error'
  pid?: number
  version?: string
  uptime?: string
  memory?: string
  port?: number
}
