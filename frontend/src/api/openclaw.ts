import request from '@/utils/request'

export interface OpenClawStatus {
  running: boolean
  apiUrl: string
  error?: string
  timestamp: string
}

export interface OpenClawConfig {
  [key: string]: string
}

export interface OpenClawPlugins {
  available: string[]
  enabled: string[]
}

export function getOpenClawStatus() {
  return request.get<OpenClawStatus>('/api/openclaw/status')
}

export function installOpenClaw() {
  return request.post('/api/openclaw/install')
}

export function uninstallOpenClaw() {
  return request.post('/api/openclaw/uninstall')
}

export function getOpenClawConfig() {
  return request.get<OpenClawConfig>('/api/openclaw/config')
}

export function updateOpenClawConfig(config: OpenClawConfig) {
  return request.put('/api/openclaw/config', config)
}

export function getOpenClawPlugins() {
  return request.get<OpenClawPlugins>('/api/openclaw/plugins')
}

export function togglePlugin(pluginId: string) {
  return request.post(`/api/openclaw/plugins/${pluginId}/toggle`)
}

export interface AutoDetectResult {
  running: boolean
  apiUrl: string
  token?: string
  plugins: Record<string, { enabled: boolean; config?: any }>
  workspaces: string[]
  error?: string
}

export function autoDetectOpenClaw(configPath?: string) {
  return request.post<AutoDetectResult>('/api/openclaw/auto-detect', {
    configPath
  })
}

export function confirmConnect(apiUrl: string, token: string, configPath?: string) {
  return request.post<{ connected: boolean; message: string }>('/api/openclaw/confirm-connect', {
    apiUrl,
    token,
    configPath
  })
}

export function configureMcp(configPath: string, clawdashUrl: string) {
  return request.post<{ success: boolean; message: string; configPath: string }>('/api/openclaw/configure-mcp', {
    configPath,
    clawdashUrl
  })
}
