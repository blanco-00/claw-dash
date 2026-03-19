// Agent类型定义
export interface AgentInfo {
  id: string
  name: string
  title: string
  role: string
  status: 'online' | 'offline' | 'busy'
  workspace?: string
  lastActive?: string
  memory?: {
    soul?: number
    memory?: number
    tasks?: number
  }
}

export interface AgentListItem {
  id: string
  name: string
  title: string
  status: 'online' | 'offline' | 'busy'
  lastActive?: string
}
