<script setup lang="ts">
export interface Task {
  id: string
  type: string
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'
  createdAt?: string
}

const props = defineProps<{
  tasks: Task[]
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'view', taskId: string): void
}>()

function getStatusType(status: string): string {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'RUNNING': return 'primary'
    case 'COMPLETED': return 'success'
    case 'FAILED': return 'danger'
    default: return 'info'
  }
}

function getStatusText(status: string): string {
  switch (status) {
    case 'PENDING': return '待处理'
    case 'RUNNING': return '运行中'
    case 'COMPLETED': return '已完成'
    case 'FAILED': return '失败'
    default: return status
  }
}

function formatTime(time?: string): string {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function handleView(taskId: string) {
  emit('view', taskId)
}
</script>

<template>
  <el-card shadow="hover" class="recent-tasks-card">
    <template #header>
      <div class="flex items-center justify-between">
        <span class="font-bold list-title">最近任务</span>
        <el-button type="primary" link size="small" @click="$router.push('/tasks')">
          查看全部
        </el-button>
      </div>
    </template>
    
    <div v-if="loading" class="h-48 flex items-center justify-center">
      <el-icon class="is-loading text-2xl text-gray-400"><Loading /></el-icon>
    </div>
    
    <div v-else-if="tasks.length === 0" class="h-48 flex items-center justify-center empty-text">
      <div class="text-center">
        <div class="text-4xl mb-2">📝</div>
        <div>暂无任务</div>
      </div>
    </div>
    
    <div v-else class="space-y-2">
      <div
        v-for="task in tasks"
        :key="task.id"
        class="flex items-center justify-between p-2 task-item rounded cursor-pointer transition-colors"
        @click="handleView(task.id)"
      >
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <span class="text-xs font-mono task-id truncate">{{ task.id }}</span>
            <el-tag :type="getStatusType(task.status)" size="small">
              {{ getStatusText(task.status) }}
            </el-tag>
          </div>
          <div class="text-xs task-type mt-1">
            {{ task.type || '未知类型' }}
          </div>
        </div>
        <div class="text-xs task-time ml-2">
          {{ formatTime(task.createdAt) }}
        </div>
      </div>
    </div>
  </el-card>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>

<style scoped>
.recent-tasks-card {
  background-color: var(--card);
  border-color: var(--border);
}

.list-title {
  color: var(--text-primary);
}

.empty-text {
  color: var(--text-secondary);
  opacity: 0.7;
}

.task-item:hover {
  background-color: var(--hover-bg);
}

.task-id {
  color: var(--text-secondary);
}

.task-type {
  color: var(--text-secondary);
}

.task-time {
  color: var(--text-secondary);
  opacity: 0.7;
}
</style>
