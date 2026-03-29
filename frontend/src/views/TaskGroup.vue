<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getTaskGroups,
  getTaskGroupDetail,
  getExecutableTasks,
  deleteTaskGroup,
  createTaskGroup
} from '@/api/tasks'
import type { TaskGroup, Task } from '@/types/task'

const loading = ref(true)
const groups = ref<any[]>([])
const selectedGroup = ref<any>(null)
const drawerVisible = ref(false)
const executableTasks = ref<Task[]>([])
const showCreateDialog = ref(false)
const creating = ref(false)
const createForm = ref({
  name: '',
  description: ''
})

// 分页状态
const currentPage = ref(1)
const pageSize = ref(8)
const totalGroups = ref(0)

// 计算选中任务组的进度
const selectedGroupProgress = computed(() => {
  if (!selectedGroup.value || !selectedGroup.value.totalTasks) return 0
  const total = selectedGroup.value.totalTasks as number
  const completed = selectedGroup.value.completedTasks as number
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
})

const tasksByAgent = computed(() => {
  if (!selectedGroup.value?.tasks) return new Map<string, Task[]>()
  
  const grouped = new Map<string, Task[]>()
  
  for (const task of selectedGroup.value.tasks) {
    const agentId = task.assignedAgent || 'Unassigned'
    if (!grouped.has(agentId)) {
      grouped.set(agentId, [])
    }
    grouped.get(agentId)!.push(task)
  }
  
  return grouped
})

function getAgentStats(tasks: Task[]) {
  return {
    total: tasks.length,
    completed: tasks.filter(t => t.status === 'COMPLETED').length,
    running: tasks.filter(t => t.status === 'RUNNING').length,
    pending: tasks.filter(t => t.status === 'PENDING').length,
    failed: tasks.filter(t => t.status === 'FAILED').length
  }
}

function getAgentProgress(tasks: Task[]) {
  const stats = getAgentStats(tasks)
  if (stats.total === 0) return 0
  return Math.round((stats.completed / stats.total) * 100)
}

// 进度环颜色 - 100%时用浅紫色，与紫色背景和谐
const progressColor = (percentage: number) => {
  if (percentage >= 100) {
    return '#e0c9fa'
  }
  return '#ffffff'
}

