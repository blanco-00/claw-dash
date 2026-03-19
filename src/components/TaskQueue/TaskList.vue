<template>
  <div class="task-list">
    <div class="filter-bar">
      <el-select
        v-model="statusFilter"
        :placeholder="t('taskQueue.filter.status')"
        clearable
        @change="handleFilterChange"
      >
        <el-option :label="t('taskQueue.filter.all')" value="" />
        <el-option :label="t('taskQueue.status.PENDING')" value="PENDING" />
        <el-option :label="t('taskQueue.status.RUNNING')" value="RUNNING" />
        <el-option :label="t('taskQueue.status.COMPLETED')" value="COMPLETED" />
        <el-option :label="t('taskQueue.status.FAILED')" value="FAILED" />
        <el-option :label="t('taskQueue.status.DEAD')" value="DEAD" />
      </el-select>

      <el-button type="primary" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        {{ t('taskQueue.button.refresh') }}
      </el-button>
    </div>

    <el-table :data="tasks" v-loading="loading" style="width: 100%">
      <el-table-column prop="taskId" :label="t('taskQueue.table.taskId')" width="180">
        <template #default="{ row }">
          <span class="task-id">{{ row.taskId }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="type" :label="t('taskQueue.table.type')" width="120" />

      <el-table-column prop="priority" :label="t('taskQueue.table.priority')" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)" size="small">
            {{ row.priority }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="status" :label="t('taskQueue.table.status')" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ t(`taskQueue.status.${row.status}`) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="retryCount" :label="t('taskQueue.table.retries')" width="80">
        <template #default="{ row }"> {{ row.retryCount }}/{{ row.maxRetries }} </template>
      </el-table-column>

      <el-table-column prop="claimedBy" :label="t('taskQueue.table.claimedBy')" width="120">
        <template #default="{ row }">
          {{ row.claimedBy || '-' }}
        </template>
      </el-table-column>

      <el-table-column prop="createdAt" :label="t('taskQueue.table.created')" width="160">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>

      <el-table-column :label="t('taskQueue.table.actions')" width="120">
        <template #default="{ row }">
          <el-button text size="small" @click="$emit('view', row)">{{ t('taskQueue.button.view') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalElements"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { listTasks } from '@/lib/openclaw/taskQueueApi'
import type { TaskQueueTask, TaskPageResponse } from '@/types/agentGraph'

const { t } = useI18n()

interface Props {
  initialStatus?: string
}

interface Emits {
  (e: 'view', task: TaskQueueTask): void
}

const props = withDefaults(defineProps<Props>(), {
  initialStatus: ''
})

const emit = defineEmits<Emits>()

const tasks = ref<TaskQueueTask[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const totalElements = ref(0)
const statusFilter = ref(props.initialStatus)

async function fetchTasks() {
  loading.value = true
  try {
    const response = await listTasks(
      currentPage.value - 1,
      pageSize.value,
      statusFilter.value || undefined
    )
    const data: TaskPageResponse = response.data as any
    tasks.value = data.content
    totalElements.value = data.totalElements
  } catch (error) {
    console.error('Failed to fetch tasks:', error)
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  currentPage.value = page
  fetchTasks()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  fetchTasks()
}

function handleFilterChange() {
  currentPage.value = 1
  fetchTasks()
}

function handleRefresh() {
  fetchTasks()
}

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

onMounted(() => {
  fetchTasks()
})

defineExpose({
  refresh: fetchTasks
})
</script>

<style scoped>
.task-list {
  padding: 16px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.task-id {
  font-family: monospace;
  font-size: 12px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
