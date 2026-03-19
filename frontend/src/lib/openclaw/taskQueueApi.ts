import request from '@/utils/request'
import type {
  TaskQueueTask,
  CreateTaskRequest,
  TaskPageResponse,
  TaskStatus,
  TaskPriority
} from '@/types/agentGraph'

export function createTask(data: CreateTaskRequest) {
  return request.post<{ data: TaskQueueTask }>('/task-queue/tasks', data)
}

export function listTasks(
  page: number = 0,
  size: number = 20,
  status?: string,
  sortBy: string = 'createdAt',
  ascending: boolean = false
) {
  return request.get<{ data: TaskPageResponse }>('/task-queue/tasks', {
    params: { page, size, status, sortBy, ascending }
  })
}

export function getTask(id: number) {
  return request.get<{ data: TaskQueueTask }>(`/task-queue/tasks/${id}`)
}

export function claimTask(taskId: string, workerId: string = 'worker-1') {
  return request.post<{ data: TaskQueueTask }>(`/task-queue/tasks/${taskId}/claim`, { workerId })
}

export function completeTask(taskId: string, result?: string) {
  return request.post<{ code: number }>(`/task-queue/tasks/${taskId}/complete`, { result })
}

export function failTask(taskId: string, error: string) {
  return request.post<{ code: number }>(`/task-queue/tasks/${taskId}/fail`, { error })
}

export function getTaskStats() {
  return request.get<{ data: any }>('/task-queue/stats')
}
