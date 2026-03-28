<template>
  <el-drawer 
    v-model="visible" 
    :title="t('taskGroup.detail.title')" 
    size="550px"
    @close="$emit('close')"
  >
    <div v-if="group" class="task-group-detail">
      <div class="detail-section">
        <h4>{{ t('taskGroup.context.totalGoal') }}</h4>
        <p class="context-text">{{ group.totalGoal || group.description || t('taskGroup.context.noGoal') }}</p>
      </div>

      <div v-if="group.overallDesign" class="detail-section">
        <h4>{{ t('taskGroup.context.overallDesign') }}</h4>
        <pre class="design-text">{{ group.overallDesign }}</pre>
      </div>

      <div class="detail-section">
        <h4>{{ t('taskGroup.detail.taskList') }}</h4>
        <div class="subtask-list">
          <div 
            v-for="task in sortedTasks" 
            :key="task.id" 
            class="subtask-card"
            :class="{ 
              'intervention': task.status === 'NEEDS_INTERVENTION',
              'completed': task.status === 'COMPLETED'
            }"
          >
            <div class="subtask-header">
              <div class="subtask-info">
                <el-tag 
                  :type="getStatusType(task.status)" 
                  size="small"
                >
                  {{ getStatusIcon(task.status) }} {{ t(`taskQueue.status.${task.status}`) }}
                </el-tag>
                <span class="agent-name">[{{ task.assignedAgent || 'unassigned' }}]</span>
                <span class="task-type">{{ task.type }}</span>
              </div>
              <div v-if="task.status === 'NEEDS_INTERVENTION'" class="intervention-actions">
                <el-button size="small" type="warning" @click="handleReassign(task)">
                  {{ t('taskGroup.intervention.reassign') }}
                </el-button>
                <el-button size="small" type="info" @click="handleViewDetails(task)">
                  {{ t('taskGroup.intervention.viewDetails') }}
                </el-button>
                <el-button size="small" type="danger" @click="handleAbandon(task)">
                  {{ t('taskGroup.intervention.abandon') }}
                </el-button>
              </div>
            </div>

            <div v-if="task.context?.subtaskDescription" class="subtask-description">
              {{ task.context.subtaskDescription }}
            </div>

            <div v-if="task.status === 'NEEDS_INTERVENTION'" class="intervention-info">
              <div class="retry-info">
                {{ t('taskGroup.intervention.retryInfo', { count: task.retryCount, max: task.maxRetries }) }}
              </div>
              <div v-if="task.error" class="error-info">
                {{ t('taskGroup.intervention.lastError') }}: {{ task.error }}
              </div>
            </div>

            <div v-if="task.status === 'COMPLETED' && task.result" class="result-info">
              <strong>{{ t('taskQueue.detail.result') }}:</strong>
              <pre class="result-text">{{ formatResult(task.result) }}</pre>
            </div>

            <div class="timing-info">
              <span v-if="task.startedAt">
                {{ t('taskGroup.detail.startTime', { time: formatTime(task.startedAt) }) }}
              </span>
              <span v-if="task.completedAt">
                {{ t('taskGroup.detail.endTime', { time: formatTime(task.completedAt) }) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="group.completedAt" class="detail-section">
        <h4>{{ t('taskGroup.detail.completedAt') }}</h4>
        <p>{{ formatTime(group.completedAt) }}</p>
      </div>
    </div>

    <el-dialog 
      v-model="showReassignDialog" 
      :title="t('taskGroup.intervention.reassignTitle')"
      width="400px"
    >
      <el-form label-width="100px">
        <el-form-item :label="t('taskGroup.intervention.newAgent')">
          <el-input v-model="newAgentName" :placeholder="t('taskGroup.intervention.agentPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('taskGroup.intervention.reason')">
          <el-input 
            v-model="reassignReason" 
            type="textarea" 
            :rows="3"
            :placeholder="t('taskGroup.intervention.reasonPlaceholder')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReassignDialog = false">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="reassigning" @click="confirmReassign">
          {{ t('common.confirm') }}
        </el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { TaskGroup, Task } from '@/types/task'
import { reassignTask, abandonTaskGroup } from '@/lib/openclaw/taskGroupApi'

const { t } = useI18n()

interface Props {
  modelValue: boolean
  group?: TaskGroup | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
  (e: 'refresh'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)
const showReassignDialog = ref(false)
const selectedTask = ref<Task | null>(null)
const newAgentName = ref('')
const reassignReason = ref('')
const reassigning = ref(false)

watch(() => props.modelValue, val => { visible.value = val })
watch(visible, val => { emit('update:modelValue', val) })

const sortedTasks = computed(() => {
  if (!props.group?.tasks) return []
  return [...props.group.tasks].sort((a, b) => {
    const order = { NEEDS_INTERVENTION: 0, RUNNING: 1, PENDING: 2, COMPLETED: 3, FAILED: 4, DEAD: 5 }
    return (order[a.status] ?? 99) - (order[b.status] ?? 99)
  })
})

function getStatusType(status: string): string {
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

function getStatusIcon(status: string): string {
  const map: Record<string, string> = {
    PENDING: '⏳',
    RUNNING: '🔄',
    COMPLETED: '✅',
    FAILED: '❌',
    DEAD: '💀',
    NEEDS_INTERVENTION: '⚠️'
  }
  return map[status] || ''
}

function formatTime(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

function formatResult(result: unknown): string {
  if (typeof result === 'string') {
    try {
      return JSON.stringify(JSON.parse(result), null, 2)
    } catch {
      return result
    }
  }
  return JSON.stringify(result, null, 2)
}

function handleReassign(task: Task) {
  selectedTask.value = task
  newAgentName.value = task.assignedAgent || ''
  reassignReason.value = ''
  showReassignDialog.value = true
}

async function confirmReassign() {
  if (!selectedTask.value || !newAgentName.value.trim()) {
    ElMessage.warning(t('taskGroup.intervention.agentRequired'))
    return
  }

  reassigning.value = true
  try {
    await reassignTask(selectedTask.value.id, newAgentName.value.trim(), reassignReason.value)
    ElMessage.success(t('taskGroup.intervention.reassignSuccess'))
    showReassignDialog.value = false
    emit('refresh')
  } catch {
    ElMessage.error(t('taskGroup.intervention.reassignFailed'))
  } finally {
    reassigning.value = false
  }
}

function handleViewDetails(task: Task) {
  ElMessageBox.alert(
    `<pre style="max-height: 400px; overflow: auto;">${formatResult(task)}</pre>`,
    t('taskGroup.intervention.taskDetails'),
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: t('common.close')
    }
  )
}

async function handleAbandon(task: Task) {
  try {
    await ElMessageBox.confirm(
      t('taskGroup.intervention.confirmAbandon'),
      t('taskGroup.intervention.abandonTitle'),
      { type: 'warning' }
    )
    
    if (props.group?.id) {
      await abandonTaskGroup(props.group.id)
      ElMessage.success(t('taskGroup.intervention.abandonSuccess'))
      emit('refresh')
    }
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.task-group-detail {
  padding: 0 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border);
  padding-bottom: 8px;
}

.context-text {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

.design-text {
  background: var(--bg-secondary);
  padding: 12px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  color: var(--text-primary);
}

.subtask-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subtask-card {
  background: var(--bg-secondary);
  border-radius: var(--radius);
  padding: 12px;
  border: 1px solid var(--border);
}

.subtask-card.intervention {
  border-color: var(--danger-color);
  background: linear-gradient(135deg, var(--bg-secondary) 0%, rgba(239, 68, 68, 0.05) 100%);
}

.subtask-card.completed {
  opacity: 0.8;
}

.subtask-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.subtask-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.agent-name {
  color: var(--primary);
  font-weight: 500;
  font-size: 12px;
}

.task-type {
  color: var(--text-primary);
  font-weight: 500;
}

.intervention-actions {
  display: flex;
  gap: 6px;
}

.subtask-description {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 8px;
  padding: 8px;
  background: var(--card);
  border-radius: var(--radius-sm);
}

.intervention-info {
  margin-top: 8px;
  padding: 8px;
  background: rgba(239, 68, 68, 0.1);
  border-radius: var(--radius-sm);
  font-size: 12px;
}

.retry-info {
  color: var(--danger-color);
  font-weight: 500;
  margin-bottom: 4px;
}

.error-info {
  color: var(--text-secondary);
}

.result-info {
  margin-top: 8px;
  font-size: 12px;
}

.result-text {
  background: var(--success-color);
  color: white;
  padding: 8px;
  border-radius: var(--radius-sm);
  margin: 4px 0 0 0;
  font-size: 11px;
  max-height: 150px;
  overflow: auto;
}

.timing-info {
  margin-top: 8px;
  font-size: 11px;
  color: var(--text-secondary);
  display: flex;
  gap: 16px;
}
</style>
