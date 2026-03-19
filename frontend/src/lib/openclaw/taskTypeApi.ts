import request from '@/utils/request'

export interface TaskType {
  id?: number
  name: string
  displayName: string
  description?: string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export function getTaskTypes() {
  return request.get<{ data: TaskType[] }>('/api/task-types')
}

export function getTaskType(id: number) {
  return request.get<{ data: TaskType }>(`/api/task-types/${id}`)
}

export function createTaskType(data: TaskType) {
  return request.post<{ data: TaskType }>('/api/task-types', data)
}

export function updateTaskType(id: number, data: TaskType) {
  return request.put<{ data: TaskType }>(`/api/task-types/${id}`, data)
}

export function deleteTaskType(id: number) {
  return request.delete<{ code: number }>(`/api/task-types/${id}`)
}
