import axios from 'axios'

const api = axios.create({
  baseURL: '/api'
})

export const settingsApi = {
  getGlobalSetting: (key: string) => 
    api.get(`/settings/global/${key}`).then(r => r.data),
  setGlobalSetting: (key: string, value: string) =>
    api.put(`/settings/global/${key}`, { value }).then(r => r.data),
  getGraphSetting: (graphId: number, key: string) =>
    api.get(`/settings/graph/${graphId}/${key}`).then(r => r.data),
  setGraphSetting: (graphId: number, key: string, value: string) =>
    api.put(`/settings/graph/${graphId}/${key}`, { value }).then(r => r.data),
}
