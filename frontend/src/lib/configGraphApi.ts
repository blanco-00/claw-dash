import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const configGraphApi = {
  list: () => api.get('/config-graphs').then(r => r.data),
  create: (data: { name?: string; description?: string }) => api.post('/config-graphs', data).then(r => r.data),
  get: (id: number) => api.get(`/config-graphs/${id}`).then(r => r.data),
  update: (id: number, data: { name?: string; description?: string }) => api.put(`/config-graphs/${id}`, data).then(r => r.data),
  delete: (id: number) => api.delete(`/config-graphs/${id}`).then(r => r.data),
  
  // Nodes
  addNode: (graphId: number, data: { agentId: string; x: number; y: number }) => 
    api.post(`/config-graphs/${graphId}/nodes`, data).then(r => r.data),
  removeNode: (graphId: number, agentId: string) => 
    api.delete(`/config-graphs/${graphId}/nodes/${agentId}`).then(r => r.data),
  updateNodePosition: (graphId: number, agentId: string, data: { x: number; y: number }) => 
    api.put(`/config-graphs/${graphId}/nodes/${agentId}/position`, data).then(r => r.data),
  updateAllNodePositions: (graphId: number, positions: Array<{ id: string; x: number; y: number }>) => 
    api.put(`/config-graphs/${graphId}/nodes/positions`, positions).then(r => r.data),
  
  // Edges
  addEdge: (graphId: number, data: { sourceId: string; targetId: string; edgeType: string; label?: string }) =>
    api.post(`/config-graphs/${graphId}/edges`, data).then(r => r.data),
  updateEdge: (graphId: number, edgeId: number, data: { enabled?: boolean; label?: string }) =>
    api.put(`/config-graphs/${graphId}/edges/${edgeId}`, data).then(r => r.data),
  removeEdge: (graphId: number, edgeId: number) =>
    api.delete(`/config-graphs/${graphId}/edges/${edgeId}`).then(r => r.data),
  
  // Sync
  sync: (graphId: number) => api.post(`/config-graphs/${graphId}/sync`).then(r => r.data),
}
