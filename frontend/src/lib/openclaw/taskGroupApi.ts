import request from '@/utils/request'
import type { TaskGroup, TaskGroupPageResponse, TaskGroupStats } from '@/types/task'
import type { TaskQueueTask } from '@/types/agentGraph'

export function listTaskGroups(
  page: number = 0,
  size: number = 20,
  status?: string
) {
  return request.get<{ data: TaskGroupPageResponse }>('/api/task-groups', {
    params: { page, size, status }
  })
}

export function getTaskGroup(id: string) {
  return request.get<{ data: TaskGroup }>(`/api/task-groups/${id}`)
}

export function getTaskGroupDetail(id: string | number) {
  return request.get<{ data: any }>(`/api/task-groups/${id}/detail`)
}

export function getTaskGroupProgress(id: string) {
  return request.get<{ data: { completed: number; total: number; progress: number } }>(`/api/task-groups/${id}/progress`)
}

export function getTasksNeedingIntervention() {
  return request.get<{ data: TaskQueueTask[] }>('/api/tasks/needs-intervention')
}

export function reassignTask(taskId: string, newAssignedAgent: string, reason?: string) {
  return request.patch<{ code: number }>(`/api/tasks/${taskId}/reassign`, {
    newAssignedAgent,
    reason
  })
}

export function abandonTaskGroup(taskGroupId: string) {
  return request.patch<{ code: number }>(`/api/task-groups/${taskGroupId}/status`, {
    status: 'failed'
  })
}

export function getTaskGroupStats() {
  return request.get<{ data: TaskGroupStats }>('/api/task-groups/stats')
}
