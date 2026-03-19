<template>
  <el-drawer v-model="visible" title="Task Details" size="500px" @close="$emit('close')">
    <div v-if="task" class="task-detail">
      <div class="detail-section">
        <h4>Basic Info</h4>
        <div class="detail-row">
          <span class="label">Task ID:</span>
          <span class="value mono">{{ task.taskId }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Type:</span>
          <span class="value">{{ task.type }}</span>
        </div>
        <div class="detail-row">
          <span class="label">Status:</span>
          <el-tag :type="getStatusType(task.status)">{{ task.status }}</el-tag>
        </div>
        <div class="detail-row">
          <span class="label">Priority:</span>
          <el-tag :type="getPriorityType(task.priority)">{{ task.priority }}</el-tag>
        </div>
        <div class="detail-row">
          <span class="label">Retries:</span>
          <span class="value">{{ task.retryCount }} / {{ task.maxRetries }}</span>
        </div>
      </div>

      <div class="detail-section">
        <h4>Timing</h4>
        <div class="detail-row">
          <span class="label">Created:</span>
          <span class="value">{{ formatDate(task.createdAt) }}</span>
        </div>
        <div v-if="task.startedAt" class="detail-row">
          <span class="label">Started:</span>
          <span class="value">{{ formatDate(task.startedAt) }}</span>
        </div>
        <div v-if="task.completedAt" class="detail-row">
          <span class="label">Completed:</span>
          <span class="value">{{ formatDate(task.completedAt) }}</span>
        </div>
        <div v-if="task.scheduledAt" class="detail-row">
          <span class="label">Scheduled:</span>
          <span class="value">{{ formatDate(task.scheduledAt) }}</span>
        </div>
      </div>

      <div v-if="task.claimedBy" class="detail-section">
        <h4>Execution</h4>
        <div class="detail-row">
          <span class="label">Claimed By:</span>
          <span class="value">{{ task.claimedBy }}</span>
        </div>
      </div>

      <div v-if="task.payload" class="detail-section">
        <h4>Payload</h4>
        <pre class="code-block">{{ formatJson(task.payload) }}</pre>
      </div>

      <div v-if="task.result" class="detail-section">
        <h4>Result</h4>
        <pre class="code-block success">{{ formatJson(task.result) }}</pre>
      </div>

      <div v-if="task.error" class="detail-section">
        <h4>Error</h4>
        <pre class="code-block error">{{ task.error }}</pre>
      </div>

      <div v-if="task.dependsOn" class="detail-section">
        <h4>Dependencies</h4>
        <div class="depends-list">
          <el-tag v-for="dep in task.dependsOn.split(',')" :key="dep" size="small">
            {{ dep }}
          </el-tag>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { TaskQueueTask } from '@/types/agentGraph'

interface Props {
  modelValue: boolean
  task?: TaskQueueTask
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'close'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const visible = ref(props.modelValue)

watch(
  () => props.modelValue,
  val => {
    visible.value = val
  }
)

watch(visible, val => {
  emit('update:modelValue', val)
})

function getStatusType(status: string): string {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    DEAD: 'danger'
  }
  return map[status] || 'info'
}

function getPriorityType(priority: number): string {
  if (priority >= 10) return 'danger'
  if (priority >= 5) return 'warning'
  return 'info'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString()
}

function formatJson(json: string): string {
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch {
    return json
  }
}
</script>

<style scoped>
.task-detail {
  padding: 0 8px;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-row .label {
  color: #909399;
}

.detail-row .value {
  color: #606266;
}

.detail-row .value.mono {
  font-family: monospace;
  font-size: 12px;
}

.code-block {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 12px;
  font-family: monospace;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.code-block.success {
  background: #f0f9eb;
  color: #67c23a;
}

.code-block.error {
  background: #fef0f0;
  color: #f56c6c;
}

.depends-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
