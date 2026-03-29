<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { listTasks, getTaskCounts } from '@/api/tasks'
import type { Task, TaskCounts } from '@/types/task'

const { t } = useI18n()

const loading = ref(true)
const tasks = ref<Task[]>([])
const counts = ref<TaskCounts>({
  pending: 0,
  running: 0,
  completed: 0,
  failed: 0,
  dead: 0,
  total: 0
})
const currentStatus = ref<string>('')

async function refresh() {
  loading.value = true
  try {
    tasks.value = await listTasks(50, currentStatus.value || undefined)
    counts.value = await getTaskCounts()
  } catch (error) {
    console.error(t('tasks.message.fetchError'), error)
  } finally {
    loading.value = false
  }
}

function filterByStatus(status: string) {
  currentStatus.value = status
  refresh()
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">📋</div>
        <div class="header-text">
          <h2 class="page-title">{{ t('tasks.title') }}</h2>
          <p class="page-subtitle">共 <span class="count">{{ counts.total }}</span> 个任务</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          {{ t('tasks.refresh') }}
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-card-grid">
      <div 
        class="stat-card" 
        :class="{ active: currentStatus === '' }"
        @click="filterByStatus('')"
      >
        <div class="stat-value">{{ counts.total }}</div>
        <div class="stat-label">{{ t('tasks.stats.all') }}</div>
      </div>
      <div 
        class="stat-card warning" 
        :class="{ active: currentStatus === 'PENDING' }"
        @click="filterByStatus('PENDING')"
      >
        <div class="stat-value">{{ counts.pending }}</div>
        <div class="stat-label">{{ t('tasks.stats.pending') }}</div>
      </div>
      <div 
        class="stat-card info" 
        :class="{ active: currentStatus === 'RUNNING' }"
        @click="filterByStatus('RUNNING')"
      >
        <div class="stat-value">{{ counts.running }}</div>
        <div class="stat-label">{{ t('tasks.stats.running') }}</div>
      </div>
      <div 
        class="stat-card success" 
        :class="{ active: currentStatus === 'COMPLETED' }"
        @click="filterByStatus('COMPLETED')"
      >
        <div class="stat-value">{{ counts.completed }}</div>
        <div class="stat-label">{{ t('tasks.stats.completed') }}</div>
      </div>
      <div 
        class="stat-card danger" 
        :class="{ active: currentStatus === 'FAILED' }"
        @click="filterByStatus('FAILED')"
      >
        <div class="stat-value">{{ counts.failed }}</div>
        <div class="stat-label">{{ t('tasks.stats.failed') }}</div>
      </div>
      <div 
        class="stat-card" 
        :class="{ active: currentStatus === 'DEAD' }"
        @click="filterByStatus('DEAD')"
      >
        <div class="stat-value" style="color: var(--text-secondary);">{{ counts.dead }}</div>
        <div class="stat-label">{{ t('tasks.stats.dead') }}</div>
      </div>
    </div>

    <!-- 任务列表 -->
    <div class="table-panel">
      <el-table :data="tasks" v-loading="loading" stripe>
        <el-table-column prop="id" :label="t('tasks.table.id')" width="80">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.id.slice(0, 8) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" :label="t('tasks.table.type')" width="150" />
        <el-table-column prop="priority" :label="t('tasks.table.priority')" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.priority === 'high' ? 'danger' : row.priority === 'medium' ? 'warning' : 'info'"
              size="small"
            >
              {{ t(`tasks.priority.${row.priority}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('tasks.table.status')" width="120">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 'COMPLETED' ? 'success' : row.status === 'FAILED' ? 'danger' : row.status === 'RUNNING' ? 'primary' : 'info'"
            >
              {{ t(`tasks.stats.${row.status.toLowerCase()}`) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" :label="t('tasks.table.created')" width="180">
          <template #default="{ row }">
            <span class="time-text">{{ new Date(row.createdAt).toLocaleString() }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="retryCount" :label="t('tasks.table.retry')" width="80">
          <template #default="{ row }">
            <span v-if="row.retryCount > 0" class="stat-num warning">{{ row.retryCount }}</span>
            <span v-else class="stat-num muted">-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
/* All styles now use global classes from style.css */
</style>
