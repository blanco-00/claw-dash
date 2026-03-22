import axios from 'axios'
import type { EdgeRoutingType, DecisionMode, SyncPreviewResult, SyncResult } from '@/types/agentGraph'

const api = axios.create({
  baseURL: '/api'
})

export const configGraphApi = {
  list: () => api.get('/config-graphs').then(r => r.data),
  create: (data: { name?: string; description?: string }) => api.post('/config-graphs', data).then(r => r.data),
  get: (id: number) => api.get(`/config-graphs/${id}`).then(r => r.data),
  update: (id: number, data: { name?: string; description?: string }) => api.put(`/config-graphs/${id}`, data).then(r => r.data),
  delete: (id: number) => api.delete(`/config-graphs/${id}`).then(r => r.data),
  
  addNode: (graphId: number, data: { agentId: string; x: number; y: number }) => 
    api.post(`/config-graphs/${graphId}/nodes`, data).then(r => r.data),
  removeNode: (graphId: number, agentId: string) => 
    api.delete(`/config-graphs/${graphId}/nodes/${agentId}`).then(r => r.data),
  updateNodePosition: (graphId: number, agentId: string, data: { x: number; y: number }) => 
    api.put(`/config-graphs/${graphId}/nodes/${agentId}/position`, data).then(r => r.data),
  updateAllNodePositions: (graphId: number, positions: Array<{ id: string; x: number; y: number }>) => 
    api.put(`/config-graphs/${graphId}/nodes/positions`, positions).then(r => r.data),
  
  addEdge: (graphId: number, data: { sourceId: string; targetId: string; edgeType: string; label?: string }) =>
    api.post(`/config-graphs/${graphId}/edges`, data).then(r => r.data),
  updateEdge: (graphId: number, edgeId: number, data: { 
    enabled?: boolean
    label?: string
    edgeRoutingType?: EdgeRoutingType
    decisionMode?: DecisionMode
    messageTemplate?: string
  }) =>
    api.put(`/config-graphs/${graphId}/edges/${edgeId}`, data).then(r => r.data),
  removeEdge: (graphId: number, edgeId: number) =>
    api.delete(`/config-graphs/${graphId}/edges/${edgeId}`).then(r => r.data),
  
  getEdgeTypes: () => api.get('/edge-types').then(r => r.data),
  createEdgeType: (data: { value: string; name: string; description?: string; defaultMessageTemplate?: string }) =>
    api.post('/edge-types', data).then(r => r.data),
  updateEdgeType: (id: number, data: { name?: string; description?: string; defaultMessageTemplate?: string }) =>
    api.patch(`/edge-types/${id}`, data).then(r => r.data),
  deleteEdgeType: (id: number) =>
    api.delete(`/edge-types/${id}`).then(r => r.data),

  syncPreview: (graphId: number, edgeId?: number): Promise<{ data: SyncPreviewResult }> =>
    api.get(`/config-graphs/${graphId}/sync-preview`, { params: edgeId ? { edgeId } : {} }).then(r => r.data),
  sync: (graphId: number): Promise<{ data: SyncResult }> =>
    api.post(`/config-graphs/${graphId}/sync`).then(r => r.data),

  getA2AConfig: () => api.get('/openclaw/a2a-config').then(r => r.data),
  syncA2AConfig: (graphId: number) => api.post(`/openclaw/a2a-config/sync?graphId=${graphId}`).then(r => r.data),
}