// 刷新
async function refresh() {
  loading.value = true
  try {
    const response = await getTaskGroups(currentPage.value, pageSize.value)
    groups.value = response?.data?.content || []
    totalGroups.value = response?.data?.totalElements || 0
    executableTasks.value = await getExecutableTasks()
  } catch (error) {
    console.error('获取任务组失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页变化
function handlePageChange(page: number) {
  currentPage.value = page
  refresh()
}

function handleSizeChange(size: number) {
  pageSize.value = size
  currentPage.value = 1
  refresh()
}

// 打开任务组详情
async function openGroup(group: any) {
  selectedGroup.value = await getTaskGroupDetail(group.id)
  drawerVisible.value = true
}

// 关闭详情
function closeDetail() {
  drawerVisible.value = false
  setTimeout(() => {
    selectedGroup.value = null
  }, 300)
}

// 删除任务组
async function handleDelete(group: any) {
  const taskCount = group.totalTasks || 0
  const message = taskCount > 0
    ? `确定删除任务组"${group.name}"吗？该操作将同时删除其下的${taskCount}个子任务，且不可恢复。`
    : `确定删除任务组"${group.name}"吗？该操作不可恢复。`
  
  try {
    await ElMessageBox.confirm(message, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    
    const success = await deleteTaskGroup(group.id)
    if (success) {
      ElMessage.success('删除成功')
      refresh()
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消
  }
}

// 创建任务组
async function handleCreate() {
  if (!createForm.value.name.trim()) {
    ElMessage.warning('请输入任务组名称')
    return
  }
  
  creating.value = true
  try {
    const result = await createTaskGroup({
      name: createForm.value.name.trim(),
      description: createForm.value.description.trim() || undefined
    })
    
    if (result) {
      ElMessage.success('创建成功')
      showCreateDialog.value = false
      createForm.value = { name: '', description: '' }
      refresh()
    } else {
      ElMessage.error('创建失败')
    }
  } catch {
    ElMessage.error('创建失败')
  } finally {
    creating.value = false
  }
}

// 获取任务状态
function getTaskStatus(task: Task): string {
  if (task.status === 'PENDING' && task.dependsOn?.length) {
    const deps = task.dependsOn.map(depId => {
      const depTask = selectedGroup.value?.tasks.find(t => t.id === depId)
      return depTask?.status
    })
    if (deps?.some(s => s !== 'COMPLETED')) {
      return 'blocked'
    }
  }
  return task.status.toLowerCase()
}

// 判断是否可执行
function canExecute(task: Task): boolean {
  if (task.status !== 'PENDING') return false
  if (!task.dependsOn || task.dependsOn.length === 0) return true
  return task.dependsOn.every(depId => {
    const depTask = selectedGroup.value?.tasks.find(t => t.id === depId)
    return depTask?.status === 'COMPLETED'
  })
}

// 状态颜色
function getStatusColor(status: string): string {
  switch (status) {
    case 'COMPLETED':
      return 'success'
    case 'RUNNING':
      return 'primary'
    case 'FAILED':
      return 'danger'
    case 'IN_PROGRESS':
      return 'warning'
    default:
      return 'info'
  }
}

// 状态中文标签
function getStatusLabel(status: string): string {
  switch (status) {
    case 'COMPLETED': return '已完成'
    case 'RUNNING': return '进行中'
    case 'FAILED': return '失败'
    case 'IN_PROGRESS': return '进行中'
    case 'PENDING': return '待处理'
    default: return status
  }
}

// 计算任务组进度百分比
// 计算任务组进度百分比
function getProgressPercentage(group: any): number {
  const total = group.totalTasks || 0
  const completed = group.completedTasks || 0
  if (total === 0) return 0
  return Math.round((completed / total) * 100)
}

// 时间标签
function getTimeLabel(group: any): string {
  if (group.completedAt) {
    return `完成于 ${new Date(group.completedAt).toLocaleDateString('zh-CN')}`
  }
  if (group.createdAt) {
    return `创建于 ${new Date(group.createdAt).toLocaleDateString('zh-CN')}`
  }
  return ''
}

// 执行顺序样式
function getOrderClass(order?: number): string {
  if (order === undefined || order === 0) return ''
  return 'border-l-2 border-pink-400 pl-2'
}

onMounted(() => {
  refresh()
})
</script>

<template>
  <div class="page-container taskgroup-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">📋</div>
        <div class="header-text">
          <h2 class="page-title">任务组管理</h2>
          <p class="page-subtitle">共 <span class="count">{{ totalGroups }}</span> 个任务组</p>
        </div>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          创建任务组
        </el-button>
        <el-button type="default" :loading="loading" @click="refresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 可执行任务提示 -->
    <div v-if="executableTasks.length > 0" class="alert-banner success">
      <div class="alert-icon">⚡</div>
      <div class="alert-content">
        <span class="alert-title">有可执行任务</span>
        <span class="alert-desc">{{ executableTasks.length }}个任务的依赖已满足，可以立即执行</span>
      </div>
    </div>

    <!-- 任务组列表 - 表格布局 -->
    <div class="table-panel">
      <el-table 
        :data="groups" 
        v-loading="loading"
        stripe
        highlight-current-row
        @row-click="openGroup"
      >
        <el-table-column prop="name" label="任务组名称" min-width="200">
          <template #default="{ row }">
            <div class="name-cell">
              <span class="name-text">{{ row.name }}</span>
              <span v-if="row.description" class="desc-text">{{ row.description }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)" size="small" class="status-tag">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="进度" width="200" align="center">
          <template #default="{ row }">
            <div class="progress-cell">
              <el-progress
                :percentage="getProgressPercentage(row)"
                :stroke-width="6"
                :color="getProgressPercentage(row) >= 100 ? '#8b47ea' : '#722ed1'"
                class="inline-progress"
              />
              <span class="progress-text">{{ row.completedTasks }}/{{ row.totalTasks }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="完成" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-num success">{{ row.completedTasks }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="进行中" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-num primary">{{ row.runningTasks }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="待处理" width="70" align="center">
          <template #default="{ row }">
            <span class="stat-num warning">{{ row.pendingTasks }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="失败" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.failedTasks" class="stat-num danger">{{ row.failedTasks }}</span>
            <span v-else class="stat-num inactive">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdAt" label="创建时间" width="110" align="center">
          <template #default="{ row }">
            <span class="time-text">
              {{ row.createdAt ? new Date(row.createdAt).toLocaleDateString('zh-CN') : '-' }}
            </span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click.stop="openGroup(row)">
              查看详情
            </el-button>
            <el-button type="danger" size="small" text @click.stop="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="flex justify-end mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="totalGroups"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 任务组详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="任务组详情"
      direction="rtl"
      size="550px"
      @close="closeDetail"
    >
      <div v-if="selectedGroup" class="drawer-content">
        <!-- 概览卡片 -->
        <div class="overview-card">
          <div style="display: flex; align-items: center; gap: 16px;">
            <el-progress type="circle" :percentage="selectedGroupProgress" :width="70" :stroke-width="8" :color="progressColor" />
            <div style="flex: 1;">
              <h3 class="overview-title">{{ selectedGroup.name }}</h3>
              <p class="overview-subtitle">
                创建于 {{ new Date(selectedGroup.createdAt).toLocaleString('zh-CN') }}
              </p>
              <div style="display: flex; gap: 12px; flex-wrap: wrap;">
                <span class="stat-badge">
                  <span class="badge-num">{{ selectedGroup.completedTasks }}</span>
                  <span class="badge-label">完成</span>
                </span>
                <span class="stat-badge">
                  <span class="badge-num">{{ selectedGroup.runningTasks }}</span>
                  <span class="badge-label">进行中</span>
                </span>
                <span class="stat-badge">
                  <span class="badge-num">{{ selectedGroup.pendingTasks }}</span>
                  <span class="badge-label">待处理</span>
                </span>
                <span v-if="selectedGroup.failedTasks" class="stat-badge">
                  <span class="badge-num">{{ selectedGroup.failedTasks }}</span>
                  <span class="badge-label">失败</span>
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 描述 -->
        <div v-if="selectedGroup.description" class="section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            描述
          </div>
          <div class="section-content">{{ selectedGroup.description }}</div>
        </div>

        <!-- 依赖关系 -->
        <div v-if="selectedGroup.tasks?.some(t => t.dependsOn?.length)" class="section">
          <div class="section-title warning">
            <el-icon><Warning /></el-icon>
            依赖关系
          </div>
          <div class="section-content">
            <div 
              v-for="task in selectedGroup.tasks.filter(t => t.dependsOn?.length)" 
              :key="task.id"
              style="margin-bottom: 8px;"
            >
              <strong>{{ task.type }}</strong> ← 依赖 {{ task.dependsOn.join(', ') }}
            </div>
          </div>
        </div>

        <!-- 任务列表 - 按代理分组 -->
        <div class="section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            任务列表 ({{ selectedGroup.tasks?.length || 0 }})
          </div>
          
          <!-- 总体进度条 -->
          <div class="total-progress">
            <el-progress 
              :percentage="selectedGroupProgress" 
              :stroke-width="10"
              :color="selectedGroupProgress >= 100 ? '#8b47ea' : '#722ed1'"
            />
            <div class="progress-label">
              {{ selectedGroup.completedTasks || 0 }} / {{ selectedGroup.totalTasks || 0 }} 已完成
            </div>
          </div>
          
          <!-- 按代理分组显示 -->
          <div v-if="tasksByAgent.size > 0" class="agent-groups">
            <div 
              v-for="[agentId, tasks] in tasksByAgent" 
              :key="agentId"
              class="agent-group"
            >
              <div class="agent-group-header">
                <div class="agent-info">
                  <span class="agent-icon">🤖</span>
                  <span class="agent-name">{{ agentId }}</span>
                </div>
                <div class="agent-stats">
                  <el-tag size="small" type="warning">{{ getAgentStats(tasks).pending }} 待处理</el-tag>
                  <el-tag size="small" type="primary">{{ getAgentStats(tasks).running }} 运行中</el-tag>
                  <el-tag size="small" type="success">{{ getAgentStats(tasks).completed }} 完成</el-tag>
                  <el-tag v-if="getAgentStats(tasks).failed > 0" size="small" type="danger">{{ getAgentStats(tasks).failed }} 失败</el-tag>
                </div>
              </div>
              
              <!-- 代理进度条 -->
              <div class="agent-progress">
                <el-progress 
                  :percentage="getAgentProgress(tasks)" 
                  :stroke-width="6"
                  :color="getAgentProgress(tasks) >= 100 ? '#52c41a' : '#722ed1'"
                />
              </div>
              
              <!-- 代理下的任务列表 -->
              <div class="task-list">
                <div
                  v-for="task in tasks"
                  :key="task.id"
                  class="task-item"
                  :class="'status-' + task.status.toLowerCase()"
                >
                  <div class="task-header">
                    <span class="task-type">{{ task.type }}</span>
                    <el-tag 
                      :type="task.status === 'COMPLETED' ? 'success' : task.status === 'RUNNING' ? 'primary' : task.status === 'FAILED' ? 'danger' : 'info'" 
                      size="small"
                    >
                      {{ getStatusLabel(task.status) }}
                    </el-tag>
                  </div>
                  <div v-if="task.dependsOn?.length" class="task-deps">
                    依赖: {{ task.dependsOn.join(', ') }}
                  </div>
                  <div v-if="task.startedAt || task.completedAt" class="task-time">
                    <span v-if="task.startedAt">开始: {{ new Date(task.startedAt).toLocaleString('zh-CN') }}</span>
                    <span v-if="task.completedAt" class="ml-2">完成: {{ new Date(task.completedAt).toLocaleString('zh-CN') }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 无代理分组的任务（兼容旧数据） -->
          <div v-else class="task-list">
            <div
              v-for="task in selectedGroup.tasks"
              :key="task.id"
              class="task-item"
              :class="'status-' + task.status.toLowerCase()"
            >
              <div class="task-header">
                <span class="task-type">{{ task.type }}</span>
                <el-tag 
                  :type="task.status === 'COMPLETED' ? 'success' : task.status === 'RUNNING' ? 'primary' : task.status === 'FAILED' ? 'danger' : 'info'" 
                  size="small"
                >
                  {{ getStatusLabel(task.status) }}
                </el-tag>
              </div>
              <div v-if="task.dependsOn?.length" class="task-deps">
                依赖: {{ task.dependsOn.join(', ') }}
              </div>
              <div v-if="task.startedAt || task.completedAt" class="task-time">
                <span v-if="task.startedAt">开始: {{ new Date(task.startedAt).toLocaleString('zh-CN') }}</span>
                <span v-if="task.completedAt" class="ml-2">完成: {{ new Date(task.completedAt).toLocaleString('zh-CN') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 创建任务组对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建任务组" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="任务组名称" required>
          <el-input v-model="createForm.name" placeholder="请输入任务组名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入描述（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Refresh, Document, Warning, List, Plus } from '@element-plus/icons-vue'
export default {
  components: { Refresh, Document, Warning, List, Plus }
}
</script>

<style scoped>
.drawer-content {
  padding: 0 4px;
}

.total-progress {
  margin-bottom: 16px;
}

.progress-label {
  text-align: center;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.agent-groups {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.agent-group {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 12px;
  border: 1px solid var(--border);
}

.agent-group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-icon {
  font-size: 18px;
}

.agent-name {
  font-weight: 600;
  color: var(--text);
}

.agent-stats {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.agent-progress {
  margin-bottom: 12px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-item {
  background: var(--card);
  border-radius: 6px;
  padding: 10px 12px;
  border-left: 3px solid var(--border);
}

.task-item.status-completed {
  border-left-color: #52c41a;
}

.task-item.status-running {
  border-left-color: #722ed1;
}

.task-item.status-failed {
  border-left-color: #ff4d4f;
}

.task-item.status-pending {
  border-left-color: #faad14;
  opacity: 0.8;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-type {
  font-weight: 500;
  font-size: 14px;
}

.task-deps {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}

.task-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 4px;
}
</style>
