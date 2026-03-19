<template>
  <div class="tasks-page">
    <div class="header">
      <h2>任务列表</h2>
      <div class="header-actions">
        <el-radio-group v-model="statusFilter" size="small" @change="loadTasks">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="PENDING">待处理</el-radio-button>
          <el-radio-button label="RUNNING">运行中</el-radio-button>
          <el-radio-button label="COMPLETED">已完成</el-radio-button>
          <el-radio-button label="FAILED">失败</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="loadTasks">
          <el-icon><Refresh /></el-icon>刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ counts.total || 0 }}</div>
          <div class="stat-label">总任务</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card pending">
          <div class="stat-value">{{ counts.pending || 0 }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card running">
          <div class="stat-value">{{ counts.running || 0 }}</div>
          <div class="stat-label">运行中</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card completed">
          <div class="stat-value">{{ counts.completed || 0 }}</div>
          <div class="stat-label">已完成</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="hover" class="stat-card failed">
          <div class="stat-value">{{ counts.failed || 0 }}</div>
          <div class="stat-label">失败</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table :data="tasks" style="width: 100%" v-loading="loading">
      <el-table-column prop="taskId" label="任务ID" width="200" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column prop="priority" label="优先级" width="80">
        <template #default="{ row }">
          <el-tag :type="row.priority >= 5 ? 'danger' : 'info'" size="small">
            {{ row.priority }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <div class="status-cell">
            <span class="status-dot" :class="row.status.toLowerCase()"></span>
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.created_at) }}
        </template>
      </el-table-column>
      <el-table-column prop="groupId" label="组ID" width="150">
        <template #default="{ row }">
          {{ row.groupId || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button-group>
            <el-button
              size="small"
              :disabled="row.status === 'RUNNING'"
              @click="startTask(row)"
              title="启动"
            >
              <el-icon><VideoPlay /></el-icon>
            </el-button>
            <el-button
              size="small"
              :disabled="row.status !== 'RUNNING'"
              @click="stopTask(row)"
              title="停止"
            >
              <el-icon><VideoPause /></el-icon>
            </el-button>
            <el-button
              size="small"
              :disabled="row.status === 'RUNNING'"
              @click="retryTask(row)"
              title="重试"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-button size="small" @click="viewTaskDetail(row)" title="详情">
              <el-icon><View /></el-icon>
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDetailDialog" title="任务详情" width="700px">
      <el-descriptions v-if="selectedTask" :column="2" border>
        <el-descriptions-item label="任务ID" :span="2">{{
          selectedTask.taskId
        }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ selectedTask.type }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ selectedTask.priority }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(selectedTask.status)">{{
            getStatusText(selectedTask.status)
          }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="组ID">{{ selectedTask.groupId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{
          formatTime(selectedTask.created_at)
        }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{
          selectedTask.started_at ? formatTime(selectedTask.started_at) : '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="完成时间">{{
          selectedTask.completed_at ? formatTime(selectedTask.completed_at) : '-'
        }}</el-descriptions-item>
        <el-descriptions-item label="Payload" :span="2">
          <pre class="payload-pre">{{ JSON.stringify(selectedTask.payload, null, 2) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="结果" :span="2">
          <pre class="payload-pre">{{ selectedTask.result || '无' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="错误" :span="2">
          <pre class="payload-pre error">{{ selectedTask.error || '无' }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, VideoPlay, VideoPause, View } from '@element-plus/icons-vue'
import request from '@/utils/request'

const tasks = ref<any[]>([])
const counts = ref<any>({})
const loading = ref(false)
const statusFilter = ref('')
const showDetailDialog = ref(false)
const selectedTask = ref<any>(null)
let refreshInterval: number | null = null

const loadTasks = async () => {
  loading.value = true
  try {
    const params = statusFilter.value ? { status: statusFilter.value } : {}
    const res = await request.get('/tasks', { params })
    tasks.value = res.data?.tasks || []
    counts.value = res.data?.counts || {}
  } catch (error) {
    console.error('Failed to load tasks:', error)
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'info',
    RUNNING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger',
    DEAD: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    RUNNING: '运行中',
    COMPLETED: '已完成',
    FAILED: '失败',
    DEAD: '已终止'
  }
  return map[status] || status
}

const formatTime = (timestamp: string) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleString('zh-CN')
}

const startTask = async (row: any) => {
  try {
    await request.post(`/api/tasks/${row.taskId}/start`)
    ElMessage.success('任务已启动')
    loadTasks()
  } catch (error: any) {
    ElMessage.error(error.message || '启动失败')
  }
}

const stopTask = async (row: any) => {
  try {
    await request.post(`/api/tasks/${row.taskId}/stop`)
    ElMessage.success('任务已停止')
    loadTasks()
  } catch (error: any) {
    ElMessage.error(error.message || '停止失败')
  }
}

const retryTask = async (row: any) => {
  try {
    await request.post(`/api/tasks/${row.taskId}/retry`)
    ElMessage.success('任务已重试')
    loadTasks()
  } catch (error: any) {
    ElMessage.error(error.message || '重试失败')
  }
}

const viewTaskDetail = (row: any) => {
  selectedTask.value = row
  showDetailDialog.value = true
}

const startAutoRefresh = () => {
  refreshInterval = window.setInterval(() => {
    loadTasks()
  }, 5000)
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

onMounted(() => {
  loadTasks()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.tasks-page {
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}
.header h2 {
  margin: 0;
}
.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
.stats-row {
  margin-bottom: 20px;
}
.stat-card {
  text-align: center;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}
.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}
.stat-card.pending .stat-value {
  color: #909399;
}
.stat-card.running .stat-value {
  color: #e6a23c;
}
.stat-card.completed .stat-value {
  color: #67c23a;
}
.stat-card.failed .stat-value {
  color: #f56c6c;
}
.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #909399;
}
.status-dot.pending {
  background: #909399;
}
.status-dot.running {
  background: #e6a23c;
  animation: pulse 1.5s infinite;
}
.status-dot.completed {
  background: #67c23a;
}
.status-dot.failed {
  background: #f56c6c;
}
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
.payload-pre {
  background: var(--el-fill-color-light);
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 12px;
  max-height: 200px;
  margin: 0;
}
.payload-pre.error {
  color: var(--el-color-danger);
}
</style>
