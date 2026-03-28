<template>
  <div 
    class="task-group-card" 
    :class="{ 
      'needs-intervention': hasNeedsIntervention,
      'clickable': true
    }"
    @click="$emit('click')"
  >
    <div class="card-header">
      <div class="header-left">
        <el-tag :type="statusTagType" size="small" class="status-badge">
          {{ statusIcon }} {{ statusText }}
        </el-tag>
        <span class="group-title">{{ group.title || group.name }}</span>
      </div>
      <div class="header-right">
        <el-button text size="small" @click.stop="$emit('click')">
          {{ t('common.view') }} ▼
        </el-button>
      </div>
    </div>

    <div class="subtask-list">
      <div 
        v-for="task in displayedTasks" 
        :key="task.id" 
        class="subtask-item"
        :class="{ 'intervention-required': task.status === 'NEEDS_INTERVENTION' }"
      >
        <span class="agent-badge">[{{ task.assignedAgent || 'unassigned' }}]</span>
        <span class="task-title">{{ task.type }}</span>
        <el-tag 
          :type="getTaskStatusType(task.status)" 
          size="small"
          class="task-status"
        >
          {{ getTaskStatusText(task.status) }}
        </el-tag>
        <span v-if="task.status === 'NEEDS_INTERVENTION'" class="intervention-warning">
          {{ t('taskGroup.intervention.retryExceeded', { count: task.retryCount, max: task.maxRetries }) }}!
        </span>
      </div>
      <div v-if="(group.tasks?.length || 0) > 3" class="more-tasks">
        {{ t('taskGroup.card.more', { count: (group.tasks?.length || 0) - 3 }) }}
      </div>
    </div>

    <div class="card-footer">
      <div class="progress-section">
        <span class="progress-label">{{ t('taskGroup.card.progress') }}:</span>
        <el-progress 
          :percentage="progressPercentage" 
          :status="progressStatus"
          :stroke-width="6"
        />
        <span class="progress-text">
          {{ t('taskGroup.card.completedOf', { completed: group.completedTasks, total: group.totalTasks }) }}
        </span>
      </div>
      <div v-if="lastError" class="error-message">
        {{ t('taskGroup.lastError') }}: {{ lastError }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import type { TaskGroup, Task } from '@/types/task'

const { t } = useI18n()

interface Props {
  group: TaskGroup
}

interface Emits {
  (e: 'click'): void
}

const props = defineProps<Props>()
defineEmits<Emits>()

const displayedTasks = computed(() => {
  const tasks = props.group.tasks || []
  const sorted = [...tasks].sort((a, b) => {
    if (a.status === 'NEEDS_INTERVENTION') return -1
    if (b.status === 'NEEDS_INTERVENTION') return 1
    if (a.status === 'RUNNING') return -1
    if (b.status === 'RUNNING') return 1
    return 0
  })
  return sorted.slice(0, 3)
})

const hasNeedsIntervention = computed(() => {
  const tasks = props.group.tasks || []
  return tasks.some(task => task.status === 'NEEDS_INTERVENTION') ||
         (props.group.needsInterventionTasks && props.group.needsInterventionTasks > 0)
})

const statusTagType = computed(() => {
  if (hasNeedsIntervention.value) return 'danger'
  const map: Record<string, string> = {
    pending: 'info',
    in_progress: 'primary',
    completed: 'success',
    failed: 'danger'
  }
  return map[props.group.status] || 'info'
})

const statusIcon = computed(() => {
  if (hasNeedsIntervention.value) return '🔴'
  const map: Record<string, string> = {
    pending: '⚪',
    in_progress: '🔵',
    completed: '🟢',
    failed: '🔴'
  }
  return map[props.group.status] || '⚪'
})

const statusText = computed(() => {
  if (hasNeedsIntervention.value) return t('taskGroup.status.needsIntervention')
  const map: Record<string, string> = {
    pending: t('taskGroup.status.pending'),
    in_progress: t('taskGroup.status.inProgress'),
    completed: t('taskGroup.status.completed'),
    failed: t('taskGroup.status.failed')
  }
  return map[props.group.status] || props.group.status
})

const progressPercentage = computed(() => {
  const total = props.group.totalTasks || 0
  const completed = props.group.completedTasks || 0
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
})

const progressStatus = computed(() => {
  if (hasNeedsIntervention.value) return 'exception'
  if (props.group.status === 'completed') return 'success'
  return undefined
})

const lastError = computed(() => {
  const tasks = props.group.tasks || []
  const failedTask = tasks.find(t => t.error && t.status === 'NEEDS_INTERVENTION')
  return failedTask?.error
})

function getTaskStatusType(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    DEAD: 'danger',
    NEEDS_INTERVENTION: 'danger'
  }
  return map[status] || 'info'
}

function getTaskStatusText(status: string): string {
  const map: Record<string, string> = {
    PENDING: t('taskQueue.status.PENDING'),
    RUNNING: t('taskQueue.status.RUNNING'),
    COMPLETED: '✓',
    FAILED: '✗',
    DEAD: t('taskQueue.status.DEAD'),
    NEEDS_INTERVENTION: '!'
  }
  return map[status] || status
}
</script>

<style scoped>
.task-group-card {
  background: var(--card);
  border-radius: var(--radius);
  padding: 16px;
  margin-bottom: 12px;
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.task-group-card.clickable {
  cursor: pointer;
}

.task-group-card.clickable:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.task-group-card.needs-intervention {
  border-color: var(--danger-color);
  background: linear-gradient(135deg, var(--card) 0%, rgba(239, 68, 68, 0.05) 100%);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-badge {
  font-weight: 500;
}

.group-title {
  font-weight: 600;
  font-size: 15px;
  color: var(--text-primary);
}

.subtask-list {
  margin-bottom: 12px;
}

.subtask-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
  margin-bottom: 4px;
  font-size: 13px;
}

.subtask-item.intervention-required {
  background: rgba(239, 68, 68, 0.1);
  border-left: 3px solid var(--danger-color);
}

.agent-badge {
  color: var(--primary);
  font-weight: 500;
  font-size: 12px;
}

.task-title {
  flex: 1;
  color: var(--text-primary);
}

.task-status {
  font-size: 11px;
}

.intervention-warning {
  color: var(--danger-color);
  font-size: 11px;
  font-weight: 500;
}

.more-tasks {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 4px 8px;
}

.card-footer {
  border-top: 1px solid var(--border);
  padding-top: 12px;
}

.progress-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.progress-label {
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.progress-section :deep(.el-progress) {
  flex: 1;
}

.progress-text {
  font-size: 13px;
  color: var(--text-primary);
  font-weight: 500;
  white-space: nowrap;
}

.error-message {
  margin-top: 8px;
  font-size: 12px;
  color: var(--danger-color);
  background: rgba(239, 68, 68, 0.1);
  padding: 6px 10px;
  border-radius: var(--radius-sm);
}
</style>
