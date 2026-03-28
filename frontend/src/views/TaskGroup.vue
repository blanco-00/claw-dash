<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  getTaskGroups,
  getTaskGroupDetail,
  getExecutableTasks
} from '@/api/tasks'
import type { TaskGroup, Task } from '@/types/task'

const loading = ref(true)
const groups = ref<any[]>([])
const selectedGroup = ref<any>(null)
const drawerVisible = ref(false)
const executableTasks = ref<Task[]>([])

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
    selectedTask.value = null
  }, 300) // Wait for drawer animation
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
  <div class="taskgroup-page">
    <!-- 页面头部 -->
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <h2 class="text-2xl font-bold">🔗 任务组</h2>
        <el-tag type="info">共 {{ totalGroups }} 个任务组</el-tag>
      </div>
      <el-button type="primary" :loading="loading" @click="refresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 可执行任务提示 -->
    <el-alert
      v-if="executableTasks.length > 0"
      title="有可执行任务"
      :description="`${executableTasks.length}个任务的依赖已满足，可以立即执行`"
      type="success"
      :closable="false"
      class="mb-4"
    />

    <!-- 任务组列表 -->
    <el-row :gutter="16">
      <el-col
        v-for="group in groups"
        :key="group.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
        class="mb-4"
      >
        <el-card
          shadow="hover"
          :class="{ 'ring-2 ring-pink-500': selectedGroup?.id === group.id }"
          @click="openGroup(group)"
          class="task-group-card"
        >
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold text-sm truncate flex-1 mr-2">{{ group.name }}</span>
              <el-tag :type="getStatusColor(group.status)" size="small">
                {{ getStatusLabel(group.status) }}
              </el-tag>
            </div>
          </template>

          <!-- 进度信息 -->
          <div class="mb-2">
            <div class="flex justify-between text-xs text-gray-500 mb-1">
              <span>{{ group.completedTasks }}/{{ group.totalTasks }} 任务</span>
              <span>{{ getProgressPercentage(group) }}%</span>
            </div>
            <el-progress
              :percentage="getProgressPercentage(group)"
              :status="group.status === 'COMPLETED' ? 'success' : undefined"
              :stroke-width="8"
            />
          </div>

          <!-- 状态标签 -->
          <div class="text-xs text-gray-400">
            {{ getTimeLabel(group) }}
          </div>
        </el-card>
      </el-col>
    </el-row>

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
      size="600px"
      @close="closeDetail"
    >
      <div v-if="selectedGroup" class="space-y-4">
        <!-- 组信息头部 -->
        <div class="bg-gray-50 p-4 rounded-lg mb-4">
          <div class="flex items-center gap-6">
            <el-progress type="circle" :percentage="selectedGroupProgress" :width="80" />
            <div class="flex-1">
              <div class="text-lg font-bold mb-1">{{ selectedGroup.name }}</div>
              <div class="text-gray-500 text-sm mb-2">
                创建于：{{ new Date(selectedGroup.createdAt).toLocaleString('zh-CN') }}
              </div>
              <!-- 任务统计 -->
              <div class="flex gap-4 text-sm">
                <span class="text-green-500">✓ {{ selectedGroup.completedTasks }} 完成</span>
                <span class="text-blue-500">⚡ {{ selectedGroup.runningTasks }} 进行中</span>
                <span class="text-orange-500">⏳ {{ selectedGroup.pendingTasks }} 待处理</span>
                <span v-if="selectedGroup.failedTasks" class="text-red-500">✗ {{ selectedGroup.failedTasks }} 失败</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 任务描述 -->
        <div v-if="selectedGroup.description" class="mb-4">
          <div class="text-sm text-gray-500 mb-1">描述</div>
          <div class="text-sm">{{ selectedGroup.description }}</div>
        </div>

        <!-- 依赖关系图示 -->
        <div v-if="selectedGroup.tasks?.some(t => t.dependsOn?.length)" class="mb-4 p-3 bg-yellow-50 rounded">
          <div class="font-bold mb-2 text-sm">⚠️ 依赖关系</div>
          <div class="text-sm text-gray-600">
            <div v-for="(task, idx) in selectedGroup.tasks" :key="task.id">
              <span v-if="task.dependsOn?.length">
                <span class="font-medium">{{ task.type }}</span>
                ← 依赖 {{ task.dependsOn.join(', ') }}
              </span>
            </div>
          </div>
        </div>

        <!-- 任务列表 -->
        <div>
          <div class="font-bold mb-3">任务列表</div>
          <div class="space-y-2 max-h-96 overflow-y-auto">
            <div
              v-for="task in selectedGroup.tasks"
              :key="task.id"
              class="p-3 border rounded bg-white"
            >
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-sm">{{ task.type }}</span>
                  <el-tag 
                    :type="task.status === 'COMPLETED' ? 'success' : task.status === 'RUNNING' ? 'primary' : task.status === 'FAILED' ? 'danger' : 'info'" 
                    size="small"
                  >
                    {{ getStatusLabel(task.status) }}
                  </el-tag>
                </div>
              </div>

              <!-- 依赖显示 -->
              <div v-if="task.dependsOn?.length" class="text-xs text-gray-400 mt-1">
                依赖: {{ task.dependsOn.join(', ') }}
              </div>

              <!-- 时间 -->
              <div v-if="task.startedAt || task.completedAt" class="text-xs text-gray-400 mt-1">
                <span v-if="task.startedAt">{{ new Date(task.startedAt).toLocaleString('zh-CN') }}</span>
                <span v-if="task.completedAt"> → {{ new Date(task.completedAt).toLocaleString('zh-CN') }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script lang="ts">
import { Refresh } from '@element-plus/icons-vue'
export default {
  components: { Refresh }
}
</script>

<style scoped>
.taskgroup-page {
  padding: 20px;
}

.task-group-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.task-group-card:hover {
  transform: translateY(-2px);
}

.task-group-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: none;
}

.task-group-card :deep(.el-card__body) {
  padding: 12px 16px;
}
</style>
