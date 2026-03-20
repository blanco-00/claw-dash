import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const openclawAgentApi = {
  list: () => api.get('/openclaw/agents').then(r => r.data),
  add: (data: { name: string; workspace: string }) => api.post('/openclaw/agents', data).then(r => r.data),
  delete: (name: string) => api.delete(`/openclaw/agents/${name}`).then(r => r.data),
  bind: (name: string, channel: string) => api.post(`/openclaw/agents/${name}/bind`, { channel }).then(r => r.data),
  unbind: (name: string, channel: string) => api.post(`/openclaw/agents/${name}/unbind`, { channel }).then(r => r.data),
  getBindings: () => api.get('/openclaw/agents/bindings').then(r => r.data),
    getMain: () => api.get('/openclaw/agents/main').then(r => r.data),
}
