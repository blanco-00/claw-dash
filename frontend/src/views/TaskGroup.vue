<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  getTaskGroups,
  getTaskGroupDetail,
  detectCircularDependencies,
  getExecutableTasks
} from '@/api/tasks'
import type { TaskGroup, Task, TaskDependency } from '@/types/task'

const loading = ref(true)
const groups = ref<TaskGroup[]>([])
const selectedGroup = ref<TaskGroup | null>(null)
const selectedTask = ref<Task | null>(null)
const circularDeps = ref<string[] | null>(null)
const executableTasks = ref<Task[]>([])

// 刷新
async function refresh() {
  loading.value = true
  try {
    groups.value = await getTaskGroups()
    circularDeps.value = await detectCircularDependencies(
      groups.value.flatMap(g => (g as any).tasks || [])
    )
    executableTasks.value = await getExecutableTasks()
  } catch (error) {
    console.error('获取任务组失败:', error)
  } finally {
    loading.value = false
  }
}

// 打开任务组详情
async function openGroup(group: TaskGroup) {
  selectedGroup.value = await getTaskGroupDetail(group.id)
}

// 关闭详情
function closeDetail() {
  selectedGroup.value = null
  selectedTask.value = null
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
    case 'completed':
      return 'success'
    case 'running':
      return 'primary'
    case 'failed':
      return 'danger'
    case 'blocked':
      return 'warning'
    default:
      return 'info'
  }
}

// 执行顺序
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
        <h2 class="text-2xl font-bold">🔗 任务组与依赖</h2>
        <el-tag v-if="circularDeps?.length" type="danger">
          警告：{{ circularDeps.length }}个任务存在循环依赖
        </el-tag>
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

    <!-- 循环依赖警告 -->
    <el-alert
      v-if="circularDeps?.length"
      title="检测到循环依赖"
      :description="`任务 ${circularDeps.slice(0, 3).join(', ')}${circularDeps.length > 3 ? '...' : ''} 存在循环依赖`"
      type="error"
      :closable="false"
      class="mb-4"
    />

    <!-- 任务组列表 -->
    <el-row :gutter="20">
      <el-col
        v-for="group in groups"
        :key="group.id"
        :span="selectedGroup?.id === group.id ? 24 : 8"
        class="mb-4"
      >
        <el-card
          shadow="hover"
          :class="{ 'ring-2 ring-pink-500': selectedGroup?.id === group.id }"
          @click="openGroup(group)"
        >
          <template #header>
            <div class="flex items-center justify-between">
              <span class="font-bold">{{ group.name }}</span>
              <el-tag :type="getStatusColor(group.status)" size="small">
                {{ group.status }}
              </el-tag>
            </div>
          </template>

          <!-- 进度条 -->
          <div class="mb-3">
            <div class="flex justify-between text-sm mb-1">
              <span class="text-gray-500">进度</span>
              <span>{{ group.completedTasks }}/{{ group.totalTasks }}</span>
            </div>
            <el-progress
              :percentage="group.progress"
              :status="group.status === 'completed' ? 'success' : undefined"
            />
          </div>

          <!-- 任务列表（预览） -->
          <div v-if="selectedGroup?.id !== group.id" class="space-y-1">
            <div
              v-for="task in group.tasks.slice(0, 3)"
              :key="task.id"
              class="flex items-center justify-between text-sm"
            >
              <span class="truncate flex-1">{{ task.type }}</span>
              <el-tag :type="getStatusColor(getTaskStatus(task))" size="small">
                {{ getTaskStatus(task) }}
              </el-tag>
            </div>
            <div v-if="group.tasks.length > 3" class="text-gray-400 text-sm">
              +{{ group.tasks.length - 3 }} 更多
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务组详情抽屉 -->
    <el-drawer
      v-model="selectedGroup"
      title="任务组详情"
      direction="rtl"
      size="600px"
      @close="closeDetail"
    >
      <div v-if="selectedGroup" class="space-y-4">
        <!-- 组信息 -->
        <div class="flex items-center gap-4 pb-4 border-b">
          <div class="flex-1">
            <div class="text-lg font-bold">{{ selectedGroup.name }}</div>
            <div class="text-gray-500 text-sm">
              创建于：{{ new Date(selectedGroup.createdAt).toLocaleString('zh-CN') }}
            </div>
          </div>
          <el-progress type="circle" :percentage="selectedGroup.progress" :width="80" />
        </div>

        <!-- 依赖关系图示 -->
        <div v-if="selectedGroup.tasks.some(t => t.dependsOn?.length)" class="pb-4 border-b">
          <div class="font-bold mb-2">依赖关系</div>
          <div class="text-sm text-gray-500">
            <span v-for="(task, idx) in selectedGroup.tasks" :key="task.id">
              <span v-if="task.dependsOn?.length">
                {{ task.type }} → {{ task.dependsOn.join(', ') }}
              </span>
              <span v-if="idx < selectedGroup.tasks.length - 1">, </span>
            </span>
          </div>
        </div>

        <!-- 任务列表 -->
        <div>
          <div class="font-bold mb-2">任务列表（按执行顺序）</div>
          <div class="space-y-2">
            <div
              v-for="task in selectedGroup.tasks"
              :key="task.id"
              class="p-3 border rounded"
              :class="getOrderClass(task.orderIndex)"
            >
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-2">
                  <span v-if="task.orderIndex" class="text-pink-500 text-sm">
                    #{{ task.orderIndex }}
                  </span>
                  <span class="font-medium">{{ task.type }}</span>
                  <el-tag v-if="canExecute(task)" type="success" size="small"> 可执行 </el-tag>
                  <el-tag v-if="getTaskStatus(task) === 'blocked'" type="warning" size="small">
                    被阻塞
                  </el-tag>
                </div>
                <el-tag :type="getStatusColor(getTaskStatus(task))" size="small">
                  {{ getTaskStatus(task) }}
                </el-tag>
              </div>

              <!-- 依赖显示 -->
              <div v-if="task.dependsOn?.length" class="text-sm text-gray-500">
                依赖：{{ task.dependsOn.join(', ') }}
              </div>

              <!-- 状态流转 -->
              <div v-if="task.startedAt || task.completedAt" class="text-xs text-gray-400 mt-1">
                <span v-if="task.startedAt"
                  >开始：{{ new Date(task.startedAt).toLocaleString('zh-CN') }}</span
                >
                <span v-if="task.completedAt">
                  → 完成：{{ new Date(task.completedAt).toLocaleString('zh-CN') }}</span
                >
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
</style>
