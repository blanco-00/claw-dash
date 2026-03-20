import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export interface AgentFile {
  content: string
  path: string
}

export const agentFileApi = {
  getFile: (agentId: string, fileName: string) =>
    api.get<any>(`/agents/${agentId}/files/${fileName}`).then(r => r.data),
  
  saveFile: (agentId: string, fileName: string, content: string) =>
    api.put<void>(`/agents/${agentId}/files/${fileName}`, { content }).then(r => r.data),
  
  listFiles: (agentId: string) =>
    api.get<any>(`/agents/${agentId}/files`).then(r => r.data),
}
