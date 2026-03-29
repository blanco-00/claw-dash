import request from '@/utils/request'
import type {
  TaskQueueTask,
  CreateTaskRequest,
  TaskPageResponse,
  TaskStatus,
  TaskPriority,
  NotifyAgentRequest,
  AgentStats
} from '@/types/agentGraph'

export function createTask(data: CreateTaskRequest) {
  return request.post<{ data: TaskQueueTask }>('/api/task-queue/tasks', data)
}

export function listTasks(
  page: number = 0,
  size: number = 20,
  status?: string,
  sortBy: string = 'createdAt',
  ascending: boolean = false
) {
  return request.get<{ data: TaskPageResponse }>('/api/task-queue/tasks', {
    params: { page, size, status, sortBy, ascending }
  })
}

export function getTask(id: number) {
  return request.get<{ data: TaskQueueTask }>(`/api/task-queue/tasks/${id}`)
}

export function claimTask(taskId: string, workerId: string = 'worker-1') {
  return request.post<{ data: TaskQueueTask }>(`/api/task-queue/tasks/${taskId}/claim`, { workerId })
}

export function completeTask(taskId: string, result?: string) {
  return request.post<{ code: number }>(`/api/task-queue/tasks/${taskId}/complete`, { result })
}

export function failTask(taskId: string, error: string) {
  return request.post<{ code: number }>(`/api/task-queue/tasks/${taskId}/fail`, { error })
}

export function getTaskStats() {
  return request.get<{ data: any }>('/api/task-queue/stats')
}

export function deleteTask(taskId: string) {
  return request.delete<{ code: number }>(`/api/task-queue/tasks/${taskId}`)
}

export function notifyAgent(data: NotifyAgentRequest) {
  return request.post('/api/task-queue/notify-agent', data)
}

export function getAgentStats(agentId: string) {
  return request.get<{ data: AgentStats }>('/api/task-queue/agent-stats', { params: { agentId } })
}
