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
  return request.get<OpenClawStatus>('/openclaw/status')
}

export function installOpenClaw() {
  return request.post('/openclaw/install')
}

export function uninstallOpenClaw() {
  return request.post('/openclaw/uninstall')
}

export function getOpenClawConfig() {
  return request.get<OpenClawConfig>('/openclaw/config')
}

export function updateOpenClawConfig(config: OpenClawConfig) {
  return request.put('/openclaw/config', config)
}

export function getOpenClawPlugins() {
  return request.get<OpenClawPlugins>('/openclaw/plugins')
}

export function togglePlugin(pluginId: string) {
  return request.post(`/openclaw/plugins/${pluginId}/toggle`)
}
