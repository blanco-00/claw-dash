import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const a2aMessageApi = {
  getMessages: (params?: { from?: string; to?: string; since?: string }) =>
    api.get('/a2a/messages', { params }).then(r => r.data),
  send: (data: { fromAgentId: string; toAgentId: string; content: string; type?: string }) =>
    api.post('/a2a/send', data).then(r => r.data),
  getStats: (agentId: string) => api.get(`/a2a/stats/${agentId}`).then(r => r.data),
}
